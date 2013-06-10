package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.R;

public class EventsListPage extends SherlockListFragment {

	public static final String ARG_CATEGORY_NAME = "category_name";
	public static final String ARG_CATEGORY_NUMBER = "category_number";
	public static final String ARG_ITEMS = "items";
	private static final String ARG_ITEM_SELECTED = "item_selected";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		// this selects simple_list_item_activated_1 only for API > 11 and
		// landscape orientation; otherwise simple_list_item_1
		int listItemLayout = R.layout.simple_list_item;
		this.setListAdapter(new ArrayAdapter<String>(getSherlockActivity(),
				listItemLayout, new String[] { "1", "2", "3" }));
		TextView tv = new TextView(getSherlockActivity());
		tv.setText("Test");
		container.addView(tv);
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	public CharSequence getTitle() {
		return this.getArguments().getString(ARG_CATEGORY_NAME);
	}

}
