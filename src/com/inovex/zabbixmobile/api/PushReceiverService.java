package com.inovex.zabbixmobile.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.pubnub.Callback;
import org.pubnub.Pubnub;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inovex.zabbixmobile.GatewayActivity;
import com.inovex.zabbixmobile.R;

/**
 * push receiver service
 * this service receives pubnub push events.
 * Syntax:
 * {"triggerid": 1822, "message": "low disk space", "status": "OK"}
 *
 * Example:
 * curl http://pubsub.pubnub.com/publish/pub-xxxxxxx/sub-yyyy/0/zabbixmobile/0/%7B%22triggerid%22%3A%2015836%2C%20%22message%22%3A%20%22bla%20bla%22%2C%22status%22%3A%20%22PROBLEM%22%7D
 */
public class PushReceiverService extends Service {
	private Pubnub pubnub;
	private boolean stopped;
	private Thread mThread;
	private static int lastRequestCode = 0;

	private void messageReceived(JSONObject jsonObj) {
		String status = null, message = null;
		Long triggerid = null;

		try {
			status = jsonObj.getString("status");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			message = jsonObj.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			triggerid = jsonObj.getLong("triggerid");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		int notIcon;
		if (status != null && status.equals("OK")) {
			notIcon = R.drawable.ok;
		} else if (status != null && status.equals("PROBLEM")) {
			notIcon = R.drawable.problem;
		} else {
			notIcon = R.drawable.icon;
		}
		String notMessage;
		if (message != null && message.length()>0) {
			notMessage = message;
		} else {
			notMessage = jsonObj.toString();
		}

		Notification notification = new Notification(notIcon, notMessage, System.currentTimeMillis());
		Intent notificationIntent = new Intent(PushReceiverService.this, GatewayActivity.class);
		notificationIntent.putExtra("pushNotificationTriggerid", triggerid);
		PendingIntent pendingIntent = PendingIntent.getActivity(PushReceiverService.this, uniqueRequestCode(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT );
		notification.setLatestEventInfo(PushReceiverService.this, "Zabbix Notification", message, pendingIntent);

		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		String strRingtonePreference = preference.getString("zabbix_push_ringtone", null);
		if (strRingtonePreference != null) {
			notification.sound = Uri.parse(strRingtonePreference);
		}

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(triggerid==null?0:(int) (long) triggerid, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// we don't need this, because we use startService
		return null;
	}

	@Override
	public void onDestroy() {
		stopped = true;
		if (pubnub != null) pubnub.stopLoop();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startListening();
		return START_STICKY;
	}

	private void startListening() {
		if (mThread == null) {
			Log.i("PushReceiverService", "startListening...");

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String subscribe_key = prefs.getString("zabbix_push_subscribe_key", "").trim();

			mThread = new Thread() {
				@Override
				public void run() {
					pubnub = new Pubnub("", subscribe_key, "", false);
					final Callback rcv = new Callback() {
						@Override
						public boolean execute(JSONObject message) {
							if (stopped) return false;
							// check if the push notification is still enabled
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PushReceiverService.this);
							boolean active = prefs.getBoolean("zabbix_push_enabled", false);
							if (!active) return false;

							Log.i("PubNub", "message received: "+message);
							messageReceived(message);
							return true;
						}
					};
					pubnub.subscribe("zabbixmobile", rcv);

					// we are stopped
					if (!stopped) stopSelf();
				}
			};
			mThread.start();
		}
	}

	private int uniqueRequestCode() {
		return lastRequestCode++;
	}
}
