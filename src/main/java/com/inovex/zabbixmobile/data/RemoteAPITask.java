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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
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
	private Context context;
	private FatalException ex = null;

	public RemoteAPITask(ZabbixRemoteAPI api, Context context) {
		this.api = api;
		this.context = context;
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
		if(!exception.getType().equals(Type.NO_API_ACCESS)){
			if(exception.getCause() != null){
				exception.getCause().printStackTrace();
			} else {
				exception.printStackTrace();
			}
		} else {
			ex = exception;
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

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		String errorDescription = null;
		if(ex != null) {
			switch (ex.getType()){
				case NO_API_ACCESS:
					errorDescription = context.getResources().getString(R.string.exc_no_api_access);
					break;
				case ACCOUNT_BLOCKED:
					errorDescription = context.getResources().getString(R.string.exc_account_blocked);
					break;
				case HTTP_AUTHORIZATION_REQUIRED:
					errorDescription = context.getResources().getString(R.string.exc_http_auth_required);
					break;
				case SERVER_NOT_FOUND:
					errorDescription = context.getResources().getString(R.string.exc_not_found);
					break;
				case ZABBIX_LOGIN_INCORRECT:
					errorDescription = context.getResources().getString(R.string.exc_login_incorrect);
					break;
				case PRECONDITION_FAILED:
					errorDescription = context.getResources().getString(R.string.exc_precondition_failed);
					break;
			}
			if(errorDescription != null){
				Toast.makeText(context, errorDescription, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
