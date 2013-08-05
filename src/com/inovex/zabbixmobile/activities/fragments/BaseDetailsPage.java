package com.inovex.zabbixmobile.activities.fragments;

import java.util.Collection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphDataLoadedListener;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

public abstract class BaseDetailsPage extends BaseServiceConnectedFragment
		implements OnGraphDataLoadedListener {

	protected boolean mHistoryDetailsImported = false;
	private boolean mGraphProgressBarVisible = true;
	private static final String ARG_GRAPH_SPINNER_VISIBLE = "arg_graph_spinner_visible";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mGraphProgressBarVisible = savedInstanceState.getBoolean(
					ARG_GRAPH_SPINNER_VISIBLE, true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_GRAPH_SPINNER_VISIBLE,
				mGraphProgressBarVisible);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mGraphProgressBarVisible)
			showGraphProgressBar();

		fillDetailsText();

		if (mHistoryDetailsImported)
			showGraph();
	}

	protected abstract void fillDetailsText();

	protected void showGraph(final Item item) {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		dismissGraphProgressBar();
		if (item != null) {
			Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
			// create graph and add it to the layout
			final LineGraphView graph = GraphUtil.createItemGraphPreview(
					getSherlockActivity(), historyDetails, item);
			if (graph != null) {
				layout.removeAllViews();
				layout.addView(graph);
			} else {
				// no history data available
				layout.removeAllViews();
				TextView noGraphDataView = new TextView(getSherlockActivity());
				noGraphDataView.setText(R.string.no_history_data_found);
				layout.addView(noGraphDataView);
			}
		}
	}

	protected abstract void showGraph();

	@Override
	public void onGraphDataLoaded() {
		mHistoryDetailsImported = true;
		if (getView() != null) {
			showGraph();
			// reset progress
			ProgressBar graphProgress = (ProgressBar) getView().findViewById(
					R.id.graph_progress);
			graphProgress.setProgress(0);
		}
	}

	/**
	 * Shows a loading spinner instead of the graph view.
	 */
	public void showGraphProgressBar() {
		mGraphProgressBarVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graph_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the graph loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissGraphProgressBar() {
		mGraphProgressBarVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graph_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	@Override
	public void onGraphDataProgressUpdate(int progress) {
		if (getView() != null) {
			ProgressBar graphProgress = (ProgressBar) getView().findViewById(
					R.id.graph_progress);
			graphProgress.setProgress(progress);
		}
	}

}
