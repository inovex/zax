package com.inovex.zabbixmobile.activities.support;

import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.EventData;

public class DetailsEventFragmentSupport {
	private long lastEventId;
	private final Activity mActivity;
	private final ShowNoDataMessageSupport showNoDataMessageSupport;

	public DetailsEventFragmentSupport(Activity mActivity, ShowNoDataMessageSupport showNoDataMessageSupport) {
		this.mActivity = mActivity;
		this.showNoDataMessageSupport = showNoDataMessageSupport;
	}

	private View findViewById(Dialog dlg, int resId) {
		if (dlg != null) {
			return dlg.findViewById(resId);
		} else {
			return mActivity.findViewById(resId);
		}
	}

	public long getLastEventId() {
		return lastEventId;
	}

	/**
	 * set the content
	 * @param dlg [optional] if null, the views will be searched from activity, otherwise from dlg
	 * @param cursor the data
	 */
	public void setData(Dialog dlg, Cursor cursor) {
		if (cursor.moveToFirst()) {
			Date date = new Date(cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_CLOCK))*1000l);
			java.text.DateFormat df = DateFormat.getDateFormat(mActivity);
			java.text.DateFormat tf = DateFormat.getTimeFormat(mActivity);
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_time)).setText(df.format(date)+ " " +tf.format(date));
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_status)).setText(cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_VALUE))==0?R.string.ok:R.string.problem);
			((ImageView) findViewById(dlg, R.id.dlg_eventdetails_img_status)).setImageResource(cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_VALUE))==0?R.drawable.ok:R.drawable.problem);
			boolean ack = cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_ACK))!=0;
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_acknowledged)).setText(ack?R.string.yes:R.string.no);
			((ImageView) findViewById(dlg, R.id.dlg_eventdetails_img_acknowledged)).setImageResource(ack?R.drawable.ok:R.drawable.problem);

			findViewById(dlg, R.id.menuitem_acknowledge).setVisibility(View.VISIBLE);
			findViewById(dlg, R.id.menuitem_acknowledge).setEnabled(!ack);
			lastEventId = cursor.getLong(cursor.getColumnIndex(EventData.COLUMN_EVENTID));
		} else {
			showNoDataMessageSupport.showNoDataMessage(EventData.TABLE_NAME);
		}
	}
}
