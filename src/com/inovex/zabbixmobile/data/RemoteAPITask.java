package com.inovex.zabbixmobile.data;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.inovex.zabbixmobile.ExceptionBroadcastReceiver;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.FatalException.Type;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;

public abstract class RemoteAPITask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = RemoteAPITask.class.getSimpleName();
	private final ZabbixRemoteAPI api;

	public RemoteAPITask(ZabbixRemoteAPI api) {
		this.api = api;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			executeTask();
		} catch (ZabbixLoginRequiredException e) {
			Log.w(TAG, "Login failed. Retrying...");
			try {
				retry();
			} catch (FatalException e1) {
				sendBroadcast(e1);
			}
		} catch (FatalException e) {
			sendBroadcast(e);
		}
		return null;
	}

	private void sendBroadcast(FatalException exception) {
		// send broadcast with message depending on the type of exception
		Context context = api.getContext();
		Intent intent = new Intent();
		intent.setAction("com.inovex.zabbixmobile.EXCEPTION");
		intent.putExtra(ExceptionBroadcastReceiver.EXTRA_MESSAGE,
				context.getString(exception.getMessageResourceId()));
		context.sendBroadcast(intent);
		// print stack trace to log
		exception.printStackTrace();
	}

	private void retry() throws FatalException {
		try {
			api.authenticate();
			executeTask();
		} catch (ZabbixLoginRequiredException e) {
			throw new FatalException(Type.ZABBIX_LOGIN_INCORRECT, e);
		}
	}

	protected abstract void executeTask() throws ZabbixLoginRequiredException,
			FatalException;

}
