package com.inovex.zabbixmobile.api;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ZabbixService extends Service {

	private static final String TAG = ZabbixService.class.getSimpleName();
	// Binder given to clients
	private final IBinder mBinder = new ZabbixBinder();
	// Random number generator
	private final Random mGenerator = new Random();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class ZabbixBinder extends Binder {
		public ZabbixService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ZabbixService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,
				"Binder " + this.toString() + ": intent " + intent.toString()
						+ " bound.");
		return mBinder;
	}

	/** method for clients */
	public int getRandomNumber() {
		Log.d(TAG, "ZabbixService:getRandomNumber() [" + this.toString() + "]");
		return mGenerator.nextInt(100);
	}
}
