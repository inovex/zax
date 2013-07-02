package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.HostGroup;

/**
 * Adapter for the host groups spinner, which is used to filter items by host
 * group.
 * 
 */
public class HostGroupsSpinnerAdapter extends BaseServiceAdapter<HostGroup> {

	private String mTitle;

	public HostGroupsSpinnerAdapter(ZabbixDataService service) {
		super(service);
		addBaseGroups();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View actionBarView = getInflater().inflate(
				R.layout.spinner_host_groups_main, null);
		TextView title = (TextView) actionBarView
				.findViewById(R.id.shost_groups_spinner_title);
		title.setText(mTitle);
		TextView subtitle = (TextView) actionBarView
				.findViewById(R.id.host_groups_spinner_subtitle);
		subtitle.setText(getItem(position).getName());
		return actionBarView;

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View actionBarDropDownView = getInflater().inflate(
				R.layout.spinner_host_groups_dropdown, null);
		TextView dropDownTitle = (TextView) actionBarDropDownView
				.findViewById(R.id.host_groups_spinner_dropdown_title);

		dropDownTitle.setText(getItem(position).getName());

		return actionBarDropDownView;

	}

	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getGroupId();
	}

	@Override
	public void clear() {
		super.clear();
		addBaseGroups();
	}

	/**
	 * Adds the base host group for the display of all items.
	 */
	private void addBaseGroups() {
		mObjects.add(new HostGroup(HostGroup.GROUP_ID_ALL, mZabbixDataService
				.getResources().getString(R.string.all)));
	}

}