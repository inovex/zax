package com.inovex.zabbixmobile.view;

import android.content.Context;
import android.util.AttributeSet;

import com.inovex.zabbixmobile.R;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * This class is only used to enable a custom style attribute for this indicator
 * which can be customized in themes. It does not provide any additional
 * functionality.
 * 
 */
public class SeverityDetailsTitlePageIndicator extends TitlePageIndicator {

	public SeverityDetailsTitlePageIndicator(Context context) {
		super(context);
	}

	public SeverityDetailsTitlePageIndicator(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, R.attr.severityDetailsTitlePageIndicator);
	}

	public SeverityDetailsTitlePageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.severityDetailsTitlePageIndicator);
	}

}
