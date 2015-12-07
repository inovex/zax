package com.inovex.zabbixmobile.push.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.inovex.zabbixmobile.util.ssl.HttpsUtil;
import com.inovex.zabbixmobile.util.ssl.LocalKeyStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by felix on 16/10/15.
 */
public class RegistrationIntentService extends IntentService{
	private static final String TAG = "RegIntentService";

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 */
	public RegistrationIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String gcm_sender_id = sharedPreferences.getString("gcm_sender_id", "");
		InstanceID instanceID = InstanceID.getInstance(this);;

		switch(intent.getAction()){
			case "register":

				if(!gcm_sender_id.equals("")){
					try{
						String token = instanceID.getToken(
								gcm_sender_id,
								GoogleCloudMessaging.INSTANCE_ID_SCOPE,
								null);
						sendRegistrationToServer(token);
						sharedPreferences.edit().putBoolean("sent_token_to_server", true).apply();
					} catch (IOException e) {
						Log.d(TAG, "Registration failed", e);
						sharedPreferences.edit().putBoolean("sent_token_to_server", false).apply();
					}
				} else {
					handleMissingConfiguration("sender_id");
				}
				break;
			case "unregister":
				try {
					if(gcm_sender_id != null && gcm_sender_id.length() > 0){
						instanceID.deleteToken(gcm_sender_id, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
						sharedPreferences.edit().putBoolean("sent_token_to_server", false).apply();
					}
				} catch (IOException e) {
					Log.d(TAG,"unregister failed",e);
				}
				break;
		}
	}

	private void sendRegistrationToServer(String token) {
		Log.d(TAG, "GCM-token: " + token);

		LocalKeyStore.setKeyStoreDirectory(this.getBaseContext().getDir("keystore", MODE_PRIVATE).getAbsolutePath());

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String gcm_server_url = sharedPreferences.getString("gcm_server_url", "");
		if(gcm_server_url.equals("")){
			handleMissingConfiguration("server_url");
		} else {
			try {
				URL server_url = new URL(gcm_server_url);
				HttpURLConnection connection;
				if(server_url.getProtocol().equals("https")){
					connection = HttpsUtil.getHttpsUrlConnection(server_url,true);
				} else {
					connection = (HttpURLConnection) server_url.openConnection();
				}
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestMethod("POST");

				JSONObject requestBody = new JSONObject();
				requestBody.put("action", "register");
				requestBody.put("registrationID", token);

				try{
					OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
					writer.write(requestBody.toString());
					writer.close();

					int HttpResult = connection.getResponseCode();
					if(HttpResult == HttpURLConnection.HTTP_OK){
						BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
						StringBuilder responseStrBuilder = new StringBuilder();
						String inputStr;
						while ((inputStr = br.readLine()) != null){
							responseStrBuilder.append(inputStr);
						}
						JSONObject response = new JSONObject(responseStrBuilder.toString());
						Log.d(TAG,response.toString());
					}
				} catch (SSLHandshakeException e){
					// TODO create notification to inform user about allow ssl settings

 				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleMissingConfiguration(String cause) {
		switch (cause){
			case "sender_id":
				break;
			case "server_url":
				break;
		}
	}


}
