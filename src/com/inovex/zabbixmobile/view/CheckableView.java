package com.inovex.zabbixmobile.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableView extends LinearLayout implements Checkable {

	private boolean checked;
	private List<Checkable> checkableViews;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CheckableView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public CheckableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public CheckableView(Context context) {
		super(context);
		init(null);
	}

	@Override
	public void setChecked(boolean checked) {
		this.checked = checked;
		for (Checkable c : checkableViews) {
			c.setChecked(checked);
		}
	}

	@Override
	public boolean isChecked() {

		return this.checked;
	}

	@Override
	public void toggle() {
		this.checked = !this.checked;
		for (Checkable c : checkableViews) {
			c.toggle();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final int childCount = this.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			findCheckableChildren(this.getChildAt(i));
		}
	}

	/**
	 * Read the custom XML attributes
	 */
	private void init(AttributeSet attrs) {
		this.checked = false;
		this.checkableViews = new ArrayList<Checkable>(5);
	}

	/**
	 * Add to our checkable list all the children of the view that implement the
	 * interface Checkable
	 */
	private void findCheckableChildren(View v) {
		if (v instanceof Checkable) {
			this.checkableViews.add((Checkable) v);
		}

		if (v instanceof ViewGroup) {
			final ViewGroup vg = (ViewGroup) v;
			final int childCount = vg.getChildCount();
			for (int i = 0; i < childCount; ++i) {
				findCheckableChildren(vg.getChildAt(i));
			}
		}
	}
}
