package com.inovex.zabbixmobile.push.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

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
						Log.i(TAG, "GCM Registration Token: " + token);

						sendRegistrationToServer(token);

						SharedPreferences.Editor edit = sharedPreferences.edit();
						edit.putBoolean("sent_token_to_server", true);
//						edit.putString("gcm_token",token);
						edit.apply();
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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String gcm_server_url = sharedPreferences.getString("gcm_server_url", "");
		if(gcm_server_url.equals("")){
			handleMissingConfiguration("server_url");
		} else {
			//TODO send token to server
		}
	}

	private void handleMissingConfiguration(String cause) {

	}
}
