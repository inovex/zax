/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	private int mHostGroupPosition;
	private OnHostGroupSelectedListener mCallback;

	public interface OnHostGroupSelectedListener {
		public void onHostGroupSelected(int position);
	}

	public HostGroupsSpinnerAdapter(ZabbixDataService service) {
		super(service);
		addBaseGroups();
	}

	public void setCallback(OnHostGroupSelectedListener callback) {
		this.mCallback = callback;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View actionBarView = getInflater().inflate(
				R.layout.toolbar_spinner_item_actionbar, null);
		TextView title = (TextView) actionBarView
				.findViewById(android.R.id.text1);
		title.setText(mTitle);
		TextView subtitle = (TextView) actionBarView
				.findViewById(android.R.id.text2);
		subtitle.setText(getItem(position).getName());
		return actionBarView;

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View actionBarDropDownView = getInflater().inflate(
				R.layout.toolbar_spinner_item_dropdown, null);
		TextView dropDownTitle = (TextView) actionBarDropDownView
				.findViewById(android.R.id.text1);

		dropDownTitle.setText(getItem(position).getName());

		return actionBarDropDownView;

	}

	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public long getItemId(int position) {
		HostGroup item = getItem(position);
		if(item != null)
			return item.getGroupId();
		return 0;
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
				.getResources().getString(R.string.hostgroup_all)));
	}

	@Override
	public void setCurrentPosition(int position) {
		this.mHostGroupPosition = position;
	}

	@Override
	public int getCurrentPosition() {
		return mHostGroupPosition;
	}

	public long getCurrentItemId() {
		return getItemId(mHostGroupPosition);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		// update the current selection (we might have saved this position
		// before)
		refreshSelection();
	}

	public void refreshSelection() {
		if (mCallback != null && mObjects.size() > mHostGroupPosition)
			mCallback.onHostGroupSelected(mHostGroupPosition);
	}

}