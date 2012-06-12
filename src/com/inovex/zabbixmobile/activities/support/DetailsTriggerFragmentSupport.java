package com.inovex.zabbixmobile.activities.support;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.MainActivitySmartphone;
import com.inovex.zabbixmobile.model.HostData;
import com.inovex.zabbixmobile.model.TriggerData;

public class DetailsTriggerFragmentSupport {
	private final Activity mActivity;
	private final ShowNoDataMessageSupport showNoDataMessageSupport;

	public DetailsTriggerFragmentSupport(Activity mActivity, ShowNoDataMessageSupport showNoDataMessageSupport) {
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

	public void setData(Dialog dlg, Cursor cursor) {
		if (cursor.moveToFirst()) {
			int host_idx = cursor.getColumnIndex(TriggerData.COLUMN_HOSTS);
			if (host_idx == -1 || cursor.getString(host_idx) == null || cursor.getString(host_idx).length() == 0) {
				host_idx = cursor.getColumnIndex("hosts_trigger");
				if (host_idx == -1 || cursor.getString(host_idx) == null || cursor.getString(host_idx).length() == 0) {
					host_idx = cursor.getColumnIndex(HostData.COLUMN_HOST);
				}
			}
			String hosts = cursor.getString(host_idx);
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_host)).setText(hosts);
			String desc = cursor.getString(cursor.getColumnIndex(TriggerData.COLUMN_DESCRIPTION));
			if (hosts != null) {
				desc = desc.replace("{HOSTNAME}", hosts);
			}
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_trigger)).setText(desc);
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_severity)).setText(MainActivitySmartphone.getTriggerPriorityText(mActivity, cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY))));
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_expression)).setText(cursor.getString(cursor.getColumnIndex(TriggerData.COLUMN_EXPRESSION)));
			((TextView) findViewById(dlg, R.id.dlg_eventdetails_tv_disabled)).setText(cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_STATUS))==0?R.string.no:R.string.yes);
		} else {
			showNoDataMessageSupport.showNoDataMessage(TriggerData.TABLE_NAME);
		}
	}
}
