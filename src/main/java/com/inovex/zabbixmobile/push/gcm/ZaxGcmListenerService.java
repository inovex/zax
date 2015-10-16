package com.inovex.zabbixmobile.push.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by felix on 16/10/15.
 */
public class ZaxGcmListenerService extends GcmListenerService {

	private static final String TAG = "ZaxGcmListener";

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Log.d(TAG, "From: " + from);
		for(String s:data.keySet()){
			Log.d(TAG,s + ": " + data.get(s).toString());
		}
		//TODO build notification

	}
}
