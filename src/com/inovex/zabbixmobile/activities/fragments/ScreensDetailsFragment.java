package com.inovex.zabbixmobile.activities.fragments;

import java.util.Collection;
import java.util.Date;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphsLoadedListener;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.GraphItem;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Screen;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
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
		else
			showGraphs();

	}

	public void setScreen(Screen screen) {
		this.mScreen = screen;
		if (getView() != null) {
			if (mZabbixDataService != null) {
				mZabbixDataService.loadGraphsByScreen(mScreen, this);
			}
		}
	}

	private void showGraph(Graph graph) {
		ViewGroup layout = (LinearLayout) getView().findViewById(R.id.graphs);
		if (graph != null) {
			Collection<HistoryDetail> historyDetails = null;
			Item item = null;

			final java.text.DateFormat dateTimeFormatter = DateFormat
					.getTimeFormat(getSherlockActivity());
			LineGraphView graphView = new LineGraphView(getSherlockActivity(),
					graph.getName()) {
				@Override
				protected String formatLabel(double value, boolean isValueX) {
					if (isValueX) {
						// transform number to time
						return dateTimeFormatter.format(new Date(
								(long) value * 1000));
					} else
						return super.formatLabel(value, isValueX);
				}
			};
			graphView.setShowLegend(true);
			graphView.setLegendAlign(LegendAlign.TOP);
			graphView.setLegendWidth(250);

			GraphViewSeries series;
			long highestclock = Long.MIN_VALUE;
			long lowestclock = Long.MAX_VALUE;
			for (GraphItem gi : graph.getGraphItems()) {
				item = gi.getItem();
				historyDetails = item.getHistoryDetails();
				if (historyDetails.size() > 0) {
					GraphViewData[] values = new GraphViewData[historyDetails
							.size()];
					int i = 0;
					for (HistoryDetail detail : historyDetails) {
						long clock = detail.getClock() / 1000;
						double value = detail.getValue();
						values[i] = new GraphViewData(clock, value);
						highestclock = Math.max(highestclock,
								clock);
						lowestclock = Math
								.min(lowestclock, clock);
						i++;
					}
					
					series = new GraphViewSeries(item.getDescription(),
							new GraphViewSeriesStyle(gi.getColor(), 3), values);
					// series = new GraphViewSeries(values);
					graphView.addSeries(series);
				}
			}

			// create graph and add it to the layout
			long size = (highestclock - lowestclock) * 2 / 3; // we show 2/3
			graphView.setViewPort(highestclock - size, size);
			graphView.setScalable(true);

			GraphViewStyle style = new GraphViewStyle();
			style.setHorizontalLabelsColor(getSherlockActivity().getResources()
					.getColor(android.R.color.black));
			style.setVerticalLabelsColor(getSherlockActivity().getResources()
					.getColor(android.R.color.black));
			graphView.setGraphViewStyle(style); 

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
		for (Graph g : mScreen.getGraphs()) {
			showGraph(g);
		}
	}

	/**
	 * Shows a loading spinner instead of the graph view.
	 */
	public void showGraphLoadingSpinner() {
		mGraphLoadingSpinnerVisible = true;
		LinearLayout progressLayout = (LinearLayout) getView().findViewById(
				R.id.graphs_progress_layout);
		if (progressLayout != null)
			progressLayout.setVisibility(View.VISIBLE);
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

}
