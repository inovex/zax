package com.inovex.zabbixmobile.activities.support;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.HistoryDetailData;
import com.inovex.zabbixmobile.model.ItemData;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class DetailsItemFragmentSupport {
	private final Activity mActivity;

	public DetailsItemFragmentSupport(Activity mActivity) {
		super();
		this.mActivity = mActivity;
	}

	public String getItemText(Cursor cursor) {
		String svalue = cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_LASTVALUE));
		if (svalue == null || svalue.equals("null")) {
			svalue = "- "+mActivity.getResources().getString(R.string.unknown)+" -";
		} else {
			svalue += ' '+cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_UNITS));
		}
		Date date = new Date(cursor.getInt(cursor.getColumnIndex(ItemData.COLUMN_LASTCLOCK))*1000l);
		java.text.DateFormat df = DateFormat.getDateFormat(mActivity.getApplicationContext());
		java.text.DateFormat tf = DateFormat.getTimeFormat(mActivity.getApplicationContext());
		String clock = df.format(date)+ " " +tf.format(date);

		return String.format(mActivity.getResources().getString(R.string.graph_text) + "itemID: " + cursor.getInt(cursor.getColumnIndex(ItemData.COLUMN_ITEMID)), svalue, clock);
	}

	public void showGraph(Cursor cursor, String currentGraphDescription) {
		int numEntries = cursor.getCount();
		if (numEntries > 0) {
			long lowestclock = 0;
			long highestclock = 0;

			GraphViewData[] values = new GraphViewData[numEntries];
			cursor.moveToFirst();
			for (int i=0; i<values.length; i++) {
				long clock = cursor.getLong(cursor.getColumnIndex(HistoryDetailData.COLUMN_CLOCK));
				double value = cursor.getDouble(cursor.getColumnIndex(HistoryDetailData.COLUMN_VALUE));
				if (i==0) {
					lowestclock = highestclock = clock;
				} else {
					highestclock = Math.max(highestclock, clock);
					lowestclock = Math.min(lowestclock, clock);
				}
				values[i] = new GraphViewData(clock, value);
				cursor.moveToNext();
			}

			final java.text.DateFormat dateTimeFormatter = DateFormat.getTimeFormat(mActivity);
			LineGraphView graph = new LineGraphView(
					mActivity
					, currentGraphDescription // title
			) {
				@Override
				protected String formatLabel(double value, boolean isValueX) {
					if (isValueX) {
						// transform number to time
						return dateTimeFormatter.format(new Date((long) value*1000));
					} else return super.formatLabel(value, isValueX);
				}
			};
			graph.addSeries(new GraphViewSeries(values));
			graph.setDrawBackground(true);
			long size = (highestclock-lowestclock)*2/3; // we show 2/3
			graph.setViewPort(highestclock - size, size);
			graph.setScalable(true);
			LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.tab_graphs_container);
			layout.removeAllViews();
			layout.addView(graph);
		} else {
			// no history data available
			LinearLayout layout = (LinearLayout) mActivity.findViewById(R.id.tab_graphs_container);
			layout.removeAllViews();
			mActivity.getLayoutInflater().inflate(R.layout.details_no_data, layout);
			((TextView) layout.findViewById(R.id.details_no_data_text)).setText(R.string.no_history_data_found);
		}
	}
}
