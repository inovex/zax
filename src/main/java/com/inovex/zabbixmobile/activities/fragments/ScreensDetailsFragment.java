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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphsLoadedListener;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.Screen;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

/**
 * Fragment showing one particular screen.
 * 
 */
public class ScreensDetailsFragment extends BaseServiceConnectedFragment
		implements OnGraphsLoadedListener {

	private static final String ARG_GRAPH_SPINNER_VISIBLE = "arg_graph_spinner_visible";

	private Screen mScreen;
	private boolean mProgressBarVisible = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mProgressBarVisible = savedInstanceState.getBoolean(
					ARG_GRAPH_SPINNER_VISIBLE, true);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_GRAPH_SPINNER_VISIBLE, mProgressBarVisible);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_screens_details, container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mProgressBarVisible)
			showProgressBar();
		if (mScreen != null)
			showGraphs();

	}

	public void setScreen(Screen screen) {
		this.mScreen = screen;
		showProgressBar();
		if (getView() != null) {
			loadGraphs();
		}
	}

	/**
	 * Shows a graph.
	 * 
	 * @param graph
	 * @return true: Graph has been added to the layout; false: graph could not
	 *         be displayed due to missing history data
	 */
	private boolean showGraph(Graph graph) {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		if (graph != null) {
			LineGraphView graphView = GraphUtil.createScreenGraphPreview(
					getActivity(), graph);
			if (graphView == null)
				return false;
			LinearLayout graphLayout = new LinearLayout(getActivity());
			graphLayout.addView(graphView);

			layout.addView(graphLayout, new LayoutParams(
					LayoutParams.MATCH_PARENT, 300));
			return true;
		}
		return false;
	}

	@Override
	public void onGraphsLoaded() {
		// now we have all graphs, so we can show them
		if (getView() != null) {
			showGraphs();
		}
		dismissGraphProgressBar();
	}

	protected void showGraphs() {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		layout.removeAllViews();
		boolean graphsDisplayed = false;
		if (mScreen != null && mScreen.getGraphs() != null) {
			for (Graph g : mScreen.getGraphs()) {
				if (showGraph(g))
					graphsDisplayed = true;
			}
		}
		if (!graphsDisplayed) {
			// no graphs have been shown (due to missing history data)
			layout.removeAllViews();
			TextView noGraphDataView = new TextView(getActivity());
			noGraphDataView.setText(R.string.no_items_to_display);
			layout.addView(noGraphDataView);
		}

	}

	/**
	 * Shows a loading spinner instead of the graph view.
	 */
	public void showProgressBar() {
		mProgressBarVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graphs_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
			ProgressBar graphProgress = (ProgressBar) getView().findViewById(
					R.id.graphs_progress);
			graphProgress.setProgress(0);
		}
	}

	/**
	 * Dismisses the graph loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissGraphProgressBar() {
		mProgressBarVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graphs_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * Loads all graphs for this fragment's screen.
	 */
	public void loadGraphs() {
		if (mZabbixDataService != null && mScreen != null) {
			mZabbixDataService.loadGraphsByScreen(mScreen, this);
		}
	}

	@Override
	public void onGraphsProgressUpdate(int progress) {
		if (getView() != null) {
			ProgressBar graphProgress = (ProgressBar) getView().findViewById(
					R.id.graphs_progress);
			graphProgress.setProgress(progress);
		}
	}
}
