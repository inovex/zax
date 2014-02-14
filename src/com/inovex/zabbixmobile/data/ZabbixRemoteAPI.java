package com.inovex.zabbixmobile.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.FatalException.Type;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Application;
import com.inovex.zabbixmobile.model.ApplicationItemRelation;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.GraphItem;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.HostHostGroupRelation;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Screen;
import com.inovex.zabbixmobile.model.ScreenItem;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerHostGroupRelation;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.util.HttpClientWrapper;
import com.inovex.zabbixmobile.util.JsonArrayOrObjectReader;
import com.inovex.zabbixmobile.util.JsonObjectReader;

/**
 * This class encapsulates all calls to the Zabbix API.
 * 
 */
public class ZabbixRemoteAPI {
	private static final String API_PHP = "api_jsonrpc.php";
	private static final String ZABBIX_ERROR_NO_API_ACCESS = "No API access";
	private static final String ZABBIX_ERROR_NOT_AUTHORIZED = "Not authorized";
	// Sometimes this message contains a dot in the end, sometimes it doesn't.
	private static final String ZABBIX_ERROR_LOGIN_INCORRECT = "Login name or password is incorrect(\\.?)";
	private static final int RECORDS_PER_INSERT_BATCH = 50;
	private static final String TAG = ZabbixRemoteAPI.class.getSimpleName();
	private static final String ZABBIX_ACCOUNT_BLOCKED = "Account is blocked for (.*)";

