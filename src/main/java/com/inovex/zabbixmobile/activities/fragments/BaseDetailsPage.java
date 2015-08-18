/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphDataLoadedListener;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

/**
 * Base page for details of an item, an event or a trigger.
 * 
 * This class provides functionality to show a graph corresponding to the data
 * object.
 * 
 */
public abstract class BaseDetailsPage extends BaseServiceConnectedFragment
		implements OnGraphDataLoadedListener {

	protected boolean mHistoryDetailsImported = false;
	private boolean mGraphProgressBarVisible = true;
	private static final String ARG_GRAPH_SPINNER_VISIBLE = "arg_graph_spinner_visible";
	private static final String TAG = BaseDetailsPage.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mGraphProgressBarVisible = savedInstanceState.getBoolean(
					ARG_GRAPH_SPINNER_VISIBLE, true);
		}
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_GRAPH_SPINNER_VISIBLE, mGraphProgressBarVisible);
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, "onViewCreated");

		if (mGraphProgressBarVisible)
			showGraphProgressBar();

		fillDetailsText();

		if (mHistoryDetailsImported)
			showGraph();
	}

	protected abstract void fillDetailsText();

	protected void showGraph(final Item item) {
		Log.d(TAG, "showGraph(" + item + ")");
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		dismissGraphProgressBar();
		if (item != null && item.getHistoryDetails() != null) {
			// create graph and add it to the layout
			final LineGraphView graph = GraphUtil.createItemGraphPreview(
					getActivity(), item);
			if (graph != null) {
				layout.removeAllViews();
				layout.addView(graph);
			} else {
				// no history data available
				layout.removeAllViews();
				TextView noGraphDataView = new TextView(getActivity());
				noGraphDataView.setText(R.string.no_history_data_found);
				layout.addView(noGraphDataView);
			}
		}
	}

	protected abstract void showGraph();

	@Override
	public void onGraphDataLoaded() {
		Log.d(TAG, "onGraphDataLoaded");
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
	 * Shows a progress bar instead of the graph view.
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
	 * Dismisses the graph progress bar view.
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
