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
