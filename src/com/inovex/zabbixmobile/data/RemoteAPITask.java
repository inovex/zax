package com.inovex.zabbixmobile.data;

import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;

import android.os.AsyncTask;

public abstract class RemoteAPITask extends AsyncTask<Void, Void, Void> {
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			executeTask();
		} catch (ZabbixLoginRequiredException e) {
			// TODO try to relogin
			e.printStackTrace();
		} catch (FatalException e) {
			// TODO broadcast to UI
			e.printStackTrace();
		}
		return null;
	}

	protected abstract void executeTask() throws ZabbixLoginRequiredException,
			FatalException;

}
