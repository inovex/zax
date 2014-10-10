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
