package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.Application;

/**
 * A page representing one particular application and thus containing a list of
 * all items in this application.
 * 
 */
public class ChecksApplicationsPage extends BaseServiceConnectedListFragment {

	private Application mApplication;
	private String mTitle = "";

	public static String TAG = ChecksApplicationsPage.class.getSimpleName();

	// TODO: move selection state to adapter
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_base_list, null);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// setEmptyText(getResources().getString(R.string.empty_list_checks));
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mCurrentPosition, true);
		getListView().setSelection(mCurrentPosition);

		TextView emptyView = (TextView) getView().findViewById(
				android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(R.string.empty_list_checks);

	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	public void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	/**
	 * Selects an item in this page's list.
	 * 
	 * @param position
	 *            the item's position
	 */
	public void selectItem(int position) {
		setCurrentPosition(position);
		if (getView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
		ListAdapter adapter = getListAdapter();
		if (adapter != null && adapter.getCount() > position) {
			long id = adapter.getItemId(position);
			setCurrentItemId(id);
		}
	}

	/**
	 * Restores the item selection using the list adapter's state.
	 */
	public void restoreItemSelection() {
		if (mZabbixDataService == null
				|| mZabbixDataService.getChecksItemsListAdapter().getCount() <= 0)
			return;
		if (mCurrentPosition >= mZabbixDataService.getChecksItemsListAdapter()
				.getCount())
			mCurrentPosition = 0;
		// selectItem(mCurrentPosition);
		mCallbackMain.onItemSelected(mCurrentPosition, mZabbixDataService
				.getChecksItemsListAdapter().getItem(mCurrentPosition), false);
	}

	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getChecksItemsListAdapter());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallbackMain.onItemSelected(position, mZabbixDataService
				.getChecksItemsListAdapter().getItem(position), true);
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
