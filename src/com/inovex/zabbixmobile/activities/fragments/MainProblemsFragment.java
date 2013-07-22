package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.model.Trigger;

public class MainProblemsFragment extends BaseServiceConnectedListFragment {

	private static final String TAG = MainProblemsFragment.class.getSimpleName();
	
	private static final String ARG_SPINNER_VISIBLE = "arg_spinner_visible";
	
	private ListView mProblemsList;
	private Button mProblemsButton;
	
	private boolean mLoadingSpinnerVisible = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mLoadingSpinnerVisible = savedInstanceState.getBoolean(ARG_SPINNER_VISIBLE, false);
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_problems, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mProblemsButton = (Button) getView().findViewById(R.id.main_problems_button);
		mProblemsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getSherlockActivity(),
						ProblemsActivity.class);
				getSherlockActivity().startActivity(intent);
			}
		});
		
		mProblemsList = getListView();
		mProblemsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "onItemClick(pos: " + position + ", id: " + id);
				Intent intent = new Intent(getSherlockActivity(),
						ProblemsActivity.class);
				intent.putExtra(ProblemsActivity.ARG_ITEM_POSITION,
						position);
				intent.putExtra(ProblemsActivity.ARG_ITEM_ID, id);
				startActivity(intent);
			}
		});

		TextView emptyView = (TextView) getView().findViewById(
				android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(R.string.no_items_to_display);

		
		if (mLoadingSpinnerVisible)
			showLoadingSpinner();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		setupListAdapter();
	}
	
	@Override
	protected void setupListAdapter() {
		BaseServiceAdapter<Trigger> adapter = mZabbixDataService
				.getProblemsMainListAdapter();
		setListAdapter(adapter);		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ARG_SPINNER_VISIBLE, mLoadingSpinnerVisible);
	}
	
	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showLoadingSpinner() {
		mLoadingSpinnerVisible = true;
		LinearLayout progressLayout = (LinearLayout) getView().findViewById(
				R.id.list_progress_layout);
		if (progressLayout != null)
			progressLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissLoadingSpinner() {
		mLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.list_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	public void disableUI() {
		mProblemsButton.setEnabled(false);
	}

	public void enableUI() {
		mProblemsButton.setEnabled(false);
	}

}
