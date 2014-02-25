package com.inovex.zabbixmobile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.inovex.zabbixmobile.R;

public class FrameLayoutWithBorderAlternate extends FrameLayout {

	public FrameLayoutWithBorderAlternate(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, R.attr.viewWithBorderAlternate);
	}

	public FrameLayoutWithBorderAlternate(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.viewWithBorderAlternate);
	}

	public FrameLayoutWithBorderAlternate(Context context) {
		super(context);
	}

}
