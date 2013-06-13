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
import com.inovex.zabbixmobile.model.DataAccess;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverities;
import com.inovex.zabbixmobile.view.EventsArrayAdapter;

public class EventsListPage extends SherlockListFragment {

	private static final String TAG = EventsListPage.class.getSimpleName();

	private OnEventSelectedListener mCallbackMain;

	private TriggerSeverities mSeverity;
	private int mItemSelected;

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
		// if(savedInstanceState != null) {
		// mTitle = savedInstanceState.getString(ARG_TITLE);
		// if(mTitle == null)
		// mTitle = TriggerSeverities.ALL.getName();
		// mSeverity = savedInstanceState.getInt(ARG_SEVERITY,
		// TriggerSeverities.ALL.getNumber());
		// mItemSelected = savedInstanceState.getInt(ARG_ITEM_SELECTED, 0);
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container,
				savedInstanceState);

		DataAccess dataAccess = DataAccess.getInstance(getSherlockActivity());

		try {
			List<Event> events;

			Log.d(TAG, "category name: " + mSeverity.getName());
			events = dataAccess.getEventsBySeverity(mSeverity);
			EventsArrayAdapter adapter = new EventsArrayAdapter(
					getSherlockActivity(), R.layout.events_list_item, events);
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
		// outState.putString(ARG_TITLE, mTitle);
		// outState.putInt(ARG_SEVERITY, mSeverity);
		// outState.putInt(ARG_ITEM_SELECTED, mItemSelected);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	}

	public CharSequence getTitle() {
		return mSeverity.getName();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(l, v, " + position + ", " + id
				+ "). severity: " + mSeverity);
		mItemSelected = position;
		mCallbackMain.onEventSelected(position, mSeverity, id);
	}

	public void selectEvent(int position) {
		getListView().setItemChecked(position, true);
		getListView().setSelection(position);
		mItemSelected = position;
	}

	public void setSeverity(TriggerSeverities severity) {
		this.mSeverity = severity;
	}

	public void setItemSelected(int itemSelected) {
		this.mItemSelected = itemSelected;
	}

}
