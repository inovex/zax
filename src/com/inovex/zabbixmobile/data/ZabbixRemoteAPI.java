package com.inovex.zabbixmobile.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.FatalException.Type;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Application;
import com.inovex.zabbixmobile.model.ApplicationItemRelation;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.HostHostGroupRelation;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerHostGroupRelation;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.util.HttpClientWrapper;
import com.inovex.zabbixmobile.util.JsonArrayOrObjectReader;
import com.inovex.zabbixmobile.util.JsonObjectReader;

public class ZabbixRemoteAPI {
	private static final String ZABBIX_ERROR_NO_API_ACCESS = "No API access";
	private static final String ZABBIX_ERROR_NOT_AUTHORIZED = "Not authorized";
	private static final String ZABBIX_ERROR_LOGIN_INCORRECT = "Login name or password is incorrect";
	private static final int RECORDS_PER_INSERT_BATCH = 50;
	private static final String TAG = ZabbixRemoteAPI.class.getSimpleName();

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
		public static final int HTTP_CONNECTION_TIMEOUT = 30000;
	}

	private final HttpClientWrapper httpClient;
	private final DatabaseHelper databaseHelper;
	private String url;
	private String token;
	private final Context mContext;
	private int _transactionStack;
	private JsonParser lastStream;
	private int transformProgressStart;
	private int transformProgressEnd;
	private boolean _notAuthorizedRetry;
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
	public ZabbixRemoteAPI(Context context, DatabaseHelper databaseHelper, HttpClientWrapper httpClientMock) {
		ClientConnectionManager ccm = null;
		HttpParams params = null;

		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();

			if (prefs.getBoolean("zabbix_trust_all_ssl_ca", false)) {
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

		if (httpClientMock != null) {
			httpClient = httpClientMock;
		} else {
			if (ccm == null || params == null) {
				httpClient = new HttpClientWrapper(new DefaultHttpClient());
			} else {
				httpClient = new HttpClientWrapper(new DefaultHttpClient(ccm, params));
			}
		}

		// if applicable http auth
		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (prefs.getBoolean("http_auth_enabled", false)) {
				String user = prefs.getString("http_auth_username", "");
				String pwd = prefs.getString("http_auth_password", "");
				httpClient.getCredentialsProvider().setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(user, pwd));
			}
		} catch (java.lang.UnsupportedOperationException e1) {
			// for unit test
		}

		// not for testing...
		if (httpClientMock == null) {
			params = httpClient.getParams();
			HttpClientParams.setRedirecting(params, true); // redirecting
			HttpConnectionParams.setConnectionTimeout(params,
					ZabbixConfig.HTTP_CONNECTION_TIMEOUT);
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
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	private JSONObject _queryBuffer(String method, JSONObject params)
			throws IOException, JSONException, ZabbixLoginRequiredException,
			FatalException {
		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", "application/json; charset=utf-8");

		String json = "{" + "	\"jsonrpc\" : \"2.0\"," + "	\"method\" : \""
				+ method + "\"," + "	\"params\" : " + params.toString() + ","
				+ "	\"auth\" : " + (token == null ? "null" : '"' + token + '"')
				+ "," + "	\"id\" : 0" + "}";
		Log.d(TAG, "queryBuffer="+json);

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
				if (result.getJSONObject("error") != null) {
					if (result.getJSONObject("error").getString("data")
							.equals(ZABBIX_ERROR_NO_API_ACCESS)) {
						throw new FatalException(Type.NO_API_ACCESS);
					}
					if (result.getJSONObject("error").getString("data")
							.equals(ZABBIX_ERROR_LOGIN_INCORRECT))
						throw new FatalException(Type.ZABBIX_LOGIN_INCORRECT);
					throw new FatalException(Type.INTERNAL_ERROR, result
							.getJSONObject("error").toString());
				}
				if (result.getString("data").equals(
						ZABBIX_ERROR_LOGIN_INCORRECT))
					throw new FatalException(Type.ZABBIX_LOGIN_INCORRECT);
				if (result.getString("data")
						.equals(ZABBIX_ERROR_NOT_AUTHORIZED)) {
					// this should lead to a retry
					throw new ZabbixLoginRequiredException();
				}
			} catch (JSONException e) {
				// ignore
			}
			return result;
		} catch (SocketException e) {
			throw new FatalException(Type.NO_CONNECTION, e);
		} catch (ConnectTimeoutException e) {
			throw new FatalException(Type.CONNECTION_TIMEOUT, e);
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
		// http request
		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", "application/json; charset=utf-8");

		JSONObject json = new JSONObject().put("jsonrpc", "2.0")
				.put("method", method).put("params", params).put("auth", token)
				.put("id", 0);

		Log.d("ZabbixService", "_queryStream: " + url);
		Log.d("ZabbixService", "_queryStream: " + json.toString());

		post.setEntity(new StringEntity(json.toString(), "UTF-8"));
		try {
			HttpResponse resp = httpClient.execute(post);
			checkHttpStatusCode(resp);

			JsonFactory jsonFac = new JsonFactory();
			JsonParser jp = jsonFac.createParser(resp.getEntity().getContent());
			// store the last stream to close it if an exception will be thrown
			lastStream = jp;
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
					Log.d("ZabbixService",
							"current token: " + jp.getCurrentToken());
					Log.d("ZabbixService",
							"current name: " + jp.getCurrentName());
					Log.d("ZabbixService", "get text: " + jp.getText());
					Log.d("ZabbixService", "next value: " + jp.nextValue());
					Log.d("ZabbixService", "next token: " + jp.nextToken());
					Log.d("ZabbixService",
							"current token: " + jp.getCurrentToken());
					Log.d("ZabbixService",
							"current name: " + jp.getCurrentName());
					Log.d("ZabbixService", "get text: " + jp.getText());
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
			Log.d("ZabbixService", resp.getStatusLine().getStatusCode() + " "
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
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		String url = prefs.getString("zabbix_url", "").trim();
		String user = prefs.getString("zabbix_username", "").trim();
		String password = prefs.getString("zabbix_password", "");
		// String url = "http://10.10.0.21/zabbix";
		// String user = "admin";
		// String password = "zabbix";

		this.url = url + (url.endsWith("/") ? "" : '/') + "api_jsonrpc.php";
		Log.d("ZabbixContentProvider", url + "//" + user);

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
			// get API version
			JSONObject result;
			try {
				result = _queryBuffer("apiinfo.version", new JSONObject());
				apiVersion = result.getString("result");
				isVersion2 = (apiVersion.equals("1.4") || apiVersion
						.startsWith("2"));
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
		return token != null;
	}

	/**
	 * close the last http stream
	 */
	private void closeLastStream() {
		if (lastStream != null && !lastStream.isClosed()) {
			try {
				lastStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Imports all applications from Zabbix.
	 * 
	 * @param hostIds
	 *            collection of host ids to filter the applications by; null: no
	 *            filtering
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public void importApplicationsByHostIds(Collection<Long> hostIds)
			throws FatalException, ZabbixLoginRequiredException {
		// TODO: ignore empty applications
		JSONObject params;
		try {
			params = new JSONObject().put("output", "extend")
					.put("limit", ZabbixConfig.APPLICATION_GET_LIMIT)
					.put(isVersion2 ? "selectHosts" : "select_hosts", "extend")
					.put(isVersion2 ? "selectItems" : "select_items", "extend")
					.put("source", 0);
			if (!isVersion2) {
				// in Zabbix version <2.0, this is not default
				params.put("sortfield", "clock").put("sortorder", "DESC");
			}
			if (hostIds != null)
				params.put("hostids", new JSONArray(hostIds));
			JsonArrayOrObjectReader applications = _queryStream(
					"application.get", params);
			importApplicationsFromStream(applications);
			// events.close();
		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}
	}

	/**
	 * Imports applications from a JSON stream.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @throws JsonParseException
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void importApplicationsFromStream(JsonArrayOrObjectReader jsonReader)
			throws JsonParseException, NumberFormatException, IOException,
			SQLException {
		int num = 0;
		List<Application> applicationsCollection = new ArrayList<Application>(
				RECORDS_PER_INSERT_BATCH);
		List<ApplicationItemRelation> applicationItemRelations = new ArrayList<ApplicationItemRelation>(
				RECORDS_PER_INSERT_BATCH);
		JsonObjectReader application;
		List<Item> items = new ArrayList<Item>();
		while ((application = jsonReader.next()) != null) {
			Application app = new Application();
			while (application.nextValueToken()) {
				String propName = application.getCurrentName();
				if (propName.equals(Application.COLUMN_APPLICATIONID)) {
					app.setId(Long.parseLong(application.getText()));
				} else if (propName.equals(Application.COLUMN_NAME)) {
					app.setName(application.getText());
					// } else if
					// (propName.equals(ApplicationData.COLUMN_HOSTID)) {
					// app.set(ApplicationData.COLUMN_HOSTID,
					// Long.parseLong(application.getText()));
				} else if (propName.equals("items")) {
					items = importItemsFromStream(
							application.getJsonArrayOrObjectReader(), 0, false);
				} else if (propName.equals("hosts")) {
					// import hosts
					List<Host> hosts = importHostsFromStream(
							application.getJsonArrayOrObjectReader(), null);
					if (hosts.size() > 0) {
						Host t = hosts.get(0);
						app.setHost(t);
					}
					if (hosts.size() > 1) {
						Log.w(TAG, "More than one host found for application "
								+ app.getId() + ": " + app.getName());
					}
				} else {
					application.nextProperty();
				}
			}
			// create applications that does not exist
			// app.insert(zabbixLocalDB, ApplicationData.COLUMN_APPLICATIONID);
			// _commitTransactionIfRecommended();
			//
			// // create ApplicationItemRelation
			// long appid = (Long)
			// app.get(ApplicationData.COLUMN_APPLICATIONID);
			// ApplicationItemRelationData rel = new
			// ApplicationItemRelationData();
			// rel.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, appid);
			// rel.set(ApplicationItemRelationData.COLUMN_ITEMID, itemid);
			// rel.set(ApplicationItemRelationData.COLUMN_HOSTID, hostid);
			// rel.insert(zabbixLocalDB);
			// _commitTransactionIfRecommended();

			num++;
			applicationsCollection.add(app);
			if (applicationsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertApplications(applicationsCollection);
				applicationsCollection = new ArrayList<Application>(
						RECORDS_PER_INSERT_BATCH);
			}

			// insert application item relations
			for(Item item : items) {
				applicationItemRelations.add(new ApplicationItemRelation(app, item));
			}
			if (applicationItemRelations.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertApplicationItemRelations(applicationItemRelations);
				applicationItemRelations = new ArrayList<ApplicationItemRelation>(
						RECORDS_PER_INSERT_BATCH);
			}
		}
		// insert the last batch of applications
		databaseHelper.insertApplications(applicationsCollection);
		databaseHelper.insertApplicationItemRelations(applicationItemRelations);
		// if (num == 0) {
		// // if there's no application, the ID #0 must be added (for other)
		// ApplicationItemRelationData rel = new ApplicationItemRelationData();
		// rel.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 0);
		// rel.set(ApplicationItemRelationData.COLUMN_ITEMID, itemid);
		// rel.set(ApplicationItemRelationData.COLUMN_HOSTID, hostid);
		// rel.insert(zabbixLocalDB);
		//
		// // if #0 "other" does not exist yet, it has to be created
		// ApplicationData app = new ApplicationData();
		// app.set(ApplicationData.COLUMN_APPLICATIONID, 0);
		// app.set(ApplicationData.COLUMN_NAME, "- "
		// + context.getResources().getString(R.string.other) + " -");
		// app.insert(zabbixLocalDB, ApplicationData.COLUMN_APPLICATIONID);
		// }
	}

	//
	// /**
	// * import the newest event of a trigger
	// * @param triggerid
	// * @throws JSONException
	// * @throws IOException
	// * @throws HttpAuthorizationRequiredException
	// * @throws NoAPIAccessException
	// */
	// public void importEventByTriggerId(String triggerid) throws
	// JSONException, IOException, HttpAuthorizationRequiredException,
	// NoAPIAccessException, PreconditionFailedException {
	// if (!isCached(EventData.TABLE_NAME, "triggerid="+triggerid)) {
	// _startTransaction();
	// zabbixLocalDB.delete(EventData.TABLE_NAME,
	// EventData.COLUMN_OBJECTID+"="+triggerid, null);
	//
	// JSONObject params = new JSONObject()
	// .put("output", "extend")
	// .put(isVersion2?"selectHosts":"select_hosts", "extend")
	// .put("triggerids", new JSONArray().put(triggerid))
	// .put("limit", 1)
	// .put("source", 0);
	// if (!isVersion2) {
	// params.put("sortfield", "clock")
	// .put("sortorder", "DESC");
	// }
	//
	// JsonArrayOrObjectReader events = _queryStream(
	// "event.get"
	// , params
	// );
	// importEvents(events, null);
	// events.close();
	//
	// setCached(EventData.TABLE_NAME, "triggerid="+triggerid,
	// ZabbixConfig.CACHE_LIFETIME_EVENTS);
	// _endTransaction();
	// }
	// }

	/**
	 * Import the newest events if they are not cached in the local database
	 * already.
	 * 
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	public void importEvents() throws ZabbixLoginRequiredException,
			FatalException {

		try {
			if (databaseHelper.isCached(CacheDataType.EVENT)) {
				Log.d(TAG, "Events do not need to be refreshed.");
				return;
			}
		} catch (SQLException e1) {
			// ignore this because the worst that can happen is an unnecessary
			// API call
		}

		try {
			databaseHelper.clearEvents();

			int numEvents = ZabbixConfig.EVENTS_GET_LIMIT;

			JSONObject params;
			params = new JSONObject()
					.put("output", "extend")
					.put("limit", ZabbixConfig.EVENTS_GET_LIMIT)
					.put(isVersion2 ? "selectHosts" : "select_hosts", "extend")
					.put(isVersion2 ? "selectTriggers" : "select_triggers",
							"extend")
					.put("source", 0)
					.put("time_from",
							(new Date().getTime() / 1000)
									- ZabbixConfig.EVENT_GET_TIME_FROM_SHIFT);

			if (!isVersion2) {
				// in Zabbix version <2.0, this is not default
				params.put("sortfield", "clock").put("sortorder", "DESC");
			}
			JsonArrayOrObjectReader events = _queryStream("event.get", params);
			importEvents(events);
			// events.close();
			databaseHelper.setCached(CacheDataType.EVENT);
		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
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
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws SQLException
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 * @throws JSONException
	 */
	private void importEvents(JsonArrayOrObjectReader jsonReader)
			throws JsonParseException, IOException, SQLException,
			JSONException, ZabbixLoginRequiredException, FatalException {
		JsonObjectReader eventReader;
		List<Event> eventsCollection = new ArrayList<Event>(
				RECORDS_PER_INSERT_BATCH);
		HashSet<Long> triggerIds = new HashSet<Long>();
		while ((eventReader = jsonReader.next()) != null) {
			Event e = new Event();
			while (eventReader.nextValueToken()) {
				String propName = eventReader.getCurrentName();
				if (propName.equals("hosts")) {
					// import hosts
					List<Host> hosts = importHostsFromStream(
							eventReader.getJsonArrayOrObjectReader(), null);
					String hostNames = createHostNamesString(hosts);
					// store hosts namen
					e.setHostNames(hostNames);
				} else if (propName.equals("triggers")) {
					// import triggers
					List<Trigger> triggers = importTriggersFromStream(eventReader
							.getJsonArrayOrObjectReader());
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
			}
			eventsCollection.add(e);
			if (eventsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertEvents(eventsCollection);
				eventsCollection = new ArrayList<Event>(
						RECORDS_PER_INSERT_BATCH);
			}
		}
		// insert the last batch of events
		databaseHelper.insertEvents(eventsCollection);
		// we need to close here to be able to start another import (triggers)
		jsonReader.close();
		importTriggersByIds(triggerIds, false);
	}

	// /**
	// * import graph items
	// *
	// * @param graphItems
	// * @return true, if graphid column has to be updated from -1 to the
	// correct graphid later.
	// * @throws JsonParseException
	// * @throws NumberFormatException
	// * @throws IOException
	// */
	// private boolean importGraphItems(JsonArrayOrObjectReader graphItems)
	// throws JsonParseException, NumberFormatException, IOException {
	// boolean mustSetGraphid = false;
	// JsonObjectReader graphItemReader;
	// while ((graphItemReader = graphItems.next()) != null) {
	// GraphItemData gi = new GraphItemData();
	// while (graphItemReader.nextValueToken()) {
	// String propName = graphItemReader.getCurrentName();
	// if (propName.equals("gitemid")) {
	// gi.set(GraphItemData.COLUMN_GRAPHITEMID,
	// Long.parseLong(graphItemReader.getText()));
	// } else if (propName.equals(GraphItemData.COLUMN_GRAPHID)) {
	// gi.set(GraphItemData.COLUMN_GRAPHID,
	// Long.parseLong(graphItemReader.getText()));
	// } else if (propName.equals(GraphItemData.COLUMN_ITEMID)) {
	// gi.set(GraphItemData.COLUMN_ITEMID,
	// Long.parseLong(graphItemReader.getText()));
	// } else if (propName.equals(GraphItemData.COLUMN_COLOR)) {
	// // hex string => color int
	// gi.set(GraphItemData.COLUMN_COLOR,
	// Color.parseColor("#"+graphItemReader.getText()));
	// } else {
	// graphItemReader.nextProperty();
	// }
	// }
	// // if graphid-column was not set, this must be done later. temporary
	// graphid=-1
	// if (gi.get(GraphItemData.COLUMN_GRAPHID) == null) {
	// gi.set(GraphItemData.COLUMN_GRAPHID, -1);
	// mustSetGraphid = true;
	// }
	// gi.insert(zabbixLocalDB);
	// }
	// return mustSetGraphid;
	// }
	//
	// public void importGraphsForScreen(long screenid) throws
	// ClientProtocolException, IOException, JSONException,
	// HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(GraphData.TABLE_NAME, "screenid="+screenid)) {
	// _startTransaction();
	//
	// // collect all graphids
	// Set<Long> graphids = new HashSet<Long>();
	// Cursor cur = zabbixLocalDB.query(ScreenItemData.TABLE_NAME, null,
	// ScreenItemData.COLUMN_SCREENID+"="+screenid, null, null, null, null);
	// while (cur.moveToNext()) {
	// graphids.add(cur.getLong(cur.getColumnIndex(ScreenItemData.COLUMN_RESOURCEID)));
	// }
	//
	// // delete old graphs
	// String str_graphids = graphids.toString().replace("[", "").replace("]",
	// "");
	// if (str_graphids.length()>0) {
	// zabbixLocalDB.delete(GraphData.TABLE_NAME,
	// GraphData.COLUMN_GRAPHID+" IN ("+str_graphids+")", null);
	// zabbixLocalDB.delete(GraphItemData.TABLE_NAME,
	// GraphItemData.COLUMN_GRAPHID+" IN ("+str_graphids+")", null);
	// }
	// zabbixLocalDB.delete(CacheData.TABLE_NAME,
	// CacheData.COLUMN_KIND+"='"+GraphData.TABLE_NAME+"' AND "+CacheData.COLUMN_FILTER+"='screenid="+screenid+'\'',
	// null);
	//
	// JsonArrayOrObjectReader graphs = _queryStream(
	// "graph.get"
	// , new JSONObject()
	// .put(isVersion2?"selectGraphItems":"select_graph_items", "extend")
	// .put(isVersion2?"selectItems":"select_items", "extend")
	// .put("output", "extend")
	// .put("graphids", new JSONArray(graphids))
	// );
	// JsonObjectReader graphReader;
	// while ((graphReader = graphs.next()) != null) {
	// boolean mustSetGraphid = false;
	// GraphData scr = new GraphData();
	// while (graphReader.nextValueToken()) {
	// String propName = graphReader.getCurrentName();
	// if (propName.equals(GraphData.COLUMN_GRAPHID)) {
	// scr.set(GraphData.COLUMN_GRAPHID, Long.parseLong(graphReader.getText()));
	// } else if (propName.equals(GraphData.COLUMN_NAME)) {
	// scr.set(GraphData.COLUMN_NAME, graphReader.getText());
	// } else if (propName.equals("gitems")) {
	// mustSetGraphid =
	// importGraphItems(graphReader.getJsonArrayOrObjectReader());
	// } else if (propName.equals("items")) {
	// importItems(graphReader.getJsonArrayOrObjectReader(), 0, true);
	// } else {
	// graphReader.nextProperty();
	// }
	// }
	// scr.insert(zabbixLocalDB);
	// if (mustSetGraphid) {
	// ContentValues values = new ContentValues(1);
	// values.put(GraphItemData.COLUMN_GRAPHID, (Long)
	// scr.get(GraphData.COLUMN_GRAPHID));
	// zabbixLocalDB.update(
	// GraphItemData.TABLE_NAME
	// , values
	// , GraphItemData.COLUMN_GRAPHID+"=-1"
	// , null);
	// }
	// }
	// graphs.close();
	//
	// setCached(GraphData.TABLE_NAME, "screenid="+screenid,
	// ZabbixConfig.CACHE_LIFETIME_SCREENS);
	// _endTransaction();
	// }
	// }
	//
	// public void importHistoryDetails(String itemid) throws
	// ClientProtocolException, IOException, JSONException,
	// HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(HistoryDetailData.TABLE_NAME, itemid)) {
	// _startTransaction();
	// zabbixLocalDB.delete(HistoryDetailData.TABLE_NAME,
	// HistoryDetailData.COLUMN_ITEMID+"="+itemid, null);
	//
	// // the past 2 hours
	// long time_till = new Date().getTime() / 1000;
	// long time_from = time_till - ZabbixConfig.HISTORY_GET_TIME_FROM_SHIFT;
	//
	// // Workaround: historydetails only comes if you use the correct
	// "history"-parameter.
	// // this parameter can be "null" or a number 0-4. Because we don't know
	// when to use which,
	// // we try them all, until we get results.
	// Integer historytype = null;
	// JSONObject result = _queryBuffer(
	// "history.get"
	// , new JSONObject()
	// .put("limit", 1)
	// .put("history", historytype) // for integer ?
	// .put("itemids", new JSONArray().put(itemid))
	// .put("time_from", time_from)
	// );
	//
	// JSONArray testHistorydetails = result.getJSONArray("result");
	// if (testHistorydetails.length() == 0) {
	// historytype = -1;
	// while (testHistorydetails.length() == 0 && ++historytype <= 4) {
	// // if we get an empty array, we try another history parameter
	// result = _queryBuffer(
	// "history.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put("limit", 1)
	// .put("history", historytype)
	// .put("itemids", new JSONArray().put(itemid))
	// .put("time_from", time_from)
	// );
	// testHistorydetails = result.getJSONArray("result");
	// }
	// }
	// // correct historytype found and there are data
	// if (testHistorydetails.length() > 0) {
	// // count of the entries cannot be detected (zabbix bug),
	// // so we use a fiction
	// int numDetails = 400;
	// int curI=0;
	// JsonArrayOrObjectReader historydetails = _queryStream(
	// "history.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put("limit", ZabbixConfig.HISTORY_GET_LIMIT)
	// .put("history", historytype)
	// .put("itemids", new JSONArray().put(itemid))
	// .put("time_from", time_from)
	// .put("sortfield", "clock")
	// .put("sortorder", "DESC")
	// );
	// JsonObjectReader historydetail;
	// try {
	// int selI = 0;
	// while ((historydetail = historydetails.next()) != null) {
	// // save only every 20th
	// if (selI++ % 20 != 0) {
	// while (historydetail.nextValueToken()) {
	// historydetail.nextProperty();
	// }
	// continue;
	// }
	//
	// HistoryDetailData h = new HistoryDetailData();
	// while (historydetail.nextValueToken()) {
	// String propName = historydetail.getCurrentName();
	// if (propName.equals(HistoryDetailData.COLUMN_CLOCK)) {
	// h.set(HistoryDetailData.COLUMN_CLOCK,
	// Integer.parseInt(historydetail.getText()));
	// } else if (propName.equals(HistoryDetailData.COLUMN_ITEMID)) {
	// h.set(HistoryDetailData.COLUMN_ITEMID,
	// Long.parseLong(historydetail.getText()));
	// } else if (propName.equals(HistoryDetailData.COLUMN_VALUE)) {
	// h.set(HistoryDetailData.COLUMN_VALUE,
	// Double.parseDouble(historydetail.getText()));
	// } else {
	// historydetail.nextProperty();
	// }
	// }
	// h.insert(zabbixLocalDB);
	// if (++curI % 10 == 0) {
	// showProgress(Math.min(curI*100/numDetails, 84));
	// }
	// _commitTransactionIfRecommended();
	// }
	// } catch (NumberFormatException e) {
	// // data are unuseable, e.g. because it's a string
	// }
	// historydetails.close();
	// }
	// setCached(HistoryDetailData.TABLE_NAME, itemid,
	// ZabbixConfig.CACHE_LIFETIME_HISTORY_DETAILS);
	// _endTransaction();
	// }
	// }

	/**
	 * Imports host groups from a JSON stream.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @return list of host groups parsed from jsonReader
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws SQLException
	 */
	private List<HostGroup> importHostGroups(JsonArrayOrObjectReader jsonReader)
			throws JsonParseException, IOException, SQLException {
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
			// TODO: hostGroupCollection.clear() ? Testen
			if (hostGroupCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertHostGroups(hostGroupCollection);
				hostGroupCollection = new ArrayList<HostGroup>(
						RECORDS_PER_INSERT_BATCH);
			}
		}
		databaseHelper.insertHostGroups(hostGroupCollection);

		return hostGroupCollection;
		// return firstHostGroupId;
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
	 * Imports hosts from a JSON stream and returns a string of comma-separated
	 * host names.
	 * 
	 * This method also fills the host to host group relation if a host's groups
	 * have been selected.
	 * 
	 * @param jsonReader
	 *            JSON stream reader
	 * @param numHosts
	 *            count of hosts for progressbar; null if unknown
	 * @return list of hosts retrieved from the stream
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws SQLException
	 */
	private List<Host> importHostsFromStream(
			JsonArrayOrObjectReader jsonReader, Integer numHosts)
			throws JsonParseException, IOException, SQLException {
		List<Host> hosts = new ArrayList<Host>();
		List<HostHostGroupRelation> hostHostGroupCollection = new ArrayList<HostHostGroupRelation>();
		long firstHostId = -1;
		JsonObjectReader hostReader;
		int i = 0;
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
					h.setHost(host);
				} else if (propName.equals("groups")) {
					List<HostGroup> groups = importHostGroups(hostReader
							.getJsonArrayOrObjectReader());
					for (HostGroup group : groups) {
						// create HostHostGroupRelation
						hostHostGroupCollection.add(new HostHostGroupRelation(
								h, group));
					}
					// if (groupid != -1) {
					// h.set(HostData.COLUMN_GROUPID, groupid);
					// }
				} else {
					hostReader.nextProperty();
				}
			}
			hosts.add(h);
			// host without group will get group #0 (other)
			// if (h.get(HostData.COLUMN_GROUPID) == null) {
			// h.set(HostData.COLUMN_GROUPID, 0);
			// // create "other" hostgroup #1
			// HostGroupData hg = new HostGroupData();
			// hg.set(HostGroupData.COLUMN_GROUPID, 0);
			// hg.set(HostGroupData.COLUMN_NAME, "- "
			// + context.getResources().getString(R.string.other)
			// + " -");
			// hg.insert(zabbixLocalDB, HostGroupData.COLUMN_GROUPID);
			// }
			// h.insert(zabbixLocalDB, HostData.COLUMN_HOSTID);
			// if (numHosts != null && ++i % 10 == 0) {
			// showProgress(i * 100 / numHosts);
			// }
			// _commitTransactionIfRecommended();
			if (hosts.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertHosts(hosts);
				hosts = new ArrayList<Host>(RECORDS_PER_INSERT_BATCH);
			}
		}
		// insert the last batch of events
		databaseHelper.insertHosts(hosts);

		if (!hostHostGroupCollection.isEmpty())
			databaseHelper
					.insertHostHostgroupRelations(hostHostGroupCollection);

		// return new Object[] { hostnames.toString().replaceAll("[\\[\\]]",
		// ""),
		// firstHostId };
		return hosts;
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
		// if (!isCached(HostData.TABLE_NAME, null)) {
		try {
			databaseHelper.clearHosts();
			databaseHelper.clearHostGroups();

			// get count of hosts
			// TODO: progress bar
			// JSONObject result = _queryBuffer(
			// "host.get",
			// new JSONObject()
			// .put("countOutput", 1)
			// .put("limit", ZabbixConfig.HOST_GET_LIMIT)
			// .put(isVersion2 ? "selectGroups" : "select_groups",
			// "extend"));
			// int numHosts = result.getInt("result");
			JsonArrayOrObjectReader hosts = _queryStream(
					"host.get",
					new JSONObject()
							.put("output", "extend")
							.put("limit", ZabbixConfig.HOST_GET_LIMIT)
							.put(isVersion2 ? "selectGroups" : "select_groups",
									"extend"));
			importHostsFromStream(hosts, null);
			hosts.close();

		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		// setCached(HostData.TABLE_NAME, null,
		// ZabbixConfig.CACHE_LIFETIME_HOSTS);
		// }
	}

	/**
	 * import items from stream.
	 * 
	 * @param jsonReader
	 *            stream
	 * @param numItems
	 *            count for progressbar, if 0 no progressbarupdate
	 * @param checkBeforeInsert
	 *            if true, only make an insert if item does not exist
	 * @return the first item id
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws SQLException
	 */
	private List<Item> importItemsFromStream(
			JsonArrayOrObjectReader jsonReader, int numItems,
			boolean checkBeforeInsert) throws JsonParseException, IOException,
			SQLException {
		long firstItemId = -1;
		int curI = 0;
		JsonObjectReader itemReader;
		List<Item> items = new ArrayList<Item>();
		while ((itemReader = jsonReader.next()) != null) {
			Item i = new Item();
			String key_ = null;
			while (itemReader.nextValueToken()) {
				String propName = itemReader.getCurrentName();
				if (propName.equals(Item.COLUMN_ITEMID)) {
					i.setId(Long.parseLong(itemReader.getText()));
					// if (firstItemId == -1) {
					// firstItemId = (Long) i.get(Item.COLUMN_ITEMID);
					// }
				} else if (propName.equals(Item.COLUMN_HOSTID)) {
					// i.setHost(Long.parseLong(itemReader.getText()));
				} else if (propName.equals(Item.COLUMN_DESCRIPTION)
						|| propName.equals(Item.COLUMN_DESCRIPTION_OLD)) {
					// since zabbix 2.x is the name of the item "name"
					// before zabbix 2.x the name field was "description"
					if (isVersion2
							&& propName.equals(Item.COLUMN_DESCRIPTION_OLD)) {
						i.setDescription(itemReader.getText());
					} else if (!isVersion2) {
						i.setDescription(itemReader.getText());
					}
				} else if (propName.equals(Item.COLUMN_LASTCLOCK)) {
					i.setLastClock(Long.parseLong(itemReader.getText()) * 1000);
				} else if (propName.equals(Item.COLUMN_LASTVALUE)) {
					i.setLastValue(itemReader.getText());
				} else if (propName.equals(Item.COLUMN_UNITS)) {
					i.setUnits(itemReader.getText());
				} else if (propName.equals("key_")) {
					key_ = itemReader.getText();
				} else if (propName.equals("applications")) {
					// at this point itemid and hostid is unknown
					// because of this, all applicationrelations will be saved
					// with itemid
					// and hostid "-1".
					// later the IDs will be replaced with the correct
					// importApplications(-1, -1,
					// itemReader.getJsonArrayOrObjectReader());
				} else {
					itemReader.nextProperty();
				}
			}
			// if applicable replace placeholder
			String description = i.getDescription();
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
			i.setDescription(description);

			// now the "-1" IDs of applicationsrelation will be replaced with
			// the
			// correct itemID
			// ContentValues values = new ContentValues(2);
			// values.put(ApplicationItemRelationData.COLUMN_ITEMID,
			// (Long) i.get(ItemData.COLUMN_ITEMID));
			// values.put(ApplicationItemRelationData.COLUMN_HOSTID,
			// (Long) i.get(ItemData.COLUMN_HOSTID));
			// zabbixLocalDB.update(ApplicationItemRelationData.TABLE_NAME,
			// values, ApplicationItemRelationData.COLUMN_ITEMID + "=-1",
			// null);
			items.add(i);
			if (items.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertItems(items);
				items = new ArrayList<Item>(RECORDS_PER_INSERT_BATCH);
			}
		}
		// insert the last batch of events
		databaseHelper.insertItems(items);
		return items;
	}

	public void importItems(long hostid) throws FatalException,
			ZabbixLoginRequiredException {
		// if (!isCached(ItemData.TABLE_NAME, "hostid=" + hostid)) {
		// zabbixLocalDB.delete(ApplicationItemRelationData.TABLE_NAME,
		// ApplicationItemRelationData.COLUMN_HOSTID + "=" + hostid,
		// null);
		try {
			databaseHelper.clearItems();

			// count of items
			// JSONObject result = _queryBuffer(
			// "item.get",
			// new JSONObject().put("output", "extend")
			// .put("countOutput", 1)
			// .put("limit", ZabbixConfig.ITEM_GET_LIMIT)
			// .put("hostids", new JSONArray().put(hostid)));
			// int numItems = result.getInt("result");
			JsonArrayOrObjectReader items = _queryStream(
					"item.get",
					new JSONObject()
							.put("output", "extend")
							.put("limit", ZabbixConfig.ITEM_GET_LIMIT)
							.put(isVersion2 ? "selectApplications"
									: "select_applications", "extend")
							.put("hostids", new JSONArray().put(hostid)));
			importItemsFromStream(items, 0, false);
			items.close();

		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		// setCached(ItemData.TABLE_NAME, "hostid=" + hostid,
		// ZabbixConfig.CACHE_LIFETIME_ITEMS);
		// }
	}

	// public void importItems(long hostid) throws JSONException, IOException,
	// HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(ItemData.TABLE_NAME, "hostid=" + hostid)) {
	// _startTransaction();
	// zabbixLocalDB.delete(ItemData.TABLE_NAME, ItemData.COLUMN_HOSTID
	// + "=" + hostid, null);
	// zabbixLocalDB.delete(ApplicationItemRelationData.TABLE_NAME,
	// ApplicationItemRelationData.COLUMN_HOSTID + "=" + hostid,
	// null);
	//
	// // count of items
	// JSONObject result = _queryBuffer(
	// "item.get",
	// new JSONObject().put("output", "extend")
	// .put("countOutput", 1)
	// .put("limit", ZabbixConfig.ITEM_GET_LIMIT)
	// .put("hostids", new JSONArray().put(hostid)));
	// int numItems = result.getInt("result");
	// JsonArrayOrObjectReader items = _queryStream(
	// "item.get",
	// new JSONObject()
	// .put("output", "extend")
	// .put("limit", ZabbixConfig.ITEM_GET_LIMIT)
	// .put(isVersion2 ? "selectApplications"
	// : "select_applications", "extend")
	// .put("hostids", new JSONArray().put(hostid)));
	// importItems(items, numItems, false);
	// items.close();
	//
	// setCached(ItemData.TABLE_NAME, "hostid=" + hostid,
	// ZabbixConfig.CACHE_LIFETIME_ITEMS);
	// _endTransaction();
	// }
	// }

	// private void importScreenItems(JsonArrayOrObjectReader screenItems)
	// throws JsonParseException, NumberFormatException, IOException {
	// JsonObjectReader screenItemReader;
	// while ((screenItemReader = screenItems.next()) != null) {
	// ScreenItemData si = new ScreenItemData();
	// int resourcetype = -1;
	// while (screenItemReader.nextValueToken()) {
	// String propName = screenItemReader.getCurrentName();
	// if (propName.equals(ScreenItemData.COLUMN_SCREENITEMID)) {
	// si.set(ScreenItemData.COLUMN_SCREENITEMID,
	// Long.parseLong(screenItemReader.getText()));
	// } else if (propName.equals(ScreenItemData.COLUMN_SCREENID)) {
	// si.set(ScreenItemData.COLUMN_SCREENID,
	// Long.parseLong(screenItemReader.getText()));
	// } else if (propName.equals(ScreenItemData.COLUMN_RESOURCEID)) {
	// si.set(ScreenItemData.COLUMN_RESOURCEID,
	// Long.parseLong(screenItemReader.getText()));
	// } else if (propName.equals("resourcetype")) {
	// resourcetype = Integer.parseInt(screenItemReader.getText());
	// } else {
	// screenItemReader.nextProperty();
	// }
	// }
	// // only resouretype == 0
	// if (resourcetype == 0) {
	// si.insert(zabbixLocalDB);
	// }
	// }
	// }
	//
	// public void importScreens() throws ClientProtocolException, IOException,
	// JSONException, HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(ScreenData.TABLE_NAME, null)) {
	// _startTransaction();
	//
	// zabbixLocalDB.delete(ScreenData.TABLE_NAME, null, null);
	// zabbixLocalDB.delete(ScreenItemData.TABLE_NAME, null, null);
	// zabbixLocalDB.delete(CacheData.TABLE_NAME,
	// CacheData.COLUMN_KIND+"='"+ScreenData.TABLE_NAME+"'", null);
	//
	// JsonArrayOrObjectReader screens = _queryStream(
	// (isVersion2?"s":"S")+"creen.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put(isVersion2?"selectScreenItems":"select_screenitems", "extend")
	// );
	// JsonObjectReader screenReader;
	// while ((screenReader = screens.next()) != null) {
	// ScreenData scr = new ScreenData();
	// while (screenReader.nextValueToken()) {
	// String propName = screenReader.getCurrentName();
	// if (propName.equals(ScreenData.COLUMN_SCREENID)) {
	// scr.set(ScreenData.COLUMN_SCREENID,
	// Long.parseLong(screenReader.getText()));
	// } else if (propName.equals(ScreenData.COLUMN_NAME)) {
	// scr.set(ScreenData.COLUMN_NAME, screenReader.getText());
	// } else if (propName.equals("screenitems")) {
	// importScreenItems(screenReader.getJsonArrayOrObjectReader());
	// } else {
	// screenReader.nextProperty();
	// }
	// }
	// scr.insert(zabbixLocalDB);
	// }
	// screens.close();
	//
	// setCached(ScreenData.TABLE_NAME, null,
	// ZabbixConfig.CACHE_LIFETIME_SCREENS);
	// _endTransaction();
	// }
	// }
	//
	// public void importTrigger(String triggerid) throws JSONException,
	// IOException, HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(TriggerData.TABLE_NAME, "triggerid="+triggerid)) {
	// _startTransaction();
	//
	// JsonArrayOrObjectReader trigger = _queryStream(
	// "trigger.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put(isVersion2?"selectHosts":"select_hosts", "extend")
	// .put("triggerids", new JSONArray().put(triggerid))
	// );
	// importTriggers(trigger);
	// trigger.close();
	//
	// setCached(TriggerData.TABLE_NAME, "triggerid="+triggerid,
	// ZabbixConfig.CACHE_LIFETIME_TRIGGERS);
	// _endTransaction();
	// }
	// }
	//
	// /**
	// * importiert ein trigger mit dem attribute itemid. ggf. wird der trigger
	// zuerst gelöscht und neu angelegt.
	// * imports a trigger with the attribute itemid. if trigger already exists,
	// it will be removed first.
	// * @param triggerid
	// * @throws JSONException
	// * @throws IOException
	// * @throws HttpAuthorizationRequiredException
	// * @throws NoAPIAccessException
	// */
	// public void importTriggerColumnItemId(String triggerid) throws
	// JSONException, IOException, HttpAuthorizationRequiredException,
	// NoAPIAccessException, PreconditionFailedException {
	// // check if the trigger exists and if the itemid was set
	// boolean mustImport = true;
	// Cursor cur = zabbixLocalDB.query(TriggerData.TABLE_NAME, null,
	// TriggerData.COLUMN_TRIGGERID+"="+triggerid, null, null, null, null);
	// if (cur.moveToFirst()) {
	// long itemid = 0;
	// try {
	// itemid = cur.getLong(cur.getColumnIndex(TriggerData.COLUMN_ITEMID));
	// } catch (Exception e) {
	// // no itemid
	// }
	// if (itemid > 0) {
	// mustImport = false;
	// }
	// }
	// if (mustImport) {
	// _startTransaction();
	// zabbixLocalDB.delete(TriggerData.TABLE_NAME,
	// TriggerData.COLUMN_TRIGGERID+"="+triggerid, null);
	// JsonArrayOrObjectReader triggers = _queryStream(
	// "trigger.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put(isVersion2?"selectHosts":"select_hosts", "extend")
	// .put(isVersion2?"selectGroups":"select_groups", "extend")
	// .put(isVersion2?"selectItems":"select_items", "extend")
	// .put("triggerids", new JSONArray().put(triggerid))
	// .put("limit", 1)
	// );
	// importTriggers(triggers);
	// _endTransaction();
	// }
	// }

	/**
	 * Imports active triggers.
	 * 
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	public void importActiveTriggers() throws ZabbixLoginRequiredException,
			FatalException {
		importTriggersByIds(null, true);
	}

	/**
	 * Imports the triggers with matching IDs.
	 * 
	 * @param triggerIds
	 *            collection of trigger ids to be matched; null: import all
	 *            triggers
	 * @param onlyActive
	 *            true: import only active triggers; false: import all
	 * @throws JSONException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	private void importTriggersByIds(Collection<Long> triggerIds,
			boolean onlyActive) throws ZabbixLoginRequiredException,
			FatalException {
		// clear triggers
		// databaseHelper.clearTriggers();

		try {

			long min = (new Date().getTime() / 1000)
					- ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;

			JSONObject params = new JSONObject();
			params.put("output", "extend")
					.put("sortfield", "lastchange")
					.put("sortorder", "desc")
					// .put(isVersion2 ? "selectHosts" : "select_hosts",
					// "extend")
					.put(isVersion2 ? "selectGroups" : "select_groups",
							"extend")
					// .put(isVersion2 ? "selectItems" : "select_items",
					// "extend").put("lastChangeSince", min)
					.put("limit", ZabbixConfig.TRIGGER_GET_LIMIT)
					.put("expandDescription", true);

			if (triggerIds != null) {
				params.put("triggerids", new JSONArray(triggerIds));
			}
			if (onlyActive)
				params.put("only_true", "1");
			JsonArrayOrObjectReader triggers = _queryStream("trigger.get",
					params);
			importTriggersFromStream(triggers);
			triggers.close();
		} catch (SQLException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
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
	 * @return list of imported triggers
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws SQLException
	 */
	private List<Trigger> importTriggersFromStream(
			JsonArrayOrObjectReader jsonReader) throws JsonParseException,
			IOException, SQLException {
		List<Trigger> triggerCollection = new ArrayList<Trigger>(
				RECORDS_PER_INSERT_BATCH);
		List<TriggerHostGroupRelation> triggerHostGroupCollection = new ArrayList<TriggerHostGroupRelation>(
				RECORDS_PER_INSERT_BATCH);
		JsonObjectReader triggerReader;
		while ((triggerReader = jsonReader.next()) != null) {
			Trigger t = new Trigger();
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
					// Object[] hostsCache =
					// importHosts(triggerReader.getJsonArrayOrObjectReader(),
					// null);
					// t.set(TriggerData.COLUMN_HOSTS, hostsCache[0]);
					// t.set(TriggerData.COLUMN_HOSTID, hostsCache[1]);
				} else if (propName.equals("groups")) {
					List<HostGroup> hostGroups = importHostGroups(triggerReader
							.getJsonArrayOrObjectReader());
					for (HostGroup h : hostGroups) {
						triggerHostGroupCollection
								.add(new TriggerHostGroupRelation(t, h));
					}
				} else if (propName.equals("items")) {
					// store the first item id
					// t.set(TriggerData.COLUMN_ITEMID,
					// importItems(triggerReader.getJsonArrayOrObjectReader(),
					// 1, true));
				} else {
					triggerReader.nextProperty();
				}
			}
			triggerCollection.add(t);
			if (triggerCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertTriggers(triggerCollection);
				triggerCollection = new ArrayList<Trigger>(
						RECORDS_PER_INSERT_BATCH);
			}
			if (triggerHostGroupCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper
						.insertTriggerHostgroupRelations(triggerHostGroupCollection);
				triggerHostGroupCollection = new ArrayList<TriggerHostGroupRelation>(
						RECORDS_PER_INSERT_BATCH);
			}
		}
		databaseHelper.insertTriggers(triggerCollection);
		databaseHelper
				.insertTriggerHostgroupRelations(triggerHostGroupCollection);
		return triggerCollection;

	}

	//
	// public void importTriggersByItemId(String itemid) throws JSONException,
	// IOException, HttpAuthorizationRequiredException, NoAPIAccessException,
	// PreconditionFailedException {
	// if (!isCached(TriggerData.TABLE_NAME, "itemid="+itemid)) {
	// _startTransaction();
	// zabbixLocalDB.delete(TriggerData.TABLE_NAME,
	// TriggerData.COLUMN_ITEMID+"="+itemid, null);
	// JsonArrayOrObjectReader triggers = _queryStream(
	// "trigger.get"
	// , new JSONObject()
	// .put("output", "extend")
	// .put(isVersion2?"selectHosts":"select_hosts", "extend")
	// .put(isVersion2?"selectGroups":"select_groups", "extend")
	// .put(isVersion2?"selectItems":"select_items", "extend")
	// .put("itemids", new JSONArray().put(itemid))
	// .put("limit", 1)
	// .put("sortfield", "lastchange")
	// .put("sortorder", "DESC")
	// );
	// importTriggers(triggers);
	// setCached(TriggerData.TABLE_NAME, "itemid="+itemid,
	// ZabbixConfig.CACHE_LIFETIME_TRIGGERS);
	// _endTransaction();
	// }
	// }

	// public boolean isCached(String kind, String filter) {
	// SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
	// sqlBuilder.setTables(CacheData.TABLE_NAME);
	// sqlBuilder.appendWhere(CacheData.COLUMN_KIND+"='"+kind+"' AND ");
	// sqlBuilder.appendWhere(CacheData.COLUMN_FILTER+
	// (filter==null?" IS NULL":"='"+filter+"'")
	// );
	// sqlBuilder.appendWhere(" AND "+CacheData.COLUMN_EXPIRE_DATE+">"+new
	// Date().getTime()/1000);
	// Cursor cur = sqlBuilder.query(
	// zabbixLocalDB,
	// null,
	// null,
	// null,
	// null,
	// null,
	// null
	// );
	// Log.i("ZabbixContentProvider",
	// "isCached "+kind+"//"+filter+"//"+cur.moveToFirst());
	// return cur.moveToFirst();
	// }
	//
	// /**
	// * public, for the unit test
	// */
	// public void setCached(String kind, String filter, int lifetime) {
	// CacheData cache = new CacheData();
	// cache.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 +
	// lifetime);
	// cache.set(CacheData.COLUMN_KIND, kind);
	// if (filter != null) {
	// cache.set(CacheData.COLUMN_FILTER, filter);
	// }
	// cache.insert(zabbixLocalDB);
	// }

	// /**
	// * updates the progressbar in the gui
	// * @param i 0..100
	// */
	// public void showProgress(int i) {
	// // if applicable, transform the progress
	// if (transformProgressStart != 0 && transformProgressEnd != 0) {
	// i = (int) (transformProgressStart +
	// (transformProgressEnd-transformProgressStart)*(double)i/100);
	// }
	//
	// Intent intent = new
	// Intent(ZabbixContentProvider.CONTENT_PROVIDER_INTENT_ACTION);
	// intent.putExtra("flag", ZabbixContentProvider.INTENT_FLAG_SHOW_PROGRESS);
	// intent.putExtra("value", i);
	// context.sendBroadcast(intent);
	// }
	//
	// /**
	// * transforms the progress
	// * @param start 0% => start%
	// * @param end 100% => end%
	// */
	// public void transformProgress(int start, int end) {
	// transformProgressStart = start;
	// transformProgressEnd = end;
	// }

}
