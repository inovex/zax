package com.inovex.zabbixmobile.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxServerPreferences;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

public class PubnubConnection {
	private static final String PUSHCHANNEL = "zabbixmobile";
	private static final String TAG = "PubnubConnection";

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

								int notIcon;
								if (status != null && status.equals("OK")) {
									notIcon = R.drawable.ok;
								} else if (status != null
										&& status.equals("PROBLEM")) {
									notIcon = R.drawable.problem;
								} else {
									notIcon = R.drawable.icon;
								}
								String notMessage;
								if (message != null && message.length() > 0) {
									notMessage = message;
								} else {
									notMessage = jsonObj.toString();
								}

								mContext.showNotification(message, ringtone, notMessage, notIcon);
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
							mContext.showConnectionSuccess();
							initialConnect = false;
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
							mContext.showConnectionError();
							initialConnect = false;
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
	}


	private Pubnub pubnub;
	private final PushListener mPushListener;

	private final String subscribeKey;
	private final String ringtone;
	private final PushService mContext;

	boolean initialConnect = true;

	public PubnubConnection(PushService context, long zabbixServerId) {
		mContext = context;

		ZaxServerPreferences prefs = new ZaxServerPreferences(context, zabbixServerId, true);
		subscribeKey = prefs.getPushSubscribeKey();
		ringtone = prefs.getPushRingtone();

		Log.d(TAG, "ringtone = "+ringtone);

		mPushListener = new PushListener();
		connect();
	}

	private void connect() {
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
	}

	public void refresh() {
		if (pubnub != null) {
			// continue / reconnect
			pubnub.disconnectAndResubscribe();
		} else {
			connect();
		}
	}

	public void disconnect() {
		mPushListener.cancel(true);
		pubnub.unsubscribe(PUSHCHANNEL);
	}
}
