package com.inovex.zabbixmobile.activities.support;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;
import com.inovex.zabbixmobile.model.BaseModelData;
import com.inovex.zabbixmobile.model.HostData;
import com.inovex.zabbixmobile.model.HostGroupData;
import com.inovex.zabbixmobile.model.TriggerData;

public class HostListFragmentSupport {
	private final Activity mActivity;

	public HostListFragmentSupport(Activity activity) {
		mActivity = activity;
	}

	public void setupLists(final ListView listHosts, final ListView listHostGroups) {
		listHostGroups.setAdapter(
				new ResourceCursorAdapter(mActivity, R.layout.simple_list_item_single_choice_image, null, true) {
					@Override
					public void bindView(View view, Context context, Cursor cursor) {
						CheckedTextView name = (CheckedTextView) view.findViewById(android.R.id.text1);
						name.setText(cursor.getString(cursor.getColumnIndex(HostGroupData.COLUMN_NAME)));
						name.setChecked(false);

						for (long id : listHostGroups.getCheckedItemIds()) {
							if (id == cursor.getLong(cursor.getColumnIndex(BaseModelData.COLUMN__ID))) {
								name.setChecked(true);
							}
						}

						ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
						if (((CurrentViewSupport) mActivity).getCurrentView() == AppView.PROBLEMS && cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY) > -1) {
							if (cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY)) >= 4) {
								icon.setImageResource(R.drawable.severity_high);
								icon.setTag("severity_high"); // for unit test
							} else {
								icon.setImageResource(R.drawable.severity_avg);
								icon.setTag("severity_avg"); // for unit test
							}
							icon.setVisibility(View.VISIBLE);
						} else {
							icon.setVisibility(View.GONE);
						}
					}
				}
		);

		listHosts.setAdapter(
				new ResourceCursorAdapter(mActivity, R.layout.simple_list_item_single_choice_image, null, true) {
					@Override
					public void bindView(View view, Context context, Cursor cursor) {
						CheckedTextView name = (CheckedTextView) view.findViewById(android.R.id.text1);
						name.setText(cursor.getString(cursor.getColumnIndex(HostData.COLUMN_HOST)));
						name.setChecked(false);

						for (long id : listHosts.getCheckedItemIds()) {
							if (id == cursor.getLong(cursor.getColumnIndex(BaseModelData.COLUMN__ID))) {
								name.setChecked(true);
							}
						}

						ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
						if (((CurrentViewSupport) mActivity).getCurrentView() == AppView.PROBLEMS && cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY) > -1) {
							if (cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY)) >= 4) {
								icon.setImageResource(R.drawable.severity_high);
								icon.setTag("severity_high"); // for unit test
							} else {
								icon.setImageResource(R.drawable.severity_avg);
								icon.setTag("severity_avg"); // for unit test
							}
							icon.setVisibility(View.VISIBLE);
						} else {
							icon.setVisibility(View.GONE);
						}
					}
				}
		);
	}
}
