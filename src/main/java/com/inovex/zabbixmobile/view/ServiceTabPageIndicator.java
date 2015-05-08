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

package com.inovex.zabbixmobile.view;

import android.content.Context;
import android.util.AttributeSet;

import com.viewpagerindicator.TabPageIndicator;

/**
 * This is similar to {@link TabPageIndicator} except it ignores the
 * IllegalStateException which is thrown when the indicator is used (in
 * {@link TabPageIndicator#onMeasure(int, int)} before its adapter has been set
 * up. In Zax, the adapter is usually set up when the service is connected as it
 * holds the adapter objects.
 * 
 */
public class ServiceTabPageIndicator extends TabPageIndicator {

	public ServiceTabPageIndicator(Context context) {
		super(context);
	}

	public ServiceTabPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setCurrentItem(int item) {
		try {
			super.setCurrentItem(item);
		} catch (IllegalStateException e) {
			// ignore
		}
	}

}
