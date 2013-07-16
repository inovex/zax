package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.Application;

public class ChecksDetailsPage extends BaseServiceConnectedListFragment {

	private Application mApplication;
	private String mTitle = "";

	public static String TAG = ChecksDetailsPage.class.getSimpleName();

	private static final String ARG_POSITION = "arg_position";

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;

	private OnChecksItemSelectedListener mCallbackMain;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallbackMain = (OnChecksItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChecksItemSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: " + this.toString());
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION, 0);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.empty_list_checks));
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mCurrentPosition, true);
		getListView().setSelection(mCurrentPosition);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_POSITION, mCurrentPosition);
	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	public void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getChecksItemsListAdapter());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallbackMain.onItemSelected(position, id);
	}

	public void setApplication(Application app) {
		this.mApplication = app;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

}
