/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.data;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;

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
import com.inovex.zabbixmobile.model.ZaxServerPreferences;
import com.inovex.zabbixmobile.util.JsonArrayOrObjectReader;
import com.inovex.zabbixmobile.util.JsonObjectReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private boolean useCustomKeystore = false;
	private Authenticator httpAuthenticator = null;

	public String getAuthenticationMethod() {
		if(mServerPreferences.getZabbixAPIVersion().isGreater2_3()){
			return "user.login";
		}
		return "user.authenticate";
	}

	/**
	 * global constants
	 */
	public class ZabbixConfig {
		public static final int APPLICATION_GET_LIMIT = 1000;
		public static final int EVENTS_GET_LIMIT = 60;
		public static final int HISTORY_GET_TIME_FROM_SHIFT = 24 * 60 * 60; // -24h
		public static final int HISTORY_GET_LIMIT = 8000;
		public static final int ITEM_GET_LIMIT = 200;
		public static final int TRIGGER_GET_LIMIT = 100;
		public static final int EVENT_GET_TIME_FROM_SHIFT = 7 * 24 * 60 * 60; // -7 days
		public static final int CACHE_LIFETIME_APPLICATIONS = 2 * 24 * 60 * 60; // 2 days
		public static final int CACHE_LIFETIME_EVENTS = 120;
		public static final int CACHE_LIFETIME_HISTORY_DETAILS = 4 * 60;
		public static final int CACHE_LIFETIME_HOST_GROUPS = 7 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_HOSTS = 2 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_SCREENS = 2 * 24 * 60 * 60;
		public static final int CACHE_LIFETIME_ITEMS = 4 * 60;
		public static final int CACHE_LIFETIME_TRIGGERS = 2 * 60;
		public static final long STATUS_SHOW_TRIGGER_TIME = 30 * 24 * 60 * 60;
		public static final int HTTP_CONNECTION_TIMEOUT = 10000;
		public static final int HTTP_SOCKET_TIMEOUT = 30000;
	}
	private final DatabaseHelper databaseHelper;

	private final ZaxPreferences mZaxPreferences;
	private final ZaxServerPreferences mServerPreferences;
	private final Context mContext;
	/**
	 * The API version. From 1.8.3 (maybe earlier) to 2.0 (excluded), this was
	 * 1.3. With 2.0, it changed to 1.4. Finally, since 2.0.4, the API version
	 * matches the program version.
	 */
	private String apiVersion = "";
	private final long mCurrentZabbixServerId;
	private String mZabbixAuthToken;

	/**
	 * init
	 *
	 * @param context
	 *            android context
	 * @param databaseHelper
	 *            OrmLite database helper
	 */
	public ZabbixRemoteAPI(Context context, DatabaseHelper databaseHelper, long zabbixServerId,
			ZaxServerPreferences prefsMock) {

		mCurrentZabbixServerId = zabbixServerId;

		if (prefsMock != null) {
			mServerPreferences = prefsMock;
		} else {
			mServerPreferences = new ZaxServerPreferences(context, zabbixServerId, true);
		}

		mZaxPreferences = ZaxPreferences.getInstance(context);

		// if applicable http auth
		try {
			if (mServerPreferences.isHttpAuthEnabled()) {
				final String user = mServerPreferences.getHttpAuthUsername();
				final String password = mServerPreferences.getHttpAuthPassword();
				Authenticator.setDefault(new Authenticator(){
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, password.toCharArray());
					}
				});
			}
		} catch (java.lang.UnsupportedOperationException e1) {
			// for unit test
		}

		this.mContext = context;
		this.databaseHelper = databaseHelper;
	}

	protected void initConnection() {
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
			throws IOException, JSONException, ZabbixLoginRequiredException, FatalException {
		HttpURLConnection connection = init_query(method, params, "_queryBuffer: ");

		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;
		while ((inputStr = br.readLine()) != null){
			responseStrBuilder.append(inputStr);
		}
		JSONObject result = new JSONObject(responseStrBuilder.toString());
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
	}

	@NonNull
	private HttpURLConnection init_query(String method, JSONObject params, String queryType) throws FatalException, IOException, JSONException {
		URL zabbixUrl = buildZabbixUrl();
		validateZabbixUrl(zabbixUrl);
		HttpURLConnection connection;
		connection = (HttpURLConnection) zabbixUrl.openConnection();
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setDoOutput(true);
		connection.setDoInput(true);

		JSONObject json = new JSONObject()
				.put("jsonrpc", "2.0")
				.put("method", method)
				.put("params", params)
				.put("id", 0);
		if(!method.equals("apiinfo.version")){
			json.put("auth", mZabbixAuthToken);
		}

		OutputStreamWriter outputStreamWriter
				= new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
		outputStreamWriter.write(json.toString());
		outputStreamWriter.close();

		Log.d(TAG, queryType + zabbixUrl);
		Log.d(TAG, queryType + json);

		int responseCode = connection.getResponseCode();
		checkHttpStatusCode(responseCode);
		return connection;
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
	private JsonArrayOrObjectReader _queryStream(String method, JSONObject params)
			throws JSONException, IOException, ZabbixLoginRequiredException, FatalException {
		HttpURLConnection connection = init_query(method, params, "_queryStream: ");

		JsonFactory jsonFac = new JsonFactory();
		JsonParser jp = jsonFac.createParser(connection.getInputStream());
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
					throw new FatalException(Type.INTERNAL_ERROR,errortxt);
				}
			}
		} while (!jp.getCurrentName().equals("result"));

		// result array found
		if (jp.nextToken() != JsonToken.START_ARRAY
				&& jp.getCurrentToken() != JsonToken.START_OBJECT) { // go inside the array
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
	private void checkHttpStatusCode(int resp) throws FatalException {
		if (resp == 401) {
			// http auth failed
			throw new FatalException(Type.HTTP_AUTHORIZATION_REQUIRED);
		} else if (resp == 412) {
			// Precondition failed / Looks like Zabbix 1.8.2
			throw new FatalException(Type.PRECONDITION_FAILED);
		} else if (resp == 404) {
			// file not found
			throw new FatalException(Type.SERVER_NOT_FOUND, resp
					+ " 404");
		} else {
			Log.d(TAG, "HTTP Status " + resp);
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
		String user = mServerPreferences.getUsername().trim();
		String password = mServerPreferences.getPassword();
		// String url = "http://10.10.0.21/zabbix";
		// String user = "admin";
		// String password = "zabbix";

		//Log.d(TAG, "u:"+user+"/"+password);

		// get API version
		JSONObject result;
		try {
			result = _queryBuffer("apiinfo.version", new JSONObject());
			if (result == null) {
				mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_1_3);
			} else {
				apiVersion = result.getString("result");
				if(apiVersion.equals("1.4")){
					mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_1_4);
				} else if(apiVersion.startsWith("2.4")){
					mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_2_4);
				}else if(apiVersion.startsWith("2")) {
					mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_2_0_TO_2_3);
				} else if (apiVersion.startsWith("3") || Integer.getInteger(apiVersion.substring(0,0)) >=3 ){
					mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_GT_3);
				} else{
					mServerPreferences.setZabbixAPIVersion(ZabbixAPIVersion.API_1_3);
				}
			}
			Log.i(TAG, "Zabbix API Version: " + apiVersion);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		String token = null;
		try {
			String method = getAuthenticationMethod();
			result = _queryBuffer(method,
					new JSONObject().put("user", user)
							.put("password", password));
			token = result.getString("result");
		} catch (JSONException e) {
			// there's no result
			e.printStackTrace();
		} catch (RuntimeException e) {
			// wrong password. token remains null
			e.printStackTrace();
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}
		if (token != null) {
			// persist token
			mZabbixAuthToken = token;
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

			params.put("countOutput", 1);
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
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectHosts"
							: "select_hosts", "refer").put("source", 0);
			if (!mServerPreferences.getZabbixAPIVersion().isGreater1_4()) {
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
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	private Collection<Application> importApplicationsFromStream(
			JsonArrayOrObjectReader jsonReader, RemoteAPITask task,
			int numApplications) throws JsonParseException,
			NumberFormatException, IOException, ZabbixLoginRequiredException,
			FatalException {
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
							if (h.getName() == null) {
								importHostsAndGroups();
								databaseHelper.refreshHosts(hosts);
							}
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

			if (numApplications > 0)
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
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectHosts"
							: "select_hosts", "refer")
					.put("source", 0)
					// sorting by clock is not possible, hence we sort by event
					// ID to get the newest events
					.put("sortfield", "eventid")
					.put("sortorder", "DESC")
					.put("time_from",
							(new Date().getTime() / 1000)
									- ZabbixConfig.EVENT_GET_TIME_FROM_SHIFT);
			if(!mServerPreferences.getZabbixAPIVersion().isGreater2_3()){
				// in Zabbix version >= 2.4 select_triggers is deprecated
				params.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectTriggers"
					: "select_triggers", "extend");
			}
			if (!mServerPreferences.getZabbixAPIVersion().isGreater1_4()) {
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
	 * {@link ZabbixRemoteAPI#importTriggersByIds(java.util.Collection, boolean, RemoteAPITask)} to
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
					for (Host h : hosts) {
						if (h.getName() == null) {
							importHostsAndGroups();
							databaseHelper.refreshHosts(hosts);
						}
					}
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
					long objectId = Long.parseLong(eventReader.getText());
					e.setObjectId(objectId);
					// in API version 2.4 triggers are no longer sent with the event
					// -> the trigger ID (if available) is given in the field object ID, so we use
					// that one
					// Note: we should actually check the field object for the type of referenced
					// object (https://www.zabbix.com/documentation/2.4/manual/api/reference/event/object),
					// but since the IDs are unique and this would be a major inconvenience using
					// the currently used method of JSON parsing, we keep it simple for now.
					if(mServerPreferences.getZabbixAPIVersion().isGreater2_3()){
						Trigger t = new Trigger();
						t.setId(objectId);
						e.setTrigger(t);
						triggerIds.add(objectId);
					}
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
			if (task != null && numEvents > 0)
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

						HistoryDetail h = new HistoryDetail();
						while (historydetail.nextValueToken()) {
							String propName = historydetail.getCurrentName();
							switch (propName) {
								case HistoryDetail.COLUMN_CLOCK:
									// The unit of Zabbix timestamps is seconds, we
									// need milliseconds
									h.setClock(Long.parseLong(historydetail
											.getText()) * 1000);
									break;
								case HistoryDetail.COLUMN_ITEMID:
									h.setItemId(Long.parseLong(historydetail
											.getText()));
									break;
								case HistoryDetail.COLUMN_VALUE:
									h.setValue(Double.parseDouble(historydetail
											.getText()));
									break;
								default:
									historydetail.nextProperty();
									break;
							}
						}
						historyDetailsCollection.add(h);
						if (historyDetailsCollection.size() >= RECORDS_PER_INSERT_BATCH) {
							databaseHelper
									.insertHistoryDetails(historyDetailsCollection);
							historyDetailsCollection.clear();
						}
						if (task != null && numDetails > 0)
							task.updateProgress(Math.min(selI * 100
									/ numDetails, 84));
					}
					Log.d(TAG, "itemID " + itemId + ": imported " + (selI)
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
	 * @param insertHostGroups
	 * @return list of host groups parsed from jsonReader
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private List<HostGroup> importHostGroupsFromStream(
			JsonArrayOrObjectReader jsonReader, boolean insertHostGroups)
			throws JsonParseException, IOException {
		long firstHostGroupId = -1;
		ArrayList<HostGroup> hostGroupCollection = new ArrayList<HostGroup>();
		JsonObjectReader hostReader;
		while ((hostReader = jsonReader.next()) != null) {
			HostGroup h = new HostGroup();
			h.setZabbixServerId(mCurrentZabbixServerId);
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
			if (insertHostGroups
					&& hostGroupCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertHostGroups(hostGroupCollection);
				hostGroupCollection.clear();
			}
		}
		if (insertHostGroups) {
			databaseHelper.insertHostGroups(hostGroupCollection);
			Log.d(TAG, "host groups inserted");
		}

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
					List<HostGroup> groups = importHostGroupsFromStream(
							hostReader.getJsonArrayOrObjectReader(), true);
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
			// multiple database operations on the hosts table
			synchronized (databaseHelper.getDao(Host.class)) {
				databaseHelper.clearHosts();
				databaseHelper.clearHostGroups();
				JsonArrayOrObjectReader hosts = _queryStream(
						"host.get",
						new JSONObject()
								.put("output", "extend")
								.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectGroups"
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
					if (mServerPreferences.getZabbixAPIVersion().isGreater1_4()
							&& propName.equals(Item.COLUMN_DESCRIPTION_V2)) {
						item.setDescription(itemReader.getText());
					} else if (!mServerPreferences.getZabbixAPIVersion().isGreater1_4()) {
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
			if (description != null && description.matches(".*\\$[0-9].*")) {
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
			if (task != null && numItems > 0)
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
			params.put("countOutput", 1);
			if (hostId != null)
				params.put("hostids", new JSONArray().put(hostId));

			JSONObject result = _queryBuffer("item.get", params);
			// Zabbix does not support limit when countOutput is set
			int numItems = Math.min(ZabbixConfig.ITEM_GET_LIMIT,
					getOutputCount(result));

			params = new JSONObject();
			params.put("output", "extend")
					.put("limit", ZabbixConfig.ITEM_GET_LIMIT)
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectApplications"
							: "select_applications", "refer");
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

	private ArrayList<ScreenItem> parseScreenItemsFromStream(JsonArrayOrObjectReader jsonReader)
			throws JsonParseException, NumberFormatException, IOException {
		JsonObjectReader screenItemReader;

		ArrayList<ScreenItem> screenItemsCollection = new ArrayList<ScreenItem>();
		while ((screenItemReader = jsonReader.next()) != null) {
			ScreenItem screenItem = new ScreenItem();
			int resourcetype = -1;
			while (screenItemReader.nextValueToken()) {
				String propName = screenItemReader.getCurrentName();
				if (propName.equals(ScreenItem.COLUMN_SCREENITEMID)) {
					screenItem
							.setScreenItemId(Long.parseLong(screenItemReader.getText()));
				} else if (propName.equals(ScreenItem.COLUMN_SCREENID)) {
					screenItem.setScreenId(Long.parseLong(screenItemReader
							.getText()));
				} else if (propName.equals(ScreenItem.COLUMN_RESOURCEID)) {
					screenItem.setResourceId(Long.parseLong(screenItemReader
							.getText()));
				} else if (propName.equals(ScreenItem.COLUMN_REAL_RESOURCEID)) {
					screenItem.setRealResourceId(Long.parseLong(screenItemReader
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
		}

		return screenItemsCollection;
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
			params.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectScreenItems"
					: "select_screenitems", "extend");

			// screens
			jsonReader = _queryStream((mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "s"
					: "S") + "creen.get", params);
			if(jsonReader != null) {
				importScreensFromReader(jsonReader);
				jsonReader.close();
			}

			// template screens (only possible for Zabbix 2.0 and above
			if(mServerPreferences.getZabbixAPIVersion().isGreater1_4()) {
				List<Host> hosts = databaseHelper
						.getHostsByHostGroup(HostGroup.GROUP_ID_ALL);
				List<Long> hostIds = new ArrayList<Long>();
				for(Host host : hosts) {
					hostIds.add(host.getId());
				}
				params.put("hostids", new JSONArray(hostIds));
				jsonReader = _queryStream("templatescreen.get", params);
				if(jsonReader != null) {
					importScreensFromReader(jsonReader);
					jsonReader.close();
				}
			}

		} catch (JSONException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		} catch (IOException e) {
			throw new FatalException(Type.INTERNAL_ERROR, e);
		}

		databaseHelper.setCached(CacheDataType.SCREEN, null);
	}

	private void importScreensFromReader(JsonArrayOrObjectReader jsonReader) throws IOException {
		JsonObjectReader screenReader;
		ArrayList<Screen> screensCollection = new ArrayList<Screen>(
				RECORDS_PER_INSERT_BATCH);
		ArrayList<Screen> screensComplete = new ArrayList<Screen>();
		while ((screenReader = jsonReader.next()) != null) {
			List<ScreenItem> screenItemsCollection = null;
			Screen screen = new Screen();
			screen.setZabbixServerId(mCurrentZabbixServerId);
			while (screenReader.nextValueToken()) {
				String propName = screenReader.getCurrentName();
				if (propName.equals(Screen.COLUMN_SCREENID)) {
					screen.setScreenId(Long.parseLong(screenReader.getText()));
				} else if (propName.equals(Screen.COLUMN_NAME)) {
					screen.setName(screenReader.getText());
				} else if (propName.equals("screenitems")) {
					screenItemsCollection = parseScreenItemsFromStream(screenReader
							.getJsonArrayOrObjectReader());
				} else if(propName.equals("hostid")) {
					screen.setHost(databaseHelper.getHostById(Long.parseLong(screenReader.getText())));
				} else {
					screenReader.nextProperty();
				}
			}
			if(screenItemsCollection != null) {
				if(screen.getHost() != null) {
					for (ScreenItem screenItem : screenItemsCollection) {
						screenItem.setHost(screen.getHost());
					}
				}
				databaseHelper.insertScreenItems(screenItemsCollection);
			}
			screensCollection.add(screen);
			screensComplete.add(screen);
			if (screensCollection.size() >= RECORDS_PER_INSERT_BATCH) {
				databaseHelper.insertScreens(screensCollection);
				screensCollection.clear();
			}
		}
		databaseHelper.insertScreens(screensCollection);
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
							.put(mServerPreferences.getZabbixAPIVersion()
									.isGreater1_4() ? "selectGraphItems"
									: "select_graph_items", "extend")
							.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectItems"
									: "select_items", "extend")
							.put("output", "extend")
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
	 * cached, the method {@link ZabbixRemoteAPI#importEvents(RemoteAPITask)}} does not call
	 * this method at all. Hence the caching of the corresponding triggers is
	 * directly linked to caching of the events (just like triggers are linked
	 * to events in the data model).
	 *
	 * 2. Import of all active triggers. The method
	 * {@link ZabbixRemoteAPI#importActiveTriggers(RemoteAPITask)} takes care of caching by
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
			params.put("countOutput", 1).put("lastChangeSince", min)
					.put("expandDescription", true);
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
					// TODO: selectHosts is deprecated since 2.4
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectHosts"
							: "select_hosts", "refer")
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectGroups"
							: "select_groups", "refer")
					.put(mServerPreferences.getZabbixAPIVersion().isGreater1_4() ? "selectItems"
							: "select_items", "extend")
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
	 * @throws FatalException
	 * @throws ZabbixLoginRequiredException
	 */
	private List<Trigger> importTriggersFromStream(
			JsonArrayOrObjectReader jsonReader, int numTriggers,
			RemoteAPITask task) throws JsonParseException, IOException,
			ZabbixLoginRequiredException, FatalException {
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
						if (host.getName() == null) {
							importHostsAndGroups();
							databaseHelper.refreshHosts(hosts);
						}
						if (host.getStatus() != Host.STATUS_MONITORED)
							enabled = false;
					}
					String hostNames = createHostNamesString(hosts);
					// store hosts names
					t.setHostNames(hostNames);
				} else if (propName.equals("groups")) {
					List<HostGroup> hostGroups = importHostGroupsFromStream(
							triggerReader.getJsonArrayOrObjectReader(), false);
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
			if (task != null && numTriggers > 0)
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
	protected URL buildZabbixUrl() throws MalformedURLException {
		String url = mServerPreferences.getZabbixUrl().trim();
		String prefix = "";
		if (!url.startsWith("http"))
			prefix = "http://";
		String urlString = prefix + url + (url.endsWith("/") ? "" : '/')
				+ API_PHP;
		return new URL(urlString);
	}

	/**
	 * Validates the Zabbix URL.
	 *
	 * @throws FatalException
	 *             type SERVER_NOT_FOUND, if the URL is null or equal to the
	 *             default example URL
	 * @param zabbixUrl
	 */
	private void validateZabbixUrl(URL zabbixUrl) throws FatalException {
		String urlString = zabbixUrl.toString();
		String exampleUrl = mContext.getResources().getString(
				R.string.url_example)
				+ (mContext.getResources().getString(R.string.url_example)
						.endsWith("/") ? "" : '/') + API_PHP;
		if(!Patterns.WEB_URL.matcher(urlString).matches()
				|| urlString.equals(exampleUrl)
				|| urlString.startsWith("https:///") //for default url which is build together
				|| urlString.startsWith("http:///")) //for default url which is build together
		{
			throw new FatalException(Type.SERVER_NOT_FOUND);
		}
	}

	public boolean isLoggedIn() {
		return mZabbixAuthToken != null;
	}

	public void logout() {
		mZabbixAuthToken = null;
	}

	public long getZabbixSeverId() {
		return mCurrentZabbixServerId;
	}

}
