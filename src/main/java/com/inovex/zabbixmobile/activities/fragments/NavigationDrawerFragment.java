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

package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.BaseActivity;
import com.inovex.zabbixmobile.activities.ServersActivity;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;

public class NavigationDrawerFragment extends BaseServiceConnectedFragment
		implements OnItemClickListener {

	private static final String TAG = NavigationDrawerFragment.class
			.getSimpleName();

	private BaseActivity mActivity;

	private ListView mMenuList;
	private ListView mServerList;

	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;

	private Button mManageServersButton;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mActivity = (BaseActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must inherit from BaseActivity.");
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_navigation_drawer, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mServerList = (ListView) getView()
				.findViewById(R.id.drawer_server_list);

		mManageServersButton = (Button) getView().findViewById(
				R.id.drawer_manage_servers);
		mManageServersButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						ServersActivity.class);
				getActivity().startActivity(intent);
			}
		});

/*		mMenuList = (ListView) getView().findViewById(R.id.drawer_menu);
		// set up the drawer's list view with items and click listener
		mMenuList.setAdapter(new ArrayAdapter<String>(this.getActivity(),
				R.layout.list_item_main_menu, getResources().getStringArray(
						R.array.activities)));
		mMenuList.setOnItemClickListener(mActivity);*/
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		mServersListAdapter = mZabbixDataService.getServersSelectionAdapter();
		mServerList.setAdapter(mServersListAdapter);
		mServerList.setOnItemClickListener(this);
		mServerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mServerList.setItemsCanFocus(false);

		Log.d(TAG, "on service connected - restore server selection");
		restoreServerSelection();
	}

	public void restoreServerSelection() {
		long persistedSelection = ZaxPreferences.getInstance(
				getActivity().getApplicationContext()).getServerSelection();
		selectServerItem(persistedSelection);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectServerItem(id);
		// persist selection
		ZaxPreferences.getInstance(getActivity().getApplicationContext())
				.setServerSelection(id);
		Log.d(TAG, "selectedid="+id);

		mActivity.refreshData();
	}

	protected void selectServerItem(long zabbixserverId) {
		for (int i=0; i<mServersListAdapter.getCount(); i++) {
			if (mServersListAdapter.getItemId(i) == zabbixserverId) {
				mServersListAdapter.setCurrentPosition(i);
				mServerList.setItemChecked(i, true);
				mServerList.setSelection(i);
				break;
			}
		}
	}

	public void selectMenuItem(int index) {
		//todo fix me mMenuList.setItemChecked(index, true);
	}
}