	class CustomSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public CustomSSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}
	}

	/**
	 * global constants
	 */
	public class ZabbixConfig {
		public static final int APPLICATION_GET_LIMIT = 1000;
		public static final int EVENTS_GET_LIMIT = 60;
		public static final int HISTORY_GET_TIME_FROM_SHIFT = 24 * 60 * 60; // -24h
		public static final int HISTORY_GET_LIMIT = 8000;
		public static final int HOSTGROUP_GET_LIMIT = 200;
		public static final int HOST_GET_LIMIT = 300;
		public static final int ITEM_GET_LIMIT = 200;
		public static final int TRIGGER_GET_LIMIT = 100;
		public static final int EVENT_GET_TIME_FROM_SHIFT = 7 * 24 * 60 * 60; // -7
																				// days
		public static final int CACHE_LIFETIME_APPLICATIONS = 2 * 24 * 60 * 60; // 2
																				// days
		public static final int CACHE_LIFETIME_EVENTS = 120;
		public static final int CACHE_LIFETIME_HISTORY_DETAILS = 4 * 60;
		public static final int CACHE_LIFETIME_HOST_GROUPS = 7 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_HOSTS = 2 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_SCREENS = 2 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_ITEMS = 4 * 60;
		public static final int CACHE_LIFETIME_TRIGGERS = 2 * 60;
		public static final long STATUS_SHOW_TRIGGER_TIME = 14 * 24 * 60 * 60;
		public static final int HTTP_CONNECTION_TIMEOUT = 10000;
		public static final int HTTP_SOCKET_TIMEOUT = 30000;
	}

	private final HttpClientWrapper httpClient;
	private final DatabaseHelper databaseHelper;
	private final ZaxPreferences mPreferences;
	private String zabbixUrl;
	private final Context mContext;
	private boolean isVersion2 = true;
	/**
	 * The API version. From 1.8.3 (maybe earlier) to 2.0 (excluded), this was
	 * 1.3. With 2.0, it changed to 1.4. Finally, since 2.0.4, the API version
	 * matches the program version.
	 */
	private String apiVersion = "";

	/**
	 * init
	 * 
	 * @param context
	 *            android context
	 * @param databaseHelper
	 *            OrmLite database helper
	 */
	public ZabbixRemoteAPI(Context context, DatabaseHelper databaseHelper,
			HttpClientWrapper httpClientMock, ZaxPreferences prefsMock) {
		ClientConnectionManager ccm = null;
		HttpParams params = null;
		if (prefsMock != null) {
			mPreferences = prefsMock;
		} else {
			mPreferences = ZaxPreferences.getInstance(context);
		}

		try {

			params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();

			if (mPreferences.isTrustAllSSLCA()) {
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);

				SSLSocketFactory sf = new CustomSSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				registry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				registry.register(new Scheme("https", sf, 443));
			} else {
				registry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
			}
			ccm = new ThreadSafeClientConnManager(params, registry);
		} catch (Exception e) {
			// ignore for unit test
		}

		// not for testing...
		if (httpClientMock == null) {
			HttpClientParams.setRedirecting(params, true); // redirecting
			HttpConnectionParams.setConnectionTimeout(params,
					ZabbixConfig.HTTP_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params,
					ZabbixConfig.HTTP_SOCKET_TIMEOUT);
			// TODO: The timeouts do not work properly (neither in the emulator
			// nor on a real device)
		}

		if (httpClientMock != null) {
			httpClient = httpClientMock;
		} else {
			if (ccm == null || params == null) {
				httpClient = new HttpClientWrapper(new DefaultHttpClient());
			} else {
				httpClient = new HttpClientWrapper(new DefaultHttpClient(ccm,
						params));
			}
		}

		// if applicable http auth
		try {
			if (mPreferences.isHttpAuthEnabled()) {
				String user = mPreferences.getHttpAuthUsername();
				String pwd = mPreferences.getHttpAuthPassword();
				httpClient.getCredentialsProvider().setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(user, pwd));
			}
		} catch (java.lang.UnsupportedOperationException e1) {
			// for unit test
		}

		this.mContext = context;
		this.databaseHelper = databaseHelper;
	}

	public Context getContext() {
		return mContext;
	}

	/**
	 * zabbix api call. data will be parsed as json object on-the-fly. Caution:
	 * use this only for api calls with a small return data.
	 * 
	 * @param method
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	private JSONObject _queryBuffer(String method, JSONObject params)
			throws IOException, JSONException, ZabbixLoginRequiredException,
			FatalException {
		buildZabbixUrl();
		validateZabbixUrl();
		HttpPost post = new HttpPost(zabbixUrl);
		post.addHeader("Content-Type", "application/json; charset=utf-8");

		String auth = "null";
		if (mPreferences.getZabbixAuthToken() != null
				&& method != "user.authenticate")
			auth = "\"" + mPreferences.getZabbixAuthToken() + "\"";

		Log.d(TAG, "queryBuffer: " + zabbixUrl);
		String json = "{" + "	\"jsonrpc\" : \"2.0\"," + "	\"method\" : \""
				+ method + "\"," + "	\"params\" : " + params.toString() + ","
				+ "	\"auth\" : " + auth + "," + "	\"id\" : 0" + "}";
		Log.d(TAG, "queryBuffer: " + json);

		post.setEntity(new StringEntity(json, "UTF-8"));
		try {
			HttpResponse resp = httpClient.execute(post);

			checkHttpStatusCode(resp);
			StringBuilder total = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(resp
					.getEntity().getContent()));
			int chr;
			while ((chr = rd.read()) != -1) {
				total.append((char) chr);
			}
			JSONObject result = new JSONObject(total.toString());
			try {
				String errorData;
				if (result.getJSONObject("error") != null) {
					errorData = result.getJSONObject("error").getString("data");
				} else {
					errorData = result.getString("data");
				}
				if (errorData.equals(ZABBIX_ERROR_NO_API_ACCESS)) {
					throw new FatalException(Type.NO_API_ACCESS);
				}
				if (errorData.matches(ZABBIX_ERROR_LOGIN_INCORRECT))
					throw new FatalException(Type.ZABBIX_LOGIN_INCORRECT);
				if (errorData.equals(ZABBIX_ERROR_NOT_AUTHORIZED)) {
					// this should lead to a retry
					throw new ZabbixLoginRequiredException();
				}
				if (errorData.matches(ZABBIX_ACCOUNT_BLOCKED)) {
					// this should lead to a retry
					throw new FatalException(Type.ACCOUNT_BLOCKED);
				}
				throw new FatalException(Type.INTERNAL_ERROR, errorData);
			} catch (JSONException e) {
				// ignore
			}
			return result;
		} catch (SocketException e) {
			throw new FatalException(Type.NO_CONNECTION, e);
		} catch (NoHttpResponseException e) {
			throw new FatalException(Type.NO_HTTP_RESPONSE, e);
		} catch (ConnectTimeoutException e) {
			throw new FatalException(Type.CONNECTION_TIMEOUT, e);
		} catch (UnknownHostException e) {
			throw new FatalException(Type.SERVER_NOT_FOUND, e);
		} catch (InterruptedIOException e) {
			// this exception is thrown when the task querying data from Zabbix
			// is cancelled. The interruption happens by design, so we can
			// ignore this.
			return null;
		}
	}

	/**
	 * zabbix api call as stream.
	 * 
	 * @param method
	 * @param params
	 * @return stream im json array wrapper
	 * @throws JSONException
	 * @throws IOException
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	private JsonArrayOrObjectReader _queryStream(String method,
			JSONObject params) throws JSONException, IOException,
			ZabbixLoginRequiredException, FatalException {

		buildZabbixUrl();
		validateZabbixUrl();
		// http request
		HttpPost post = new HttpPost(zabbixUrl);
		post.addHeader("Content-Type", "application/json; charset=utf-8");

		JSONObject json = new JSONObject().put("jsonrpc", "2.0")
				.put("method", method).put("params", params)
				.put("auth", mPreferences.getZabbixAuthToken()).put("id", 0);

		Log.d(TAG, "_queryStream: " + zabbixUrl);
		Log.d(TAG, "_queryStream: " + json.toString());

		post.setEntity(new StringEntity(json.toString(), "UTF-8"));
		try {
			HttpResponse resp = httpClient.execute(post);
			checkHttpStatusCode(resp);

			JsonFactory jsonFac = new JsonFactory();
			JsonParser jp = jsonFac.createParser(resp.getEntity().getContent());
			// store the last stream to close it if an exception will be thrown
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected data to start with an Object");
			}
			do {
				jp.nextToken();
				if (jp.getCurrentName().equals("error")) {
					jp.nextToken();
					String errortxt = "";
					while (jp.nextToken() != JsonToken.END_OBJECT) {
						errortxt += jp.getText();
					}
					if (errortxt.contains(ZABBIX_ERROR_NO_API_ACCESS)) {
						throw new FatalException(Type.NO_API_ACCESS);
					} else if (errortxt.contains(ZABBIX_ERROR_NOT_AUTHORIZED)) {
						throw new ZabbixLoginRequiredException();
					} else {
						throw new FatalException(Type.INTERNAL_ERROR,
								errortxt.toString());
					}
				}
			} while (!jp.getCurrentName().equals("result"));

			// result array found
			if (jp.nextToken() != JsonToken.START_ARRAY
					&& jp.getCurrentToken() != JsonToken.START_OBJECT) { // go
																			// inside
																			// the
																			// array
				try {
					Log.d(TAG, "current token: " + jp.getCurrentToken());
					Log.d(TAG, "current name: " + jp.getCurrentName());
					Log.d(TAG, "get text: " + jp.getText());
					Log.d(TAG, "next value: " + jp.nextValue());
					Log.d(TAG, "next token: " + jp.nextToken());
					Log.d(TAG, "current token: " + jp.getCurrentToken());
					Log.d(TAG, "current name: " + jp.getCurrentName());
					Log.d(TAG, "get text: " + jp.getText());
				} catch (Exception e) {
					throw new IOException(
							"Expected data to start with an Array");
				}
			}
			return new JsonArrayOrObjectReader(jp);
		} catch (SocketException e) {
			throw new FatalException(Type.NO_CONNECTION, e);
		} catch (ConnectTimeoutException e) {
			throw new FatalException(Type.CONNECTION_TIMEOUT, e);
		} catch (UnknownHostException e) {
			throw new FatalException(Type.SERVER_NOT_FOUND, e);
		} catch (InterruptedIOException e) {
			// this exception is thrown when the task querying data from Zabbix
			// is cancelled. The interruption happens by design, so we can
			// ignore this.
			return null;
		}
	}

	/**
	 * Checks the status code of an HTTP response and throws the appropriate
	 * exception if an error occurs.
	 * 
	 * @param resp
	 *            the HttpResponse to check
	 * @throws FatalException
	 *             if the status code indicates an error
	 */
	private void checkHttpStatusCode(HttpResponse resp) throws FatalException {
		if (resp.getStatusLine().getStatusCode() == 401) {
			// http auth failed
			throw new FatalException(Type.HTTP_AUTHORIZATION_REQUIRED);
		} else if (resp.getStatusLine().getStatusCode() == 412) {
			// Precondition failed / Looks like Zabbix 1.8.2
			throw new FatalException(Type.PRECONDITION_FAILED);
		} else if (resp.getStatusLine().getStatusCode() == 404) {
			// file not found
			throw new FatalException(Type.SERVER_NOT_FOUND, resp
					.getStatusLine().getStatusCode()
					+ " "
					+ resp.getStatusLine().getReasonPhrase());
		} else {
			Log.d(TAG, resp.getStatusLine().getStatusCode() + " "
					+ resp.getStatusLine().getReasonPhrase());
		}
	}

	/**
	 * acknowledge zabbix event. Sets ack-flag with comment. Caution: This is
	 * supported only for Zabbix version >= 1.8.4
	 * 
	 * @param eventid
	 * @param comment
	 * @return true, success.
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public boolean acknowledgeEvent(long eventid, String comment)
			throws ZabbixLoginRequiredException, FatalException {
		// for GUI unit test, just return true
		if (comment != null && comment.equals("__UNIT_TEST__RETURN_TRUE__"))
			return true;

		JSONObject result;
		try {
			result = _queryBuffer(
					"event.acknowledge",
					new JSONObject().put("eventids",
							new JSONArray().put(eventid)).put("message",
							comment));

			JSONObject resultObject = result.getJSONObject("result");
			// it can be an (empty) array
			JSONArray eventIdArray = resultObject.optJSONArray("eventids");
			if (eventIdArray != null)
				return (eventIdArray.length() == 1);
			JSONObject eventIdObject = resultObject.optJSONObject("eventids");
			if (eventIdObject != null)
				return (eventIdObject.length() == 1);
			return false;
		} catch (ClientProtocolException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}
	}

	/**
	 * zabbix auth. user and pwd from app preferences
	 * 
	 * @return true success
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public boolean authenticate() throws ZabbixLoginRequiredException,
			FatalException {
		String user = mPreferences.getUsername().trim();
		String password = mPreferences.getPassword();
		// String url = "http://10.10.0.21/zabbix";
		// String user = "admin";
		// String password = "zabbix";

		String token = null;
		try {
			JSONObject result = _queryBuffer("user.authenticate",
					new JSONObject().put("user", user)
							.put("password", password));
			token = result.getString("result");
		} catch (JSONException e) {
			// there's no result
			e.printStackTrace();
		} catch (RuntimeException e) {
			// wrong password. token remains null
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}
		if (token != null) {
			// persist token
			mPreferences.setZabbixAuthToken(token);
			// get API version
			JSONObject result;
			try {
				result = _queryBuffer("apiinfo.version", new JSONObject());
				if (result == null)
					isVersion2 = false;
				else {
					apiVersion = result.getString("result");
					isVersion2 = (apiVersion.equals("1.4") || apiVersion
							.startsWith("2"));
				}
				Log.i(TAG, "Zabbix API Version: " + apiVersion);
			} catch (ClientProtocolException e) {
				throw new FatalException(Type.INTERNAL_ERROR, e);
			} catch (IOException e) {
				throw new FatalException(Type.INTERNAL_ERROR, e);
			} catch (JSONException e) {
				throw new FatalException(Type.INTERNAL_ERROR, e);
			}
		} else {
			throw new ZabbixLoginRequiredException();
		}
		return true;
	}

	/**
	 * Imports all applications for a particular host from Zabbix. This does not
	 * include any items; they have to be imported separately.
	 * 
	 * @param hostId
	 *            host ID to filter the applications by; null: no filtering
	 * @param task
	 *            task to be notified about progress
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public void importApplicationsByHostId(Long hostId, RemoteAPITask task)
			throws FatalException, ZabbixLoginRequiredException {
		if (databaseHelper.isCached(CacheDataType.APPLICATION, hostId))
			return;

		databaseHelper.deleteApplicationsByHostId(hostId);

		JSONObject params;
		try {
			params = new JSONObject();
			int numApplications;

			params.put("output", "extend").put("countOutput", 1);
			if (hostId != null)
				params.put("hostids", new JSONArray().put(hostId));

			// count of events
			JSONObject result = _queryBuffer("application.get", params);

			// Zabbix does not support limit when countOutput is used
			numApplications = Math.min(ZabbixConfig.APPLICATION_GET_LIMIT,
					getOutputCount(result));

			params = new JSONObject();
			params.put("output", "extend")
					.put("limit", ZabbixConfig.APPLICATION_GET_LIMIT)
					.put(isVersion2 ? "selectHosts" : "select_hosts", "refer")
					.put("source", 0);
			if (!isVersion2) {
				// in Zabbix version <2.0, this is not default
				params.put("sortfield", "clock").put("sortorder", "DESC");
			}
			if (hostId != null)
				params.put("hostids", new JSONArray().put(hostId));
			JsonArrayOrObjectReader applications = _queryStream(
					"application.get", params);
			importApplicationsFromStream(applications, task, numApplications);
			// events.close();
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.APPLICATION, hostId);
	}

	/**
	 * Imports applications from a JSON stream.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @param task
	 *            task to be notified about progress
	 * @param numApplications
	 *            total number of applications that will be imported
	 * @throws JsonParseException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private Collection<Application> importApplicationsFromStream(
			JsonArrayOrObjectReader jsonReader, RemoteAPITask task,
			int numApplications) throws JsonParseException,
			NumberFormatException, IOException {
		List<Application> applicationsComplete = new ArrayList<Application>();
		List<Application> applicationsPerBatch = new ArrayList<Application>(
				RECORDS_PER_INSERT_BATCH);
		JsonObjectReader application;
		int i = 0;
		while ((application = jsonReader.next()) != null) {
			Application app = new Application();
			while (application.nextValueToken()) {
				String propName = application.getCurrentName();
				if (propName.equals(Application.COLUMN_APPLICATIONID)) {
					app.setId(Long.parseLong(application.getText()));
				} else if (propName.equals(Application.COLUMN_NAME)) {
					app.setName(application.getText());
				} else if (propName.equals(Application.COLUMN_HOSTID)) {
					// Attention: there is an inconsistency in Zabbix 1.8: Even
					// though the select_hosts parameter is set, application.get
					// does not return hosts. The hostid of the application
					// object is, however, set. So we use this id to retrieve
					// the host for an application from the database.
					Host h = databaseHelper.getHostById(Long
							.parseLong(application.getText()));
					if (h != null)
						app.setHost(h);
					// app.set(ApplicationData.COLUMN_HOSTID,
					// Long.parseLong(application.getText()));
				} else if (propName.equals("hosts")) {
					// import hosts
					List<Host> hosts = importHostsFromStream(
							application.getJsonArrayOrObjectReader(), false);
					databaseHelper.refreshHosts(hosts);
					if (hosts.size() > 0) {
						Host h = hosts.get(0);
						if (h != null) {
							app.setHost(h);
						}
					}
					if (hosts.size() > 1) {
						Log.w(TAG, "More than one host found for application "
								+ app.getId() + ": " + app.getName());
					}
				} else {
					application.nextProperty();
				}
			}

			applicationsPerBatch.add(app);
			applicationsComplete.add(app);

			if (applicationsPerBatch.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertApplications(applicationsPerBatch);
				applicationsPerBatch.clear();
			}

			i++;

			task.updateProgress(((i * 20) / numApplications));

		}
		// insert the last batch of applications
		databaseHelper.insertApplications(applicationsPerBatch);
		return applicationsComplete;
	}

	/**
	 * Imports applications from a JSON stream containing an array of IDs. The
	 * applications are queried from the database.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @throws JsonParseException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private Collection<Application> importApplicationsFromIdStream(
			JsonArrayOrObjectReader jsonReader) throws JsonParseException,
			NumberFormatException, IOException {
		List<Application> applications = new ArrayList<Application>();
		JsonObjectReader application;
		HashSet<Long> appIds = new HashSet<Long>();
		while ((application = jsonReader.next()) != null) {
			while (application.nextValueToken()) {
				String propName = application.getCurrentName();
				if (propName.equals(Application.COLUMN_APPLICATIONID)) {
					long appId = Long.parseLong(application.getText());
					if (!appIds.contains(appId)) {
						Application app = databaseHelper
								.getApplicationById(appId);
						if (app != null)
							applications.add(app);
					} else
						Log.d(TAG, "app " + appId + " already imported.");
				} else {
					application.nextProperty();
				}
			}

		}
		return applications;
	}

	/**
	 * Import the newest events if they are not cached in the local database
	 * already.
	 * 
	 * @param task
	 *            task to notify about progress
	 * 
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public void importEvents(RemoteAPITask task)
			throws ZabbixLoginRequiredException, FatalException {

		if (databaseHelper.isCached(CacheDataType.EVENT, null)) {
			Log.d(TAG, "Events do not need to be refreshed.");
			return;
		}

		try {
			databaseHelper.clearEvents();

			int numEvents;

			// count of events
			JSONObject result = _queryBuffer(
					"event.get",
					new JSONObject().put("countOutput", 1).put(
							"time_from",
							(new Date().getTime() / 1000)
									- ZabbixConfig.EVENT_GET_TIME_FROM_SHIFT));

			// Zabbix does not support limit when countOutput is used
			numEvents = Math.min(ZabbixConfig.EVENTS_GET_LIMIT,
					getOutputCount(result));

			JSONObject params;
			params = new JSONObject()
					.put("output", "extend")
					.put("limit", ZabbixConfig.EVENTS_GET_LIMIT)
					.put(isVersion2 ? "selectHosts" : "select_hosts", "refer")
					.put(isVersion2 ? "selectTriggers" : "select_triggers",
							"extend")
					.put("source", 0)
					// sorting by clock is not possible, hence we sort by event
					// ID to get the newest events
					.put("sortfield", "eventid")
					.put("sortorder", "DESC")
					.put("time_from",
							(new Date().getTime() / 1000)
									- ZabbixConfig.EVENT_GET_TIME_FROM_SHIFT);

			if (!isVersion2) {
				// in Zabbix version <2.0, this is not default
				params.put("sortfield", "clock").put("sortorder", "DESC");
			}
			JsonArrayOrObjectReader events = _queryStream("event.get", params);
			importEventsFromStream(events, task, numEvents);
			// events.close();
			databaseHelper.setCached(CacheDataType.EVENT, null);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

	}

	/**
	 * Returns the number of objects returned by an API call using countOutput.
	 * Usually, the numer is returned directly, but in Zabbix 1.8, the parameter
	 * "rowscount" may be used (e.g. event.get).
	 * 
	 * @param result
	 *            result of the API call
	 * @return number of objects
	 */
	private int getOutputCount(JSONObject result) {
		try {
			return result.getInt("result");
		} catch (JSONException e) {
			try {
				try {
					return ((JSONObject) result.get("result"))
							.getInt("rowscount");
				} catch (ClassCastException e2) {
					return ((JSONArray) result.get("result")).length();
				}
			} catch (JSONException e1) {
				return Integer.MAX_VALUE;
			}
		}
	}

	/**
	 * Imports events from a JSON stream.
	 * 
	 * This method calls
	 * {@link ZabbixRemoteAPI#importTriggersByIds(Collection, boolean)} to
	 * import the triggers referred to by the events. The usage of
	 * triggers.extend in the event.get method does not suffice because we need
	 * the host groups associated with the triggers for the hostgroup filter.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @param task
	 *            task to be notified about progress; can be null
	 * @param numEvents
	 *            total number of events that will be imported
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 * @throws JSONException
	 */
	private void importEventsFromStream(JsonArrayOrObjectReader jsonReader,
			RemoteAPITask task, int numEvents) throws JsonParseException,
			IOException, JSONException, ZabbixLoginRequiredException,
			FatalException {
		JsonObjectReader eventReader;
		List<Event> eventsCollection = new ArrayList<Event>(
				RECORDS_PER_INSERT_BATCH);
		HashSet<Long> triggerIds = new HashSet<Long>();
		int i = 0;
		while (jsonReader != null
				&& ((eventReader = jsonReader.next()) != null)) {
			i++;
			Event e = new Event();
			while (eventReader.nextValueToken()) {
				String propName = eventReader.getCurrentName();
				if (propName.equals("hosts")) {
					// import hosts
					List<Host> hosts = importHostsFromStream(
							eventReader.getJsonArrayOrObjectReader(), false);
					databaseHelper.refreshHosts(hosts);
					String hostNames = createHostNamesString(hosts);
					// store hosts names
					e.setHostNames(hostNames);
				} else if (propName.equals("triggers")) {
					// import triggers
					List<Trigger> triggers = importTriggersFromStream(
							eventReader.getJsonArrayOrObjectReader(), 0, null);
					if (triggers.size() > 0) {
						Trigger t = triggers.get(0);
						e.setTrigger(t);
						triggerIds.add(t.getId());
					}
					if (triggers.size() > 1) {
						Log.w(TAG,
								"More than one trigger found for event "
										+ e.getDetailedString());
					}
				} else if (propName.equals(Event.COLUMN_ID)) {
					e.setId(Long.parseLong(eventReader.getText()));
				} else if (propName.equals(Event.COLUMN_CLOCK)) {
					// The unit of Zabbix timestamps is seconds, we need
					// milliseconds
					e.setClock(Long.parseLong(eventReader.getText()) * 1000);
				} else if (propName.equals(Event.COLUMN_OBJECT_ID)) {
					e.setObjectId(Long.parseLong(eventReader.getText()));
				} else if (propName.equals(Event.COLUMN_ACK)) {
					e.setAcknowledged(Integer.parseInt(eventReader.getText()) == 1);
				} else if (propName.equals(Event.COLUMN_VALUE)) {
					e.setValue(Integer.parseInt(eventReader.getText()));
				} else {
					eventReader.nextProperty();
				}
				Trigger t = e.getTrigger();
				if (t != null
						&& (t.getHostNames() == null || (t.getHostNames()
								.length() == 0)))
					t.setHostNames(e.getHostNames());
			}
			eventsCollection.add(e);
			if (eventsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertEvents(eventsCollection);
				eventsCollection.clear();
			}
			if (task != null)
				task.updateProgress((i * 100) / numEvents);
		}
		// insert the last batch of events
		databaseHelper.insertEvents(eventsCollection);
		// we need to close here to be able to start another import (triggers)
		jsonReader.close();
		importTriggersByIds(triggerIds, false, null);
	}

	/**
	 * Imports history details for an item.
	 * 
	 * @param itemId
	 * @param task
	 *            task to be notified about the progress; may be null if no task
	 *            shall be notified
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importHistoryDetails(long itemId, RemoteAPITask task)
			throws ZabbixLoginRequiredException, FatalException {
		if (databaseHelper.isCached(CacheDataType.HISTORY_DETAILS, itemId))
			return;

		// delete old history items - as cached history items are still valid,
		// we keep history details which are within the time range in the local
		// database and fetch only newer objects (history doesn't change...)
		long timeTill = new Date().getTime() / 1000;
		long timeFrom = timeTill - ZabbixConfig.HISTORY_GET_TIME_FROM_SHIFT;
		databaseHelper.deleteOldHistoryDetailsByItemId(itemId,
				System.currentTimeMillis()
						- (ZabbixConfig.HISTORY_GET_TIME_FROM_SHIFT * 1000));
		long timeNewest = databaseHelper
				.getNewestHistoryDetailsClockByItemId(itemId) / 1000;
		if (timeNewest > timeFrom) {
			Log.d(TAG, "timeNewest: " + timeNewest + " - timeFrom: " + timeFrom);
			timeFrom = timeNewest;
		}

		try {

			// Workaround: historydetails only comes if you use the correct
			// "history"-parameter. This parameter can be "null" or a number
			// 0-4.
			// Because we don't know when to use which, we try them all, until
			// we get results.
			Integer historytype = null;
			JSONObject result = _queryBuffer(
					"history.get",
					new JSONObject().put("limit", 1)
							.put("history", historytype)
							// for integer ?
							.put("itemids", new JSONArray().put(itemId))
							.put("time_from", timeFrom));

			JSONArray testHistorydetails = result.getJSONArray("result");
			if (testHistorydetails.length() == 0) {
				historytype = -1;
				while (testHistorydetails.length() == 0 && ++historytype <= 4) {
					// if we get an empty array, we try another history
					// parameter
					result = _queryBuffer(
							"history.get",
							new JSONObject()
									.put("output", "extend")
									.put("limit", 1)
									.put("history", historytype)
									.put("itemids", new JSONArray().put(itemId))
									.put("time_from", timeFrom));
					if (result == null)
						continue;
					testHistorydetails = result.getJSONArray("result");
				}
			}
			// correct historytype found and there is data
			if (testHistorydetails != null && testHistorydetails.length() > 0) {
				// count of the entries cannot be detected (zabbix bug),
				// so we use a fiction
				int numDetails = 400;
				JsonArrayOrObjectReader historydetails = _queryStream(
						"history.get",
						new JSONObject().put("output", "extend")
								.put("limit", ZabbixConfig.HISTORY_GET_LIMIT)
								.put("history", historytype)
								.put("itemids", new JSONArray().put(itemId))
								.put("time_from", timeFrom)
								.put("sortfield", "clock")
								.put("sortorder", "DESC"));

				JsonObjectReader historydetail;
				List<HistoryDetail> historyDetailsCollection = new ArrayList<HistoryDetail>(
						RECORDS_PER_INSERT_BATCH);
				try {
					int selI = 0;
					while (historydetails != null
							&& (historydetail = historydetails.next()) != null) {
						// save only every 20th
						// TODO: This may produce odd graphs for a small amount
						// of values
						if (selI++ % 20 != 0) {
							while (historydetail.nextValueToken()) {
								historydetail.nextProperty();
							}
							continue;
						}

						HistoryDetail h = new HistoryDetail();
						while (historydetail.nextValueToken()) {
							String propName = historydetail.getCurrentName();
							if (propName.equals(HistoryDetail.COLUMN_CLOCK)) {
								// The unit of Zabbix timestamps is seconds, we
								// need milliseconds
								h.setClock(Long.parseLong(historydetail
										.getText()) * 1000);
							} else if (propName
									.equals(HistoryDetail.COLUMN_ITEMID)) {
								h.setItemId(Long.parseLong(historydetail
										.getText()));
							} else if (propName
									.equals(HistoryDetail.COLUMN_VALUE)) {
								h.setValue(Double.parseDouble(historydetail
										.getText()));
							} else {
								historydetail.nextProperty();
							}
						}
						historyDetailsCollection.add(h);
						if (historyDetailsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
							databaseHelper
									.insertHistoryDetails(historyDetailsCollection);
							historyDetailsCollection.clear();
						}
						if (task != null)
							task.updateProgress(Math.min(selI * 100
									/ numDetails, 84));
					}
					Log.d(TAG, "itemID " + itemId + ": imported " + (selI / 20)
							+ " history details.");
				} catch (NumberFormatException e) {
					// data are unuseable, e.g. because it's a string
				}
				// insert the last batch of events
				databaseHelper.insertHistoryDetails(historyDetailsCollection);
				historydetails.close();
			}
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}
		databaseHelper.setCached(CacheDataType.HISTORY_DETAILS, itemId);
	}

	/**
	 * Imports host groups from a JSON stream.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @return list of host groups parsed from jsonReader
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private List<HostGroup> importHostGroupsFromStream(
			JsonArrayOrObjectReader jsonReader) throws JsonParseException,
			IOException {
		long firstHostGroupId = -1;
		ArrayList<HostGroup> hostGroupCollection = new ArrayList<HostGroup>();
		JsonObjectReader hostReader;
		while ((hostReader = jsonReader.next()) != null) {
			HostGroup h = new HostGroup();
			while (hostReader.nextValueToken()) {
				String propName = hostReader.getCurrentName();
				if (propName.equals(HostGroup.COLUMN_GROUPID)) {
					long id = Long.parseLong(hostReader.getText());
					if (firstHostGroupId == -1)
						firstHostGroupId = id;
					h.setGroupId(id);
				} else if (propName.equals(HostGroup.COLUMN_NAME)) {
					h.setName(hostReader.getText());
				} else {
					hostReader.nextProperty();
				}
			}
			hostGroupCollection.add(h);
			if (hostGroupCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertHostGroups(hostGroupCollection);
				hostGroupCollection.clear();
			}
		}
		databaseHelper.insertHostGroups(hostGroupCollection);

		return hostGroupCollection;
		// return firstHostGroupId;
	}

	/**
	 * Imports hosts from a JSON stream and returns a string of comma-separated
	 * host names.
	 * 
	 * This method also fills the host to host group relation if a host's groups
	 * have been selected.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @param insertHosts
	 *            whether or not the imported hosts shall be inserted into the
	 *            database (this should be false if the hosts are imported using
	 *            "refer" as their information is not complete)
	 * @return list of hosts retrieved from the stream
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private List<Host> importHostsFromStream(
			JsonArrayOrObjectReader jsonReader, boolean insertHosts)
			throws JsonParseException, IOException {

		List<Host> hostCollection = new ArrayList<Host>();
		List<HostHostGroupRelation> hostHostGroupCollection = new ArrayList<HostHostGroupRelation>();
		JsonObjectReader hostReader;
		while ((hostReader = jsonReader.next()) != null) {
			Host h = new Host();
			while (hostReader.nextValueToken()) {
				String propName = hostReader.getCurrentName();
				if (propName.equals(Host.COLUMN_ID)) {
					h.setId(Long.parseLong(hostReader.getText()));
					// if (firstHostId == -1) {
					// firstHostId = (Long) h.get(HostData.COLUMN_HOSTID);
					// }
				} else if (propName.equals(Host.COLUMN_HOST)) {
					String host = hostReader.getText();
					h.setName(host);
				} else if (propName.equals(Host.COLUMN_STATUS)) {
					h.setStatus(Integer.parseInt(hostReader.getText()));
				} else if (propName.equals("groups")) {
					List<HostGroup> groups = importHostGroupsFromStream(hostReader
							.getJsonArrayOrObjectReader());
					for (HostGroup group : groups) {
						// create HostHostGroupRelation
						hostHostGroupCollection.add(new HostHostGroupRelation(
								h, group));
					}
				} else {
					hostReader.nextProperty();
				}
			}
			hostCollection.add(h);
			if (insertHosts
					&& hostCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertHosts(hostCollection);
				hostCollection.clear();
			}
		}
		// insert the last batch of events
		if (insertHosts) {
			databaseHelper.insertHosts(hostCollection);
			Log.d(TAG, "hosts inserted.");
		}

		if (insertHosts && !hostHostGroupCollection.isEmpty())
			databaseHelper
					.insertHostHostgroupRelations(hostHostGroupCollection);

		return hostCollection;
	}

	/**
	 * Imports all hosts and their host groups. This also fills the host to host
	 * group relation table ({@link HostHostGroupRelation}.
	 * 
	 * ATTENTION: empty host groups are not imported!
	 * 
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importHostsAndGroups() throws ZabbixLoginRequiredException,
			FatalException {
		if (databaseHelper.isCached(CacheDataType.HOST, null)
				&& databaseHelper.isCached(CacheDataType.HOSTGROUP, null))
			return;
		try {
			// hosts in the local database may not be empty; hence we prevent
			// multiple database operations on the hosts table (as soon as
			// caching is implemented, the performance impact will be 0)
			synchronized (databaseHelper.getDao(Host.class)) {
				databaseHelper.clearHosts();
				databaseHelper.clearHostGroups();
				JsonArrayOrObjectReader hosts = _queryStream(
						"host.get",
						new JSONObject()
								.put("output", "extend")
								.put("limit", ZabbixConfig.HOST_GET_LIMIT)
								.put(isVersion2 ? "selectGroups"
										: "select_groups", "extend"));
				importHostsFromStream(hosts, true);
				hosts.close();
			}

		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.HOST, null);
		databaseHelper.setCached(CacheDataType.HOSTGROUP, null);

	}

	/**
	 * import items from stream.
	 * 
	 * @param jsonReader
	 *            stream
	 * @param task
	 *            task to be notified about progress
	 * @param numItems
	 *            count for progressbar, if 0 no progressbarupdate
	 * @param checkBeforeInsert
	 *            if true, only make an insert if item does not exist
	 * @return the first item id
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private List<Item> importItemsFromStream(
			JsonArrayOrObjectReader jsonReader, RemoteAPITask task,
			int numItems, boolean checkBeforeInsert) throws JsonParseException,
			IOException {
		JsonObjectReader itemReader;
		List<Item> itemsComplete = new ArrayList<Item>();
		List<Item> itemsPerBatch = new ArrayList<Item>(RECORDS_PER_INSERT_BATCH);
		List<ApplicationItemRelation> applicationItemRelations = new ArrayList<ApplicationItemRelation>(
				RECORDS_PER_INSERT_BATCH);
		int i = 0;
		while ((itemReader = jsonReader.next()) != null) {
			Item item = new Item();
			Collection<Application> applications = new ArrayList<Application>();
			String key_ = null;
			while (itemReader.nextValueToken()) {
				String propName = itemReader.getCurrentName();
				if (propName.equals(Item.COLUMN_ITEMID)) {
					item.setId(Long.parseLong(itemReader.getText()));
					// if (firstItemId == -1) {
					// firstItemId = (Long) i.get(Item.COLUMN_ITEMID);
					// }
				} else if (propName.equals(Item.COLUMN_HOSTID)) {
					Host h = databaseHelper.getHostById(Long
							.parseLong(itemReader.getText()));
					if (h != null)
						item.setHost(h);
				} else if (propName.equals(Item.COLUMN_DESCRIPTION)
						|| propName.equals(Item.COLUMN_DESCRIPTION_V2)) {
					// since zabbix 2.x is the name of the item "name"
					// before zabbix 2.x the name field was "description"
					if (isVersion2
							&& propName.equals(Item.COLUMN_DESCRIPTION_V2)) {
						item.setDescription(itemReader.getText());
					} else if (!isVersion2) {
						item.setDescription(itemReader.getText());
					}
				} else if (propName.equals(Item.COLUMN_LASTCLOCK)) {
					if (itemReader.getText() != null)
						item.setLastClock(Long.parseLong(itemReader.getText()) * 1000);
				} else if (propName.equals(Item.COLUMN_LASTVALUE)) {
					item.setLastValue(itemReader.getText());
				} else if (propName.equals(Item.COLUMN_UNITS)) {
					item.setUnits(itemReader.getText());
				} else if (propName.equals(Item.COLUMN_STATUS)) {
					item.setStatus(Integer.parseInt(itemReader.getText()));
				} else if (propName.equals("key_")) {
					key_ = itemReader.getText();
				} else if (propName.equals("applications")) {
					applications = importApplicationsFromIdStream(itemReader
							.getJsonArrayOrObjectReader());
				} else {
					itemReader.nextProperty();
				}
			}
			// if applicable replace placeholder
			String description = item.getDescription();
			if (description.matches(".*\\$[0-9].*")) {
				if (key_ != null && key_.indexOf('[') != -1) {
					String[] keys = key_.substring(key_.indexOf('[') + 1,
							key_.indexOf(']')).split(",");
					for (int ix = 0; ix < keys.length; ix++) {
						description = description.replace("$" + (ix + 1),
								keys[ix]);
					}
				}
			}
			item.setDescription(description);

			itemsPerBatch.add(item);
			itemsComplete.add(item);
			if (itemsPerBatch.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertItems(itemsPerBatch);
				itemsPerBatch.clear();
			}

			if (item.getHost() != null
					&& (applications == null || applications.isEmpty())) {
				// If no application has been found for this particular event,
				// we create an "other" application
				long otherId = item.getHost().getId() * (-1);
				Application other = databaseHelper.getApplicationById(otherId);
				if (other == null) {
					other = new Application();
					other.setHost(item.getHost());
					other.setId(otherId);
					other.setName(mContext.getResources().getString(
							R.string.other));
					ArrayList<Application> apps = new ArrayList<Application>();
					apps.add(other);
					databaseHelper.insertApplications(apps);
				}
				applications.add(other);
			}
			// insert application item relations
			for (Application app : applications) {
				app.setHost(item.getHost());
				applicationItemRelations.add(new ApplicationItemRelation(app,
						item.getHost(), item));
			}
			if (applicationItemRelations.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper
						.insertApplicationItemRelations(applicationItemRelations);
				applicationItemRelations.clear();
			}
			i++;
			if (task != null)
				task.updateProgress(20 + ((i * 80) / numItems));
		}
		// insert the last batch of events
		databaseHelper.insertItems(itemsPerBatch);
		databaseHelper.insertApplicationItemRelations(applicationItemRelations);
		return itemsComplete;
	}

	/**
	 * Imports all items for a particular host from Zabbix.
	 * 
	 * @param hostId
	 *            host ID to filter the applications by; null: no filtering
	 * @param task
	 *            task to be notified about progress
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public void importItemsByHostId(Long hostId, RemoteAPITask task)
			throws FatalException, ZabbixLoginRequiredException {
		if (databaseHelper.isCached(CacheDataType.ITEM, hostId))
			return;

		databaseHelper.deleteItemsByHostId(hostId);

		try {
			// count of items
			JSONObject params = new JSONObject();
			params.put("output", "extend").put("countOutput", 1);
			if (hostId != null)
				params.put("hostids", new JSONArray().put(hostId));

			JSONObject result = _queryBuffer("item.get", params);
			// Zabbix does not support limit when countOutput is set
			int numItems = Math.min(ZabbixConfig.ITEM_GET_LIMIT,
					getOutputCount(result));

			params = new JSONObject();
			params.put("output", "extend")
					.put("limit", ZabbixConfig.ITEM_GET_LIMIT)
					.put(isVersion2 ? "selectApplications"
							: "select_applications", "refer")
					.put(isVersion2 ? "selectHosts" : "select_hosts", "extend");
			if (hostId != null)
				params.put("hostids", new JSONArray().put(hostId));
			JsonArrayOrObjectReader items = _queryStream("item.get", params);
			importItemsFromStream(items, task, numItems, false);
			items.close();

			// Log.d(TAG, _queryBuffer("item.get", params).toString());

		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.ITEM, hostId);

	}

	private void importScreenItemsFromStream(JsonArrayOrObjectReader jsonReader)
			throws JsonParseException, NumberFormatException, IOException {
		JsonObjectReader screenItemReader;

		ArrayList<ScreenItem> screenItemsCollection = new ArrayList<ScreenItem>(
				RECORDS_PER_INSERT_BATCH);
		while ((screenItemReader = jsonReader.next()) != null) {
			ScreenItem screenItem = new ScreenItem();
			int resourcetype = -1;
			while (screenItemReader.nextValueToken()) {
				String propName = screenItemReader.getCurrentName();
				if (propName.equals(ScreenItem.COLUMN_SCREENITEMID)) {
					screenItem
							.setId(Long.parseLong(screenItemReader.getText()));
				} else if (propName.equals(ScreenItem.COLUMN_SCREENID)) {
					screenItem.setScreenId(Long.parseLong(screenItemReader
							.getText()));
				} else if (propName.equals(ScreenItem.COLUMN_RESOURCEID)) {
					screenItem.setResourceId(Long.parseLong(screenItemReader
							.getText()));
				} else if (propName.equals("resourcetype")) {
					resourcetype = Integer.parseInt(screenItemReader.getText());
				} else {
					screenItemReader.nextProperty();
				}
			}
			// only resouretype == 0
			if (resourcetype == 0) {
				screenItemsCollection.add(screenItem);
			}
			if (screenItemsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertScreenItems(screenItemsCollection);
				screenItemsCollection.clear();
			}
		}

		databaseHelper.insertScreenItems(screenItemsCollection);
	}

	/**
	 * Imports screens from Zabbix.
	 * 
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importScreens() throws ZabbixLoginRequiredException,
			FatalException {
		if (databaseHelper.isCached(CacheDataType.SCREEN, null))
			return;

		JsonArrayOrObjectReader jsonReader;
		try {
			databaseHelper.clearScreens();
			JSONObject params = new JSONObject();
			params.put("output", "extend");
			params.put(isVersion2 ? "selectScreenItems" : "select_screenitems",
					"extend");
			jsonReader = _queryStream((isVersion2 ? "s" : "S") + "creen.get",
					params);

			JsonObjectReader screenReader;
			ArrayList<Screen> screensCollection = new ArrayList<Screen>(
					RECORDS_PER_INSERT_BATCH);
			ArrayList<Screen> screensComplete = new ArrayList<Screen>();
			while ((screenReader = jsonReader.next()) != null) {
				Screen screen = new Screen();
				while (screenReader.nextValueToken()) {
					String propName = screenReader.getCurrentName();
					if (propName.equals(Screen.COLUMN_SCREENID)) {
						screen.setId(Long.parseLong(screenReader.getText()));
					} else if (propName.equals(Screen.COLUMN_NAME)) {
						screen.setName(screenReader.getText());
					} else if (propName.equals("screenitems")) {
						importScreenItemsFromStream(screenReader
								.getJsonArrayOrObjectReader());
					} else {
						screenReader.nextProperty();
					}
				}
				screensCollection.add(screen);
				screensComplete.add(screen);
				if (screensCollection.size() >= RECORDS_PER_INSERT_BATCH) {
					databaseHelper.insertScreens(screensCollection);
					screensCollection.clear();
				}
			}
			databaseHelper.insertScreens(screensCollection);
			jsonReader.close();

		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.SCREEN, null);
	}

	/**
	 * import graph items
	 * 
	 * @param graphItems
	 * @return true, if graphid column has to be updated from -1 to the correct
	 *         graphid later.
	 * @throws JsonParseException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private Collection<GraphItem> importGraphItemsFromStream(
			JsonArrayOrObjectReader graphItems) throws JsonParseException,
			NumberFormatException, IOException {
		ArrayList<GraphItem> graphItemsCollection = new ArrayList<GraphItem>();
		JsonObjectReader graphItemReader;

		while ((graphItemReader = graphItems.next()) != null) {
			GraphItem gi = new GraphItem();
			while (graphItemReader.nextValueToken()) {
				String propName = graphItemReader.getCurrentName();
				if (propName.equals("gitemid")) {
					gi.setId(Long.parseLong(graphItemReader.getText()));
					// } else if (propName.equals(GraphItem.COLUMN_GRAPHID)) {
					// gi.setGraphId(Long.parseLong(graphItemReader.getText()));
				} else if (propName.equals(GraphItem.COLUMN_ITEMID)) {
					gi.setItemId(Long.parseLong(graphItemReader.getText()));
				} else if (propName.equals(GraphItem.COLUMN_COLOR)) {
					// hex string => color int
					gi.setColor(Color.parseColor("#"
							+ graphItemReader.getText()));
				} else {
					graphItemReader.nextProperty();
				}
			}
			graphItemsCollection.add(gi);
		}
		return graphItemsCollection;
	}

	/**
	 * Imports all graphs for a particular screen from Zabbix. This includes
	 * graph items and the corresponding items.
	 * 
	 * @param screen
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importGraphsByScreen(Screen screen)
			throws ZabbixLoginRequiredException, FatalException {
		if (databaseHelper.isCached(CacheDataType.GRAPH, screen.getId()))
			return;

		// import screens just to be sure (if screens have already been
		// imported, this will do nothing
		importScreens();

		JsonArrayOrObjectReader graphs;
		try {

			// collect all graphids
			Set<Long> graphIds = databaseHelper.getGraphIdsByScreen(screen);

			databaseHelper.deleteGraphsByIds(graphIds);
			graphs = _queryStream(
					"graph.get",
					new JSONObject()
							.put(isVersion2 ? "selectGraphItems"
									: "select_graph_items", "extend")
							.put(isVersion2 ? "selectItems" : "select_items",
									"extend").put("output", "extend")
							.put("graphids", new JSONArray(graphIds)));

			JsonObjectReader graphReader;
			ArrayList<Graph> graphsCollection = new ArrayList<Graph>(
					RECORDS_PER_INSERT_BATCH);
			ArrayList<GraphItem> graphItemsCollection = new ArrayList<GraphItem>();
			Map<Long, Item> itemsMap = new HashMap<Long, Item>();

			while ((graphReader = graphs.next()) != null) {
				Graph graph = new Graph();
				while (graphReader.nextValueToken()) {
					String propName = graphReader.getCurrentName();
					if (propName.equals(Graph.COLUMN_GRAPHID)) {
						graph.setId(Long.parseLong(graphReader.getText()));
					} else if (propName.equals(Graph.COLUMN_NAME)) {
						graph.setName(graphReader.getText());
					} else if (propName.equals("gitems")) {
						graphItemsCollection
								.addAll(importGraphItemsFromStream(graphReader
										.getJsonArrayOrObjectReader()));
					} else if (propName.equals("items")) {
						Collection<Item> items = importItemsFromStream(
								graphReader.getJsonArrayOrObjectReader(), null,
								0, true);
						for (Item i : items) {
							itemsMap.put(i.getId(), i);
						}
					} else {
						graphReader.nextProperty();
					}
				}

				// the graph id is usually not included in the graph item
				// import, hence we set the graph manually
				for (GraphItem graphItem : graphItemsCollection) {
					graphItem.setGraph(graph);
					graphItem.setItem(itemsMap.get(graphItem.getItemId()));
				}

				databaseHelper.insertGraphItems(graphItemsCollection);
				graphItemsCollection.clear();

				graphsCollection.add(graph);
				if (graphsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
					databaseHelper.insertGraphs(graphsCollection);
					graphsCollection.clear();
				}
			}
			databaseHelper.insertGraphs(graphsCollection);
			graphs.close();

		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.GRAPH, screen.getId());

	}

	/**
	 * Imports active triggers.
	 * 
	 * @param task
	 *            task to be notified of progress
	 * 
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importActiveTriggers(RemoteAPITask task)
			throws ZabbixLoginRequiredException, FatalException {
		if (databaseHelper.isCached(CacheDataType.TRIGGER, null))
			return;
		importTriggersByIds(null, true, task);
		databaseHelper.setCached(CacheDataType.TRIGGER, null);
	}

	/**
	 * Imports the triggers with matching IDs.
	 * 
	 * We do not need to take care of caching here, because this method is
	 * called in two situations:
	 * 
	 * 1. Import of events. If events (and the corresponding triggers) are
	 * cached, the method {@link ZabbixRemoteAPI#importEvents()} does not call
	 * this method at all. Hence the caching of the corresponding triggers is
	 * directly linked to caching of the events (just like triggers are linked
	 * to events in the data model).
	 * 
	 * 2. Import of all active triggers. The method
	 * {@link ZabbixRemoteAPI#importActiveTriggers()} takes care of caching by
	 * itself.
	 * 
	 * @param triggerIds
	 *            collection of trigger ids to be matched; null: import all
	 *            triggers
	 * @param onlyActive
	 *            true: import only active triggers; false: import all
	 * @param task
	 *            task to be notified of progress update
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	private void importTriggersByIds(Collection<Long> triggerIds,
			boolean onlyActive, RemoteAPITask task)
			throws ZabbixLoginRequiredException, FatalException {

		// TODO: clear triggers if necessary
		// databaseHelper.clearTriggers();

		try {

			long min = (new Date().getTime() / 1000)
					- ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;

			int numTriggers;

			JSONObject params = new JSONObject();
			params.put("output", "extend").put("countOutput", 1)
					.put("lastChangeSince", min).put("expandDescription", true);
			if (triggerIds != null) {
				params.put("triggerids", new JSONArray(triggerIds));
			}

			// count of events
			JSONObject result = _queryBuffer("trigger.get", params);

			// The Zabbix API does not support limit when countOutput is used
			numTriggers = Math.min(ZabbixConfig.TRIGGER_GET_LIMIT,
					getOutputCount(result));

			params = new JSONObject();
			params.put("output", "extend")
					.put("sortfield", "lastchange")
					.put("sortorder", "desc")
					.put(isVersion2 ? "selectHosts" : "select_hosts", "refer")
					.put(isVersion2 ? "selectGroups" : "select_groups",
							"extend")
					.put(isVersion2 ? "selectItems" : "select_items", "extend")
					.put("lastChangeSince", min)
					.put("limit", ZabbixConfig.TRIGGER_GET_LIMIT)
					.put("expandDescription", true);

			if (triggerIds != null) {
				params.put("triggerids", new JSONArray(triggerIds));
			}
			if (onlyActive) {
				params.put("only_true", "1");
				params.put("monitored", "1");
			}
			JsonArrayOrObjectReader triggers = _queryStream("trigger.get",
					params);
			importTriggersFromStream(triggers, numTriggers, task);
			triggers.close();
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

	}

	/**
	 * Imports triggers from a JSON stream.
	 * 
	 * If groups are selected, the trigger to hostgroup relation (
	 * {@link TriggerHostGroupRelation} is filled.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @param numTriggers
	 *            total number of triggers that will be imported
	 * @param task
	 *            task to be notified of progress
	 * @return list of imported triggers
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private List<Trigger> importTriggersFromStream(
			JsonArrayOrObjectReader jsonReader, int numTriggers,
			RemoteAPITask task) throws JsonParseException, IOException {
		List<Trigger> triggerCollection = new ArrayList<Trigger>(
				RECORDS_PER_INSERT_BATCH);
		List<TriggerHostGroupRelation> triggerHostGroupCollection = new ArrayList<TriggerHostGroupRelation>(
				RECORDS_PER_INSERT_BATCH);
		JsonObjectReader triggerReader;
		int i = 0;
		while ((triggerReader = jsonReader.next()) != null) {
			Trigger t = new Trigger();
			boolean enabled = true;
			while (triggerReader.nextValueToken()) {
				String propName = triggerReader.getCurrentName();
				if (propName == null)
					continue;
				if (propName.equals(Trigger.COLUMN_TRIGGERID)) {
					t.setId(Long.parseLong(triggerReader.getText()));
				} else if (propName.equals(Trigger.COLUMN_COMMENTS)) {
					t.setComments(triggerReader.getText());
				} else if (propName.equals(Trigger.COLUMN_DESCRIPTION)) {
					t.setDescription(triggerReader.getText());
				} else if (propName.equals(Trigger.COLUMN_EXPRESSION)) {
					t.setExpression(triggerReader.getText());
				} else if (propName.equals(Trigger.COLUMN_LASTCHANGE)) {
					// The unit of Zabbix timestamps is seconds, we need
					// milliseconds
					t.setLastChange(Long.parseLong(triggerReader.getText()) * 1000);
				} else if (propName.equals(Trigger.COLUMN_PRIORITY)) {
					t.setPriority(TriggerSeverity.getSeverityByNumber(Integer
							.parseInt(triggerReader.getText())));
				} else if (propName.equals(Trigger.COLUMN_STATUS)) {
					t.setStatus(Integer.parseInt(triggerReader.getText()));
				} else if (propName.equals(Trigger.COLUMN_VALUE)) {
					t.setValue(Integer.parseInt(triggerReader.getText()));
				} else if (propName.equals(Trigger.COLUMN_URL)) {
					t.setUrl(triggerReader.getText());
				} else if (propName.equals("hosts")) {
					// import hosts
					List<Host> hosts = importHostsFromStream(
							triggerReader.getJsonArrayOrObjectReader(), false);
					databaseHelper.refreshHosts(hosts);
					for (Host host : hosts) {
						if (host.getStatus() != Host.STATUS_MONITORED)
							enabled = false;
					}
					String hostNames = createHostNamesString(hosts);
					// store hosts names
					t.setHostNames(hostNames);
				} else if (propName.equals("groups")) {
					List<HostGroup> hostGroups = importHostGroupsFromStream(triggerReader
							.getJsonArrayOrObjectReader());
					for (HostGroup h : hostGroups) {
						triggerHostGroupCollection
								.add(new TriggerHostGroupRelation(t, h));
					}
				} else if (propName.equals("items")) {
					// store the first item
					List<Item> items = importItemsFromStream(
							triggerReader.getJsonArrayOrObjectReader(), null,
							1, true);
					for (Item item : items) {
						if (item.getStatus() != Item.STATUS_ENABLED)
							enabled = false;
					}
					if (items.size() > 0)
						t.setItem(items.get(0));
				} else {
					triggerReader.nextProperty();
				}
			}

			// triggers imported with events may be active but outside the time
			// range -> they should always be set to disabled
			if (t.getLastChange() < (new Date().getTime() - ZabbixConfig.STATUS_SHOW_TRIGGER_TIME * 1000)) {
				t.setEnabled(false);
			} else {
				t.setEnabled(enabled);
			}

			triggerCollection.add(t);
			if (triggerCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertTriggers(triggerCollection);
				triggerCollection.clear();
			}
			if (triggerHostGroupCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper
						.insertTriggerHostgroupRelations(triggerHostGroupCollection);
				triggerHostGroupCollection.clear();
			}
			i++;
			if (task != null)
				task.updateProgress((i * 100) / numTriggers);
		}
		databaseHelper.insertTriggers(triggerCollection);
		databaseHelper
				.insertTriggerHostgroupRelations(triggerHostGroupCollection);
		return triggerCollection;

	}

	/**
	 * Creates a comma-separated string containing the names of all hosts in the
	 * given list.
	 * 
	 * @param hosts
	 *            list of hosts
	 * @return comma-separated host names
	 */
	private String createHostNamesString(List<Host> hosts) {

		List<String> hostnames = new ArrayList<String>();
		for (Host h : hosts) {
			hostnames.add(h.getName());
		}
		return hostnames.toString().replaceAll("[\\[\\]]", "");
	}

	/**
	 * Builds the Zabbix URL using the URL set in the preferences and a constant
	 * suffix
	 */
	private void buildZabbixUrl() {
		String url = mPreferences.getZabbixUrl().trim();
		this.zabbixUrl = url + (url.endsWith("/") ? "" : '/') + API_PHP;
	}

	/**
	 * Validates the Zabbix URL.
	 * 
	 * @throws FatalException
	 *             type SERVER_NOT_FOUND, if the URL is null or equal to the
	 *             default example URL
	 */
	private void validateZabbixUrl() throws FatalException {
		String exampleUrl = mContext.getResources().getString(
				R.string.url_example)
				+ (mContext.getResources().getString(R.string.url_example)
						.endsWith("/") ? "" : '/') + API_PHP;
		if (zabbixUrl == null || zabbixUrl.equals(exampleUrl)) {
			throw new FatalException(Type.SERVER_NOT_FOUND);
		}
	}

	public boolean isLoggedIn() {
		return (mPreferences.getZabbixAuthToken() != null);
	}

	public void logout() {
		mPreferences.setZabbixAuthToken(null);
		// TODO: perform actual logout
	}

}
