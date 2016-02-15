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
//		if (message == null) {
//			message = context.getString(R.string.exc_internal_error);
//		}
		if(message != null){
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

}