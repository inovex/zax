package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.Application;

public class ChecksDetailsPage extends BaseServiceConnectedListFragment {

	private Application mApplication;
	private String mTitle = "";

	public static String TAG = ChecksDetailsPage.class.getSimpleName();

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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.empty_list_checks));
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mCurrentPosition, true);
		getListView().setSelection(mCurrentPosition);
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
