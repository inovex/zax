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
		if(!gcm_sender_id.equals("")){
			try{
				InstanceID instanceID = InstanceID.getInstance(this);
				String token = instanceID.getToken(
						gcm_sender_id,
						GoogleCloudMessaging.INSTANCE_ID_SCOPE,
						null);
				Log.i(TAG, "GCM Registration Token: " + token);

				sendRegistrationToServer(token);

				sharedPreferences.edit().putBoolean("sent_token_to_server",true).apply();
				sharedPreferences.edit().putString("gcm_token",token);
			} catch (IOException e) {
				Log.d(TAG, "Registration failed", e);
				sharedPreferences.edit().putBoolean("sent_token_to_server",false).apply();
			}
		} else {
			// TODO handle when server sender ID is not configured
		}
	}

	private void sendRegistrationToServer(String token) {
		//TODO send token to server
	}
}
