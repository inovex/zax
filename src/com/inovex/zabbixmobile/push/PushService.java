package com.inovex.zabbixmobile.push;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inovex.zabbixmobile.GatewayActivity;
import com.inovex.zabbixmobile.R;

public class PushService extends Service {
	// Callback Interface when a channel is connected
	class ConnectCallback implements Callback {
		@Override
		public boolean execute(Object message) {
			Log.i("PushService", message.toString());
			return false;
		}
	}

	// Callback Interface when a channel is disconnected
	class DisconnectCallback implements Callback {
		@Override
		public boolean execute(Object message) {
			Log.i("PushService", message.toString());
			return false;
		}
	}
	// Callback Interface when error occurs
	class ErrorCallback implements Callback {
		@Override
		public boolean execute(Object message) {
			Log.i("PushService", message.toString());
			return false;
		}
	}
	class PushListener extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				HashMap<String, Object> args = new HashMap<String, Object>(2);
				args.put("channel", params[0]);
				args.put("callback", mPushReceiver);
				args.put("connect_cb", new ConnectCallback());			// callback to get connect event
				args.put("disconnect_cb", new DisconnectCallback());	// callback to get disconnect event
				args.put("reconnect_cb", new ReconnectCallback());		// callback to get reconnect event
				args.put("error_cb", new ErrorCallback());				// callback to get error event
				pubnub.subscribe(args);
				Log.i("PushService","subscribe");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Boolean.TRUE; // Return your real result here
		}
	}
	class PushReceiver implements Callback {
		@Override
		public boolean execute(Object input) {
			Log.i("PushService","execute");
			try {
				if (input instanceof JSONObject) {
					JSONObject jsonObj = (JSONObject) input;
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
					Intent notificationIntent = new Intent(PushService.this, GatewayActivity.class);
					notificationIntent.putExtra("pushNotificationTriggerid", triggerid);
					PendingIntent pendingIntent = PendingIntent.getActivity(PushService.this, uniqueRequestCode(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT );
					notification.setLatestEventInfo(PushService.this, "Zabbix Notification", message, pendingIntent);

					SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(PushService.this);
					String strRingtonePreference = preference.getString("zabbix_push_ringtone", null);
					if (strRingtonePreference != null) {
						notification.sound = Uri.parse(strRingtonePreference);
					}

					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(triggerid==null?0:(int) (long) triggerid, notification);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}
	}

	// Callback Interface when a channel is reconnected
	class ReconnectCallback implements Callback {
		@Override
		public boolean execute(Object message) {
			Log.i("PushService", message.toString());
			return false;
		}
	}

	private static int lastRequestCode = 0;
	String PUSHCHANNEL = "zabbixmobile";
	Pubnub pubnub;
	PushReceiver mPushReceiver = new PushReceiver();
	PushListener mPushListener = new PushListener();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String subscribe_key = prefs.getString("zabbix_push_subscribe_key", "").trim();

		pubnub = new Pubnub("", // PUBLISH_KEY
			subscribe_key, // SUBSCRIBE_KEY
			"", // SECRET_KEY
			"", // CIPHER_KEY
			false    // SSL_ON?
		);
		Log.i("PushService","create");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (mPushListener.getStatus() != AsyncTask.Status.RUNNING) {
			mPushListener.execute(PUSHCHANNEL);
			Log.i("PushService","start ");
		}
	}

	private int uniqueRequestCode() {
		return lastRequestCode++;
	}
}
