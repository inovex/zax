package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Represents one page of a list view pager. Shows a list of items
 * (events/problems) for a specific severity.
 * @param <T>
 */
public abstract class BaseSeverityFilterListPage<T> extends
		BaseServiceConnectedListFragment {

	private static final String TAG = BaseSeverityFilterListPage.class
			.getSimpleName();

	private static final String ARG_SEVERITY = "arg_severity";

	private OnListItemSelectedListener mCallbackMain;

	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	
	protected BaseServiceAdapter<T> mListAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnListItemSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: " + this.toString());
		if (savedInstanceState != null) {
			mSeverity = TriggerSeverity.getSeverityByNumber(savedInstanceState
					.getInt(ARG_SEVERITY, TriggerSeverity.ALL.getNumber()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_severity_filter_list,
				null);
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_SEVERITY, mSeverity.getNumber());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, this + " onViewCreated");
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		getListView().setItemChecked(mListAdapter.getCurrentPosition(), true);
		getListView().setSelection(mListAdapter.getCurrentPosition());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(l, v, " + position + ", " + id
				+ "). severity: " + mSeverity);
		Log.d(TAG, "list item clicked in: " + this.toString());
		mCallbackMain.onListItemSelected(position, id);
	}

	public void selectItem(int position) {

		if(mListAdapter != null)
			mListAdapter.setCurrentPosition(position);
		// check if the view has already been created -> if not, calls will be
		// made in onViewCreated().
		if (getView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
	}
	
	public void refreshItemSelection() {
		int position = mListAdapter.getCurrentPosition();
		if (getView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
		
	}

	public void setSeverity(TriggerSeverity severity) {
		this.mSeverity = severity;
		Log.d(TAG,
				"setSeverity: " + severity.getName() + " - " + this.toString());
	}

	public TriggerSeverity getSeverity() {
		return mSeverity;
	}

	public void setCustomEmptyText(CharSequence text) {
		TextView emptyView = (TextView) getView().findViewById(
				android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(text);
	}

}
