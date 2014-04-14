package com.inovex.zabbixmobile.push;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.data.DatabaseHelper;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.model.ZaxServerPreferences;
import com.inovex.zabbixmobile.push.PubnubConnection.PushListener;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.pubnub.api.Pubnub;

/**
 * Push service maintaining the connection to Pubnub and showing notifications
 * when Pubnub sends data.
 *
 */
public class PushService extends Service {
	public static final String RINGTONE = "RINGTONE";
	public static final String PUBNUB_SUBSCRIBE_KEY = "PUBNUB_SUBSCRIBE_KEY";
	public static final String OLD_NOTIFICATION_ICONS = "OLD_NOTIFICATION_ICONS";

	protected static final int NUM_STACKED_NOTIFICATIONS = 5;
	public static final String ACTION_ZABBIX_NOTIFICATION = "com.inovex.zabbixmobile.push.PushService.ACTION_ZABBIX_NOTIFICATION";
	public static final String ACTION_ZABBIX_NOTIFICATION_DELETE = "com.inovex.zabbixmobile.push.PushService.ACTION_ZABBIX_NOTIFICATION_DELETE";
	private static final String TAG = PushService.class.getSimpleName();
	public static final String ZABBIXSERVER_ID = "ZABBIXSERVER_ID";
	public static final String STOP_PUSH_CONNECTION = "STOP_PUSH_CONNECTION";
	private static int lastRequestCode = 0;
	private static AlarmManager am;
	Pubnub pubnub;
	PushListener mPushListener;
	private BroadcastReceiver mNotificationBroadcastReceiver;
	private BroadcastReceiver mNotificationDeleteBroadcastReceiver;
	private Handler handler;

	int numNotifications = 0;
	ArrayBlockingQueue<CharSequence> previousMessages = new ArrayBlockingQueue<CharSequence>(
			NUM_STACKED_NOTIFICATIONS);

	private Map<Long, PubnubConnection> pubnubConnections;
	private boolean mOldNotificationIcons;


	/**
	 * This broadcast receiver reacts on a click on a notification by performing
	 * the following tasks:
	 *
	 * 1. Reset the notification numbers and previous messages.
	 *
	 * 2. Start the main activity.
	 *
	 */
	public class NotificationBroadcastReceiver extends BroadcastReceiver {

		public NotificationBroadcastReceiver() {
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			numNotifications = 0;
			previousMessages.clear();
			Intent notificationIntent = new Intent(context,
					ProblemsActivity.class);
			notificationIntent.putExtra(
					ProblemsActivity.ARG_START_FROM_NOTIFICATION, true);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(notificationIntent);
		}

	}

	/**
	 * This broadcast receiver reacts on dismissal of a notification.
	 *
	 * It resets the notification numbers and previous messages.
	 *
	 */
	public class NotificationDeleteReceiver extends BroadcastReceiver {

		public NotificationDeleteReceiver() {
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			numNotifications = 0;
			previousMessages.clear();
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

		// Register the notification broadcast receiver.
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ZABBIX_NOTIFICATION);
		mNotificationBroadcastReceiver = new NotificationBroadcastReceiver();
		registerReceiver(mNotificationBroadcastReceiver, filter);
		filter = new IntentFilter();
		filter.addAction(ACTION_ZABBIX_NOTIFICATION_DELETE);
		mNotificationDeleteBroadcastReceiver = new NotificationDeleteReceiver();
		registerReceiver(mNotificationDeleteBroadcastReceiver, filter);

		pubnubConnections = new HashMap<Long, PubnubConnection>();
	}

