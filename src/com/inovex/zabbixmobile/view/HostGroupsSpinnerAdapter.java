package com.inovex.zabbixmobile.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.HostGroup;

public class HostGroupsSpinnerAdapter extends BaseServiceAdapter<HostGroup> {

	private String mTitle;

	public HostGroupsSpinnerAdapter(ZabbixDataService service) {
		super(service);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View actionBarView = getInflater().inflate(R.layout.host_groups_spinner_main, null);
		TextView title = (TextView) actionBarView
				.findViewById(R.id.shost_groups_spinner_title);
		title.setText(mTitle);
		TextView subtitle = (TextView) actionBarView
				.findViewById(R.id.host_groups_spinner_subtitle);
		subtitle.setText(mObjects.get(position).getName());
		return actionBarView;

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View actionBarDropDownView = getInflater().inflate(
				R.layout.host_groups_spinner_dropdown, null);
		TextView dropDownTitle = (TextView) actionBarDropDownView
				.findViewById(R.id.host_groups_spinner_dropdown_title);

		dropDownTitle.setText(mObjects.get(position).getName());

		return actionBarDropDownView;

	}
	
	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public long getItemId(int position) {
		return mObjects.get(position).getGroupId();
	}

}