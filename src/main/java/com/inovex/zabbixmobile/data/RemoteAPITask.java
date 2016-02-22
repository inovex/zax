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

package com.inovex.zabbixmobile.data;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.inovex.zabbixmobile.activities.BaseActivity;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.FatalException.Type;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;

import java.net.MalformedURLException;

/**
 * Represents an asynchronous Zabbix API call. This handles
 * {@link ZabbixLoginRequiredException} by retrying the API call and
 * {@link FatalException} by sending a broadcast containing the error message to
 * be displayed by the UI.
 * 
 */
public abstract class RemoteAPITask extends AsyncTask<Void, Integer, Void> {

	private static final String TAG = RemoteAPITask.class.getSimpleName();
	private final ZabbixRemoteAPI api;
	private Messenger messenger = null;

	public RemoteAPITask(ZabbixRemoteAPI api) {
		this.api = api;
		this.messenger = messenger;
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
				handleException(e1);
			}
		} catch (FatalException e) {
			handleException(e);
		}
		return null;
	}

	/**
	 * Sends a broadcast containing an exception's message resource ID.
	 * 
	 * @param exception
	 *            the exception
	 */
	private void handleException(FatalException exception) {
		if(exception.getType().equals(Type.HTTPS_CERTIFICATE_NOT_TRUSTED)){
			if(messenger != null){

				Message msg = Message.obtain(null, BaseActivity.MESSAGE_SSL_ERROR);
				msg.obj = exception.getCause();
				Bundle bundle = new Bundle();
				try {
					bundle.putString("url", api.buildZabbixUrl().toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				msg.setData(bundle);
				try {
					messenger.send(msg);
				} catch (RemoteException e) {
					// target doesn't exist anymore, TODO maybe use notification
				}
			} else {
				// no messenger, TODO maybe use notification
			}
		} else {
			// print stack trace to log
			exception.printStackTrace();
		}
	}

	/**
	 * Tries to authenticate the user and then retries the API call. Called when
	 * authentication fails (possibly because of an expired auth token).
	 * 
	 * @throws FatalException
	 *             thrown either when a {@link FatalException} occurs within the
	 *             API call or when a {@link ZabbixLoginRequiredException}
	 *             occurs.
	 */
	private void retry() throws FatalException {
		try {
			api.authenticate();
			executeTask();
		} catch (ZabbixLoginRequiredException e) {
			throw new FatalException(Type.ZABBIX_LOGIN_INCORRECT, e);
		}
	}

	/**
	 * This method contains the actual API call and has to be overridden by
	 * subclasses.
	 * 
	 * @throws ZabbixLoginRequiredException
	 * @throws FatalException
	 */
	protected abstract void executeTask() throws ZabbixLoginRequiredException,
			FatalException;

	public void updateProgress(Integer... values) {
		publishProgress(values);
	}

}
