package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Represents one page of a list view pager. Shows a list of items
 * (events/problems) for a specific severity.
 */
public abstract class BaseSeverityFilterListPage extends
		BaseServiceConnectedListFragment {

	private static final String TAG = BaseSeverityFilterListPage.class
			.getSimpleName();

	private static final String ARG_POSITION = "arg_position";
	private static final String ARG_SEVERITY = "arg_severity";

	private OnListItemSelectedListener mCallbackMain;

	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	private int mPosition = 0;

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
			mPosition = savedInstanceState.getInt(ARG_POSITION, 0);
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
		outState.putInt(ARG_POSITION, mPosition);
		outState.putInt(ARG_SEVERITY, mSeverity.getNumber());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, this + " onViewCreated");
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mPosition, true);
		getListView().setSelection(mPosition);
	}

	public CharSequence getTitle() {
		return mSeverity.getName();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(l, v, " + position + ", " + id
				+ "). severity: " + mSeverity);
		Log.d(TAG, "list item clicked in: " + this.toString());
		mPosition = position;
		mCallbackMain.onListItemSelected(position, id);
	}

	public void selectItem(int position) {

		mPosition = position;
		// check if the view has already been created -> if not, calls will be
		// made in onViewCreated().
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

	public void setItemSelected(int itemSelected) {
		this.mPosition = itemSelected;

	}

	public void setCustomEmptyText(CharSequence text) {
		TextView emptyView = (TextView) getView().findViewById(
				android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(text);
	}

}