	public void showConnectionError() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(
						PushService.this,
						PushService.this
								.getResources()
								.getString(
										R.string.push_connection_error),
						Toast.LENGTH_SHORT).show();
			}

		});
	}

	public void showConnectionSuccess() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(
						PushService.this,
						PushService.this
								.getResources()
								.getString(
										R.string.push_connection_success),
						Toast.LENGTH_SHORT).show();
			}

		});

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");

		mOldNotificationIcons = ZaxPreferences.getInstance(getApplicationContext()).isOldNotificationIcons();

		long zabbixServerId = intent.getLongExtra(ZABBIXSERVER_ID, -1);

		// clean old connections if zabbix server gets removed
		if (intent.getBooleanExtra(STOP_PUSH_CONNECTION, false)) {
			if (pubnubConnections.containsKey(zabbixServerId)) {
				PubnubConnection p = pubnubConnections.get(zabbixServerId);
				p.disconnect();
				pubnubConnections.remove(p);
			}
			return START_REDELIVER_INTENT;
		}

		if (pubnubConnections.containsKey(zabbixServerId)) {
			pubnubConnections.get(zabbixServerId).refresh();
		} else {
			PubnubConnection con = new PubnubConnection(this, zabbixServerId);
			pubnubConnections.put(zabbixServerId, con);
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(mNotificationBroadcastReceiver);
		unregisterReceiver(mNotificationDeleteBroadcastReceiver);
	}

	private int uniqueRequestCode() {
		return lastRequestCode++;
	}

	/**
	 * This starts or stops the push service depending on the user's settings.
	 *
	 * @param context
	 */
	public static void startOrStopPushService(Context context, boolean ignoreAlarm, boolean reconnectAll) {
		// multiple servers
		DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context,
				DatabaseHelper.class);
		if (reconnectAll) {
			for (ZabbixServer srv : databaseHelper.getZabbixServers()) {
				Intent intent = new Intent(context, PushService.class);
				intent.putExtra(ZABBIXSERVER_ID, srv.getId());
				intent.putExtra(STOP_PUSH_CONNECTION, true);
				context.startService(intent);
			}
		}

		boolean anyPush = false;
		for (ZabbixServer srv : databaseHelper.getZabbixServers()) {
			ZaxServerPreferences preferences = new ZaxServerPreferences(context, srv.getId(), true);

			// start the push receiver, if it is enabled
			boolean push = preferences.isPushEnabled();
			anyPush |= push;

			Intent intent = new Intent(context, PushService.class);
			intent.putExtra(ZABBIXSERVER_ID, srv.getId());

			// alarm manager
			if (am == null)
				am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			Intent alarmIntent = new Intent(context, PushAlarm.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

			if (push) {
				Log.d(TAG, "starting service");
				if (!ignoreAlarm) setRepeatingAlarm(pendingIntent);
				context.startService(intent);
			} else {
				Log.d(TAG, "stopping repeating alarm");
				if (!ignoreAlarm) stopRepeatingAlarm(pendingIntent);
				intent.putExtra(STOP_PUSH_CONNECTION, true);
				context.startService(intent);
			}
		}
		if (!anyPush) {
			Log.d(TAG, "stopping push service");
			context.stopService(new Intent(context, PushService.class));
		}
	}

	public static void killPushService(Context context) {
		Log.d(TAG, "stopping push service");
		Intent intent = new Intent(context, PushService.class);
		context.stopService(intent);
	}

	private static void setRepeatingAlarm(PendingIntent pendingIntent) {
		Log.d("PushServiceAlarm", "setRepeatingAlarm");

		// cancel old alarm
		am.cancel(pendingIntent);

		// wake up every 60 minutes to ensure service stays alive
		long alarmFrequency = AlarmManager.INTERVAL_HALF_HOUR;
		//long alarmFrequency = 3*60*1000;
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

	public void showNotification(String message, String ringtone, String notMessage, int notIcon) {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this);
		notificationBuilder.setTicker(notMessage);
		notificationBuilder.setWhen(System
				.currentTimeMillis());

		if (mOldNotificationIcons) {
			notificationBuilder
					.setLargeIcon(BitmapFactory
							.decodeResource(
									getResources(),
									R.drawable.icon));
			notificationBuilder.setSmallIcon(notIcon);
		} else {
			notificationBuilder
					.setLargeIcon(BitmapFactory
							.decodeResource(
									getResources(),
									notIcon));
			notificationBuilder
					.setSmallIcon(R.drawable.icon);
		}

		// we do not start MainActivity directly, but
		// send a
		// broadcast which will be received by a
		// NotificationBroadcastReceiver which resets
		// the
		// notification status and starts MainActivity.
		Intent notificationIntent = new Intent();
		notificationIntent
				.setAction(ACTION_ZABBIX_NOTIFICATION);
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(
						this,
						uniqueRequestCode(),
						notificationIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);
		notificationBuilder
				.setContentTitle(getResources()
						.getString(
								R.string.notification_title));
		notificationBuilder.setContentText(message);
		notificationBuilder
				.setContentIntent(pendingIntent);
		notificationBuilder
				.setNumber(++numNotifications);

		notificationBuilder.setAutoCancel(true);
		notificationBuilder.setOnlyAlertOnce(false);

		if (previousMessages.size() == NUM_STACKED_NOTIFICATIONS)
			previousMessages.poll();
		previousMessages.offer(message);
		// if there are several notifications, we stack
		// them in the
		// extended view
		if (numNotifications > 1) {
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			// Sets a title for the Inbox style big view
			inboxStyle
					.setBigContentTitle(getResources()
							.getString(
									R.string.notification_title));
			// Moves events into the big view
			for (CharSequence prevMessage : previousMessages) {
				inboxStyle.addLine(prevMessage);
			}
			if (numNotifications > NUM_STACKED_NOTIFICATIONS) {
				inboxStyle
						.setSummaryText((numNotifications - NUM_STACKED_NOTIFICATIONS)
								+ " more");
			}
			// Moves the big view style object into the
			// notification
			// object.
			notificationBuilder.setStyle(inboxStyle);
		}

		if (ringtone != null) {
			notificationBuilder.setSound(Uri
					.parse(ringtone));
		}

		Notification notification = notificationBuilder
				.build();

		Intent notificationDeleteIntent = new Intent();
		notificationDeleteIntent
				.setAction(ACTION_ZABBIX_NOTIFICATION_DELETE);
		notification.deleteIntent = PendingIntent
				.getBroadcast(this, 0,
						notificationDeleteIntent, 0);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// We use the same ID because we want to stack
		// the notifications and we don't really care
		// about the trigger ID anyway (clicking the
		// notification just starts the main activity).
		mNotificationManager.notify(0, notification);
	}

}
