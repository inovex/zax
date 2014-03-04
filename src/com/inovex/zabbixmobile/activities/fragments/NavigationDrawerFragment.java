package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.BaseActivity;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class NavigationDrawerFragment extends BaseServiceConnectedFragment
		implements OnItemClickListener {

	private static final String TAG = NavigationDrawerFragment.class
			.getSimpleName();

	private BaseActivity mActivity;

	private ListView mMenuList;
	private ListView mServerList;

	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;

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

		mMenuList = (ListView) getView().findViewById(R.id.drawer_menu);
		// set up the drawer's list view with items and click listener
		mMenuList.setAdapter(new ArrayAdapter<String>(this.getActivity(),
				R.layout.list_item_main_menu, getResources().getStringArray(
						R.array.activities)));
		mMenuList.setOnItemClickListener(mActivity);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		mServersListAdapter = mZabbixDataService.getServersListAdapter();
		mServerList.setAdapter(mServersListAdapter);
		mServerList.setOnItemClickListener(this);
		mServerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mServerList.setItemsCanFocus(false);

		restoreServerSelection();

	}

	private void restoreServerSelection() {
		mServerList.setItemChecked(mServersListAdapter.getCurrentPosition(),
				true);
		mServerList.setSelection(mServersListAdapter.getCurrentPosition());
	}

	public void selectItem(int index) {
		mMenuList.setItemChecked(index, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mServersListAdapter.setCurrentPosition(position);
		mServerList.setItemChecked(position, true);
		mServerList.setSelection(position);
	}

}
