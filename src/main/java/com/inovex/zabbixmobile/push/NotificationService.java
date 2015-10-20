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

package com.inovex.zabbixmobile.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.model.ZaxPreferences;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by felix on 19/10/15.
 */
public class NotificationService extends Service {

	protected static final int NUM_STACKED_NOTIFICATIONS = 5;
	public static final String ACTION_ZABBIX_NOTIFICATION = "com.inovex.zabbixmobile.push.PushService.ACTION_ZABBIX_NOTIFICATION";
	public static final String ACTION_ZABBIX_NOTIFICATION_DELETE = "com.inovex.zabbixmobile.push.PushService.ACTION_ZABBIX_NOTIFICATION_DELETE";

	private BroadcastReceiver mNotificationBroadcastReceiver;
	private BroadcastReceiver mNotificationDeleteBroadcastReceiver;

	int numNotifications = 0;
	ArrayBlockingQueue<CharSequence> previousMessages = new ArrayBlockingQueue<CharSequence>(
			NUM_STACKED_NOTIFICATIONS);
	private boolean oldNotificationIcons;
	private int lastRequestCode = 0;
	private String ringtone;
	private String okRingtone;


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
			NotificationService.this.stopSelf();
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
			NotificationService.this.stopSelf();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String status, message, source;
		Long triggerid;

		status = intent.getStringExtra("status");
		message = intent.getStringExtra("message");
		triggerid = intent.getLongExtra("triggerid", 0);
		createNotification(status,message,triggerid);

		source = intent.getStringExtra("source");
		if ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) ) {
			logNotification(status, message, triggerid, source);
		}

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onCreate() {
		super.onCreate();

		ZaxPreferences preferences = ZaxPreferences.getInstance(getApplicationContext());
		oldNotificationIcons = preferences.isOldNotificationIcons();
		ringtone = preferences.getPushRingtone();
		okRingtone = preferences.getPushOkRingtone();


		// Register the notification broadcast receiver.
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ZABBIX_NOTIFICATION);
		mNotificationBroadcastReceiver = new NotificationBroadcastReceiver();
		try {
			registerReceiver(mNotificationBroadcastReceiver, filter);
		} catch (Exception e) {
		}
		filter = new IntentFilter();
		filter.addAction(ACTION_ZABBIX_NOTIFICATION_DELETE);
		mNotificationDeleteBroadcastReceiver = new NotificationDeleteReceiver();
		try {
			registerReceiver(mNotificationDeleteBroadcastReceiver, filter);
		} catch (Exception e) {
		}
	}

	@Override
	public void onDestroy() {
//		super.onDestroy();
		unregisterReceiver(mNotificationBroadcastReceiver);
		unregisterReceiver(mNotificationDeleteBroadcastReceiver);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void createNotification(String status, String message, Long triggerid) {
		int notIcon;
		if (status != null && status.equals("OK")) {
			notIcon = R.drawable.ok;
		} else if (status != null
				&& status.equals("PROBLEM")) {
			notIcon = R.drawable.problem;
		} else {
			notIcon = R.drawable.icon;
		}
		String tickerMessage;
		if (message != null && message.length() > 0) {
			tickerMessage = message;
		} else {
			return; // there is obviously no sensable message here
		}

		NotificationCompat.Builder notificationBuilder
				= new NotificationCompat.Builder(NotificationService.this);
		notificationBuilder.setTicker(tickerMessage);
		notificationBuilder.
				setWhen(System.currentTimeMillis());

		if (oldNotificationIcons) {
			notificationBuilder
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(), R.drawable.icon));
			notificationBuilder.setSmallIcon(notIcon);
		} else {
			notificationBuilder.setLargeIcon(
					BitmapFactory.decodeResource(getResources(), notIcon));
			notificationBuilder.setSmallIcon(R.drawable.icon);
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
		PendingIntent pendingIntent =
				PendingIntent.getBroadcast(
						NotificationService.this,
						uniqueRequestCode(),
						notificationIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);
		notificationBuilder.setContentTitle(getResources()
				.getString(
						R.string.notification_title));
		notificationBuilder.setContentText(message);
		notificationBuilder.setContentIntent(pendingIntent);
		notificationBuilder.setNumber(++numNotifications);

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

		if (status != null && status.equals("OK")) {
			if (okRingtone != null) {
				notificationBuilder.setSound(Uri.parse(okRingtone));
			}
		} else {
			if (ringtone != null) {
				notificationBuilder.setSound(Uri.parse(ringtone));
			}
		}

		Notification notification = notificationBuilder
				.build();

		Intent notificationDeleteIntent = new Intent();
		notificationDeleteIntent
				.setAction(ACTION_ZABBIX_NOTIFICATION_DELETE);
		notification.deleteIntent = PendingIntent
				.getBroadcast(NotificationService.this, 0,
						notificationDeleteIntent, 0);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// We use the same ID because we want to stack
		// the notifications and we don't really care
		// about the trigger ID anyway (clicking the
		// notification just starts the main activity).
		mNotificationManager.notify(0, notification);
	}

	private void logNotification(String status, String message, Long triggerid, String source) {
		// Logging incoming notifications for Debuggin
		// timestamp status message triggerid network
		try {
			File folder = new File(Environment.getExternalStorageDirectory() + "/zax");
			boolean var = false;
			if (!folder.exists()) {
				var = folder.mkdir();
			}
			final String filename = folder.toString() + "/" + "push_logs.csv";
			File csv = new File(filename);
			if(!csv.exists() || !csv.isFile()){
				csv.createNewFile();
			}
			FileWriter fw = new FileWriter(csv,true);
			String date = Calendar.getInstance().getTime().toString();
			fw.append(date);
			fw.append('\t');
			fw.append(source);
			fw.append('\t');
			fw.append(Long.toString(triggerid));
			fw.append('\t');
			fw.append(status);
			fw.append('\t');
			fw.append(message);
			fw.append('\t');

			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

			String net = null;
			switch (activeNetwork.getType()) {
				case ConnectivityManager.TYPE_WIFI:
					String ssid = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getSSID();
					net = "wifi - ssid: " + ssid;
					break;
				case ConnectivityManager.TYPE_MOBILE:
					net = "mobile";
					break;
				default:
					net = "other network type";
			}
			fw.append(net);
			fw.append("\t\n");
			fw.flush();
			fw.close();
			Log.d("PushService", "writing to logfile " + date + " " + status + " " + message + " " + net);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private int uniqueRequestCode() {
		return lastRequestCode++;
	}
}
