package com.inovex.zabbixmobile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.inovex.zabbixmobile.R;

public class FrameLayoutWithBorder extends FrameLayout {

	public FrameLayoutWithBorder(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, R.attr.viewWithBorder);
	}

	public FrameLayoutWithBorder(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.viewWithBorder);
	}

	public FrameLayoutWithBorder(Context context) {
		super(context);
	}

}
