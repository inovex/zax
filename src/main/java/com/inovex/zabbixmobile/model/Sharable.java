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

package com.inovex.zabbixmobile.model;

import android.content.Context;

/**
 * Instances of classes implementing this interface may be shared as plain text
 * using Android's share functionalities.
 */
public interface Sharable {

	/**
	 * Return the string to be used for sharing an object's contents with other
	 * applications.
	 * 
	 * @param context
	 *            the app's context; used to resolve constant string resources
	 * @return string to be shared
	 */
	public String getSharableString(Context context);
}
