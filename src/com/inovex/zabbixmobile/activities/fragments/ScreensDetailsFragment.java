package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphsLoadedListener;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.Screen;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

public class ScreensDetailsFragment extends BaseServiceConnectedFragment
		implements OnGraphsLoadedListener {

	private static final String ARG_GRAPH_SPINNER_VISIBLE = "arg_graph_spinner_visible";

	private Screen mScreen;
	private boolean mGraphLoadingSpinnerVisible = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mGraphLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_GRAPH_SPINNER_VISIBLE, true);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_GRAPH_SPINNER_VISIBLE,
				mGraphLoadingSpinnerVisible);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_screens_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mGraphLoadingSpinnerVisible)
			showGraphLoadingSpinner();
		if (mScreen != null)
			showGraphs();

	}

	public void setScreen(Screen screen) {
		this.mScreen = screen;
		if (getView() != null) {
			loadGraphs();
		}
	}

	private void showGraph(Graph graph) {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		if (graph != null) {
			LineGraphView graphView = GraphUtil.createScreenGraphPreview(getActivity(), graph);
			LinearLayout graphLayout = new LinearLayout(getSherlockActivity());
			graphLayout.addView(graphView);

			layout.addView(graphLayout, new LayoutParams(
					LayoutParams.MATCH_PARENT, 300));
		} else {
			// no history data available
			// layout.removeAllViews();
			TextView noGraphDataView = new TextView(getSherlockActivity());
			noGraphDataView.setText(R.string.no_history_data_found);
			layout.addView(noGraphDataView);
		}
	}

	@Override
	public void onGraphsLoaded() {
		// now we have all graphs, so we can show them
		if (getView() != null) {
			showGraphs();
		}
		dismissGraphLoadingSpinner();
		// if (mScreen.getGraphs() != null)
		// showGraph((Graph) mScreen.getGraphs().toArray()[0]);
	}

	protected void showGraphs() {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		layout.removeAllViews();
		if (mScreen != null && mScreen.getGraphs() != null) {
			for (Graph g : mScreen.getGraphs()) {
				showGraph(g);
			}
		}
	}

	/**
	 * Shows a loading spinner instead of the graph view.
	 */
	public void showGraphLoadingSpinner() {
		mGraphLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graphs_progress_layout);
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
	public void dismissGraphLoadingSpinner() {
		mGraphLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.graphs_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	public void loadGraphs() {
		if (mZabbixDataService != null && mScreen != null) {
			mZabbixDataService.loadGraphsByScreen(mScreen, this);
		}
	}
}
