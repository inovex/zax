package com.inovex.zabbixmobile.util;

import java.util.Collection;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.GraphFullscreenActivity;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.GraphItem;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * Utility class to support creating Zabbix graphs more easily (using the
 * GraphView library).
 */
public class GraphUtil {
	private static LineGraphView createGraph(final Context context,
			String title, boolean isFullscreen,
			final AnimatorListenerAdapter onClickListener) {
		final java.text.DateFormat dateTimeFormatter = DateFormat
				.getTimeFormat(context);
		final LineGraphView graphView = new LineGraphView(context, title) {
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

		GraphViewStyle style = new GraphViewStyle();
		style.setHorizontalLabelsColor(context.getResources().getColor(
				android.R.color.black));
		style.setVerticalLabelsColor(context.getResources().getColor(
				android.R.color.black));
		graphView.setGraphViewStyle(style);

		/*
		 * click to open graph in fullscreen
		 */
		if (onClickListener != null) {
			graphView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int colorFrom = context.getResources().getColor(
							R.color.holo_blue_light);
					int colorTo = Color.WHITE;
					ValueAnimator colorAnimation = ValueAnimator.ofObject(
							new ArgbEvaluator(), colorFrom, colorTo);
					colorAnimation.setDuration(100);
					colorAnimation
							.addUpdateListener(new AnimatorUpdateListener() {
								@Override
								public void onAnimationUpdate(
										ValueAnimator animator) {
									graphView
											.setBackgroundColor((Integer) animator
													.getAnimatedValue());
								}
							});
					colorAnimation.addListener(onClickListener);
					colorAnimation.start();
				}
			});
		}

		return graphView;
	}

	/**
	 * Creates a preview graph (with disabled gestures).
	 * 
	 * @param context
	 *            the context
	 * @param graph
	 *            the graph to be rendered
	 * @return the generated graph view
	 */
	public static LineGraphView createScreenGraphPreview(final Context context,
			final Graph graph) {
		LineGraphView graphView = createGraph(context, graph.getName(), false,
				new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						Intent intent = new Intent(context,
								GraphFullscreenActivity.class);
						intent.putExtra("graphid", graph.getId());
						context.startActivity(intent);
					}
				});
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.TOP);
		graphView.setLegendWidth(250);

		GraphViewSeries series;
		boolean emptyGraph = true;
		for (GraphItem gi : graph.getGraphItems()) {
			Item item = gi.getItem();
			Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
			if (historyDetails != null && historyDetails.size() > 0) {
				emptyGraph = false;
				GraphViewData[] values = new GraphViewData[historyDetails
						.size()];
				int i = 0;
				for (HistoryDetail detail : historyDetails) {
					long clock = detail.getClock() / 1000;
					double value = detail.getValue();
					values[i] = new GraphViewData(clock, value);
					i++;
				}

				series = new GraphViewSeries(item.getDescription(),
						new GraphViewSeriesStyle(gi.getColor(), 3), values);
				// series = new GraphViewSeries(values);
				graphView.addSeries(series);
			}
		}
		if (emptyGraph)
			return null;

		return graphView;
	}

	/**
	 * Creates a preview graph (with disabled gestures).
	 * 
	 * @param context
	 *            the context
	 * @param item
	 *            the item to be visualized
	 * @return the generated graph view
	 */
	public static LineGraphView createItemGraphPreview(final Context context,
			final Item item) {
		Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
		LineGraphView graph = createGraph(context, item.getDescription(),
				false, new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						Intent intent = new Intent(context,
								GraphFullscreenActivity.class);
						intent.putExtra("itemid", item.getId());
						context.startActivity(intent);
					}
				});

		int numEntries = historyDetails.size();
		GraphViewData[] values = new GraphViewData[numEntries];
		int i = 0;
		if (historyDetails == null || historyDetails.size() == 0)
			return null;
		for (HistoryDetail detail : historyDetails) {
			long clock = detail.getClock() / 1000;
			double value = detail.getValue();
			values[i] = new GraphViewData(clock, value);
			i++;
		}
		graph.addSeries(new GraphViewSeries(values));

		return graph;
	}

	/**
	 * Creates a fullscreen graph.
	 * 
	 * @param context
	 *            the context
	 * @param item
	 *            the item to be visualized
	 * @return the generated graph view
	 */
	public static LineGraphView createItemGraphFullscreen(Context context,
			Item item) {
		Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
		String title = item.getDescription();
		LineGraphView graph = createGraph(context, title, true, null);
		// graph.setDrawBackground(true);

		int numEntries = historyDetails.size();
		long lowestclock = 0;
		long highestclock = 0;

		GraphViewData[] values = new GraphViewData[numEntries];
		int i = 0;
		for (HistoryDetail detail : historyDetails) {
			long clock = detail.getClock() / 1000;
			double value = detail.getValue();
			if (i == 0) {
				lowestclock = highestclock = clock;
			} else {
				highestclock = Math.max(highestclock, clock);
				lowestclock = Math.min(lowestclock, clock);
			}
			values[i] = new GraphViewData(clock, value);
			i++;
		}
		graph.addSeries(new GraphViewSeries(values));

		// set viewport
		long size = (highestclock - lowestclock) * 2 / 3; // we show 2/3
		graph.setViewPort(highestclock - size, size);
		graph.setScalable(true);

		return graph;
	}

	/**
	 * Creates a fullscreen graph.
	 * 
	 * @param context
	 *            the context
	 * @param graph
	 *            the graph to be rendered
	 * @return the generated graph view
	 */
	public static LineGraphView createScreenGraphFullscreen(
			final Context context, final Graph graph) {
		LineGraphView graphView = createGraph(context, graph.getName(), true,
				null);
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.TOP);
		graphView.setLegendWidth(250);

		long lowestclock = 0;
		long highestclock = 0;

		for (GraphItem gi : graph.getGraphItems()) {
			Item item = gi.getItem();
			Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
			if (historyDetails != null && historyDetails.size() > 0) {
				GraphViewData[] values = new GraphViewData[historyDetails
						.size()];
				int i = 0;
				for (HistoryDetail detail : historyDetails) {
					long clock = detail.getClock() / 1000;
					double value = detail.getValue();
					if (i == 0) {
						lowestclock = highestclock = clock;
					} else {
						highestclock = Math.max(highestclock, clock);
						lowestclock = Math.min(lowestclock, clock);
					}

					values[i] = new GraphViewData(clock, value);
					i++;
				}

				GraphViewSeries series = new GraphViewSeries(
						item.getDescription(), new GraphViewSeriesStyle(
								gi.getColor(), 3), values);
				// series = new GraphViewSeries(values);
				graphView.addSeries(series);
			}
		}

		// set viewport
		long size = (highestclock - lowestclock) * 2 / 3; // we show 2/3
		graphView.setViewPort(highestclock - size, size);
		graphView.setScalable(true);

		return graphView;
	}
}
