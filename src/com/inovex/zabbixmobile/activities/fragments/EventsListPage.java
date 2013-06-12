package com.inovex.zabbixmobile.activities.fragments;

import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment.Severities;
import com.inovex.zabbixmobile.model.DataAccess;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.view.EventsArrayAdapter;

public class EventsListPage extends SherlockListFragment {

	private static final String TAG = EventsListPage.class.getSimpleName();
	
	public static final String ARG_TITLE = "title";
	public static final String ARG_SEVERITY = "severity";
	private static final String ARG_ITEM_SELECTED = "item_selected";

	private OnEventSelectedListener mCallbackMain;
	
	private String title;
	private int severity;
	private int itemSelected;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnEventSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnEventSelectedListener.");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null) {
			title = savedInstanceState.getString(ARG_TITLE);
			if(title == null)
				title = Severities.ALL.getName();
			severity = savedInstanceState.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
			itemSelected = savedInstanceState.getInt(ARG_ITEM_SELECTED, 0);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		DataAccess dataAccess = DataAccess.getInstance(getSherlockActivity());
		
		Bundle args = getArguments();
		title = args.getString(ARG_TITLE);
		if(title == null)
			title = Severities.ALL.getName();
		severity = args.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
		itemSelected = args.getInt(ARG_ITEM_SELECTED, 0);
		try {
			List<Event> events;
			
			Log.d(TAG, "category name: " + title);
			events = dataAccess.getEventsBySeverity(severity);
			EventsArrayAdapter adapter = new EventsArrayAdapter(getSherlockActivity(),
					R.layout.events_list_item, events);
			this.setListAdapter(adapter);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_TITLE, title);
		outState.putInt(ARG_SEVERITY, severity);
		outState.putInt(ARG_ITEM_SELECTED, itemSelected);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	}

	public CharSequence getTitle() {
		return getArguments().getString(ARG_TITLE);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(l, v, " + position + ", " + id
				+ "). severity: " + severity);
		getArguments().putInt(ARG_ITEM_SELECTED, position);
		mCallbackMain.onEventSelected(position, severity, id);
	}

	public void selectEvent(int position) {
		getListView().setItemChecked(position, true);
		getListView().setSelection(position);
		itemSelected = position;
		getArguments().putInt(ARG_ITEM_SELECTED, position);
	}

}
