package com.inovex.zabbixmobile.util;

import java.util.Collection;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

import com.inovex.zabbixmobile.model.HistoryDetail;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

public class GraphUtil {
	
	public static LineGraphView createItemGraph(Context context,
			Collection<HistoryDetail> historyDetails, String title) {
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

		final java.text.DateFormat dateTimeFormatter = DateFormat
				.getTimeFormat(context);
		LineGraphView graph = new LineGraphView(context, title
		) {
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
		graph.addSeries(new GraphViewSeries(values));
		graph.setDrawBackground(true);
		long size = (highestclock - lowestclock) * 2 / 3; // we show 2/3
		// graph.setViewPort(highestclock - size, size);
		graph.setViewPort(lowestclock, (highestclock - lowestclock));
		graph.setScalable(true);
		graph.setDiscableTouch(true);
		// graph.setScalable(false);
		GraphViewStyle style = new GraphViewStyle();
		style.setHorizontalLabelsColor(context.getResources().getColor(
				android.R.color.black));
		style.setVerticalLabelsColor(context.getResources().getColor(
				android.R.color.black));
		graph.setGraphViewStyle(style);
		return graph;
	}
	
}
