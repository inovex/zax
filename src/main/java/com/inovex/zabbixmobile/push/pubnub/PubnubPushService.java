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

package com.inovex.zabbixmobile.push.pubnub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.NotificationService;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Push service maintaining the connection to Pubnub and showing notifications
 * when Pubnub sends data.
 *
 */
public class PubnubPushService extends Service {
	public static final String RINGTONE = "RINGTONE";
	public static final String PUBNUB_SUBSCRIBE_KEY = "PUBNUB_SUBSCRIBE_KEY";
	public static final String OLD_NOTIFICATION_ICONS = "OLD_NOTIFICATION_ICONS";

	String PUSHCHANNEL = "zabbixmobile";
	private static final String TAG = PubnubPushService.class.getSimpleName();
	private static AlarmManager am;
	Pubnub pubnub;
	PushListener mPushListener;
	private Handler handler;

	boolean initialConnect = true;
	private String subscribeKey;

	class PushListener extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				pubnub.subscribe(params[0], new Callback() {

					@Override
					public void successCallback(String channel, Object input) {
						Log.i("PushService", "execute");
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

								createNotification(status, message, triggerid);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void connectCallback(String channel, Object message) {
						Log.i(TAG,
								"connect to " + channel + ": "
										+ message.toString());
						if (initialConnect) {
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											PubnubPushService.this,
											PubnubPushService.this
													.getResources()
													.getString(
															R.string.push_connection_success),
											Toast.LENGTH_SHORT).show();
									initialConnect = false;
								}

							});
						}
					}

					@Override
					public void disconnectCallback(String channel,
							Object message) {
						Log.i(TAG,
								"disconnect to " + channel + ": "
										+ message.toString());
					}

					@Override
					public void errorCallback(String channel, PubnubError error) {
						Log.i(TAG,
								"error (" + channel + "): "
										+ error.getErrorString());
						if (initialConnect) {
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											PubnubPushService.this,
											PubnubPushService.this
													.getResources()
													.getString(
															R.string.push_connection_error),
											Toast.LENGTH_SHORT).show();
									initialConnect = false;
								}

							});

						}
					}

					@Override
					public void reconnectCallback(String channel, Object message) {
						Log.i(TAG,
								"reconnect to " + channel + ": "
										+ message.toString());
					}

				});
				Log.i(TAG, "subscribe");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Boolean.TRUE; // Return your real result here
		}

		private void createNotification(String status, String message, Long triggerid) {
			Intent intent = new Intent(getApplicationContext(),NotificationService.class);
			intent.putExtra("status",status);
			intent.putExtra("message",message);
			intent.putExtra("triggerid",triggerid);
			startService(intent);
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "onCreate");
		this.handler = new Handler();
		if (mPushListener == null)
			mPushListener = new PushListener();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");

		subscribeKey = intent.getStringExtra(PUBNUB_SUBSCRIBE_KEY);
		if (subscribeKey == null)
			subscribeKey = "";

		pubnub = new Pubnub("", // PUBLISH_KEY
				subscribeKey, // SUBSCRIBE_KEY
				"", // SECRET_KEY
				"", // CIPHER_KEY
				false // SSL_ON?
		);

		if (mPushListener.getStatus() != AsyncTask.Status.RUNNING
				&& mPushListener.getStatus() != AsyncTask.Status.FINISHED) {
			mPushListener.execute(PUSHCHANNEL);
			Log.i("PushListener", "start");
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		mPushListener.cancel(true);
		pubnub.unsubscribe(PUSHCHANNEL);
	}

	/**
	 * This starts or stops the push service depending on the user's settings.
	 *
	 * @param context
	 */
	public static void startOrStopPushService(Context context) {
		// start the push receiver, if it is enabled
		ZaxPreferences preferences = ZaxPreferences.getInstance(context);
		boolean push = preferences.isPushEnabled();
		Intent intent = new Intent(context, PubnubPushService.class);

		intent.putExtra(PUBNUB_SUBSCRIBE_KEY, preferences.getPushSubscribeKey());

		// alarm manager
		if (am == null)
			am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		if (push) {
			Log.d(TAG, "starting service");
			setRepeatingAlarm(pendingIntent);
			context.startService(intent);
		} else {
			Log.d(TAG, "stopping service");
			stopRepeatingAlarm(pendingIntent);
			context.stopService(intent);
		}

	}

	public static void killPushService(Context context) {
		Log.d(TAG, "stopping push service");
		Intent intent = new Intent(context, PubnubPushService.class);
		context.stopService(intent);
	}

	private static void setRepeatingAlarm(PendingIntent pendingIntent) {
		Log.d("PushServiceAlarm", "setRepeatingAlarm");

		// cancel old alarm
		am.cancel(pendingIntent);

		// wake up every 60 minutes to ensure service stays alive
		int alarmFrequency = 60 * 60 * 1000;
		// start service after one minute to avoid wasting precious CPU time
		// after device boot
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 1 * 60 * 1000, alarmFrequency,
				pendingIntent);
	}

	private static void stopRepeatingAlarm(PendingIntent pendingIntent) {
		Log.d("PushServiceAlarm", "stopRepeatingAlarm");
		am.cancel(pendingIntent);
	}

}