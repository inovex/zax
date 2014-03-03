package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.BaseActivity;

public class NavigationDrawerFragment extends BaseServiceConnectedFragment {

	private ListView mDrawerList;
	private BaseActivity mActivity;

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
		mDrawerList = (ListView) getView().findViewById(R.id.drawer_menu);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this.getActivity(),
				R.layout.list_item_main_menu, getResources().getStringArray(
						R.array.activities)));
		mDrawerList.setOnItemClickListener(mActivity);
	}

	public void selectItem(int index) {
		mDrawerList.setItemChecked(index, true);
	}

}
