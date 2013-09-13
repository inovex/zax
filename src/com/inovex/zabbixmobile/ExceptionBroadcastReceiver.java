package com.inovex.zabbixmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * This broadcast receiver handles exceptions occurring in the communication
 * with the Zabbix server and displays a toast.
 * 
 */
public class ExceptionBroadcastReceiver extends BroadcastReceiver {

	public static String EXTRA_MESSAGE = "exception_message";

	@Override
	public void onReceive(Context context, Intent intent) {
		String message = intent.getStringExtra(EXTRA_MESSAGE);
		if (message == null) {
			message = context.getString(R.string.exc_internal_error);
		}
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

}