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
import android.util.Log;

import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.FatalException.Type;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;

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
				if(e.getCause() != null){
					e.getCause().printStackTrace();
				} else {
					e.printStackTrace();
				}
			}
		} catch (FatalException e) {
			if(e.getCause() != null){
				e.getCause().printStackTrace();
			} else {
				e.printStackTrace();
			}
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
