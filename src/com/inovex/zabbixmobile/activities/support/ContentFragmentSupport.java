package com.inovex.zabbixmobile.activities.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.MainActivitySmartphone;
import com.inovex.zabbixmobile.model.EventData;
import com.inovex.zabbixmobile.model.GraphData;
import com.inovex.zabbixmobile.model.GraphItemData;
import com.inovex.zabbixmobile.model.HistoryDetailData;
import com.inovex.zabbixmobile.model.HostData;
import com.inovex.zabbixmobile.model.ItemData;
import com.inovex.zabbixmobile.model.TriggerData;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.LineGraphView;

public class ContentFragmentSupport {
	private final Activity mActivity;

	public ContentFragmentSupport(Activity activity) {
		mActivity = activity;
	}

	public void onChecksItemClick(LoadContentSupport contentFragment, View view) {
		contentFragment.showDetailsFromItemId((Long) view.getTag());
	}

	public void onEventItemClick(LoadContentSupport contentFragment, View view) {
		String data = (String) view.getTag();
		String d[] = data.split("##\\.##");
		contentFragment.showDetailsFromEventAndTriggerId(Long.parseLong(d[0]), Long.parseLong(d[1]));
	}

	public void onProblemItemClick(LoadContentSupport contentFragment, View view) {
		contentFragment.showDetailsFromTriggerId((Long) view.getTag());
	}

	public void setupListChecksItemsAdapterBindView(View view, Context context, Cursor cursor) {
		TextView name = (TextView) view.findViewById(R.id.checks_child_entry_name);
		TextView clock = (TextView) view.findViewById(R.id.checks_child_entry_clock);
		TextView value = (TextView) view.findViewById(R.id.checks_child_entry_value);

		name.setText(cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_DESCRIPTION)));
		Date date = new Date(cursor.getInt(cursor.getColumnIndex(ItemData.COLUMN_LASTCLOCK))*1000l);
		java.text.DateFormat df = DateFormat.getDateFormat(mActivity);
		java.text.DateFormat tf = DateFormat.getTimeFormat(mActivity);
		clock.setText(df.format(date)+ " " +tf.format(date));
		String svalue = cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_LASTVALUE));
		if (svalue == null || svalue.equals("null")) {
			svalue = "- "+mActivity.getResources().getString(R.string.unknown)+" -";
			view.setEnabled(false);
		} else {
			svalue += ' '+cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_UNITS));
			view.setEnabled(true);
		}
		value.setText(svalue);
		view.setTag(
				cursor.getLong(cursor.getColumnIndex(ItemData.COLUMN_ITEMID))
		);
	}

	public void setupListEventsAdapter(ListView listEvents) {
		listEvents.setAdapter(new ResourceCursorAdapter(mActivity.getApplicationContext(), R.layout.list_events_entry, null, true) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				ImageView status = (ImageView) view.findViewById(R.id.events_entry_status);
				TextView host = (TextView) view.findViewById(R.id.events_entry_host);
				TextView description = (TextView) view.findViewById(R.id.events_entry_description);
				TextView clock = (TextView) view.findViewById(R.id.events_entry_clock);

				if (cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_VALUE)) == 0) {
					status.setImageResource(R.drawable.ok);
				} else {
					status.setImageResource(R.drawable.problem);
				}
				String host_str = cursor.getString(cursor.getColumnIndex(EventData.COLUMN_HOSTS));
				host.setText(host_str);
				String description_str = cursor.getString(cursor.getColumnIndex(TriggerData.COLUMN_DESCRIPTION));
				description.setText(description_str==null?"":description_str.replace("{HOSTNAME}", host_str));
				int unixtime = cursor.getInt(cursor.getColumnIndex(EventData.COLUMN_CLOCK));
				Date date = new Date(((long) unixtime)*1000);
				java.text.DateFormat df = DateFormat.getDateFormat(mActivity.getApplicationContext());
				java.text.DateFormat tf = DateFormat.getTimeFormat(mActivity.getApplicationContext());
				clock.setText(df.format(date)+ " " +tf.format(date));

				view.setTag(
						cursor.getLong(cursor.getColumnIndex(EventData.COLUMN_EVENTID))
						+"##.##"+cursor.getLong(cursor.getColumnIndex(TriggerData.COLUMN_TRIGGERID))
				);
			}
		});
	}

	public void setupListProblemsAdapter(ListView listProblems) {
		listProblems.setAdapter(
				new ResourceCursorAdapter(mActivity.getApplicationContext(), R.layout.list_triggers_entry, null) {
					@Override
					public void bindView(View view, Context arg1, Cursor cursor) {
						ImageView status = (ImageView) view.findViewById(R.id.triggers_entry_status);
						TextView priority = (TextView) view.findViewById(R.id.triggers_entry_priority);
						TextView description = (TextView) view.findViewById(R.id.triggers_entry_description);
						TextView clock = (TextView) view.findViewById(R.id.triggers_entry_clock);

						int severity = cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY));
						if (severity >= 4) {
							status.setImageResource(R.drawable.severity_high);
							status.setTag("severity_high"); // for unit test
						} else {
							status.setImageResource(R.drawable.severity_avg);
							status.setTag("severity_avg"); // for unit test
						}
						priority.setText(MainActivitySmartphone.getTriggerPriorityText(mActivity, severity));
						description.setText(cursor.getString(cursor.getColumnIndex(TriggerData.COLUMN_DESCRIPTION)).replace("{HOSTNAME}", cursor.getString(cursor.getColumnIndex(HostData.COLUMN_HOST))));
						int unixtime = cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_LASTCHANGE));
						Date date = new Date(((long) unixtime)*1000);
						java.text.DateFormat df = DateFormat.getDateFormat(mActivity.getApplicationContext());
						java.text.DateFormat tf = DateFormat.getTimeFormat(mActivity.getApplicationContext());
						clock.setText(df.format(date)+ " " +tf.format(date));

						// store trigger id in the tag
						view.setTag(cursor.getLong(cursor.getColumnIndex(TriggerData.COLUMN_TRIGGERID)));
					}
				}
		);
	}

	public void showScreenGraphs(Cursor cursor, View emptyView, LinearLayout layoutGraphs) {
		// struct data
		Map<Long, String> graphNames = new HashMap<Long, String>(); // graphid => name
		Map<Long, Map<Long, List<GraphViewData>>> graphData = new HashMap<Long, Map<Long, List<GraphViewData>>>(); // graphid => graphitemid => data
		Map<Long, String> graphItemNames = new HashMap<Long, String>(); // graphitemid => name
		Map<Long, Integer> graphItemColors = new HashMap<Long, Integer>(); // graphitemid => color

		while (cursor.moveToNext()) {
			long graphid = cursor.getLong(cursor.getColumnIndex(GraphData.COLUMN_GRAPHID));
			long graphitemid = cursor.getLong(cursor.getColumnIndex(GraphItemData.COLUMN_GRAPHITEMID));
			graphNames.put(
					graphid,
					cursor.getString(cursor.getColumnIndex(HostData.COLUMN_HOST))
					+ ": " + cursor.getString(cursor.getColumnIndex(GraphData.COLUMN_NAME))
			);
			graphItemNames.put(graphitemid, cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_DESCRIPTION)));
			graphItemColors.put(graphitemid, cursor.getInt(cursor.getColumnIndex(GraphItemData.COLUMN_COLOR)));

			// each graph has a set of GraphItems (series)
			Map<Long, List<GraphViewData>> graphItems = graphData.get(graphid);
			if (graphItems == null) {
				graphItems = new HashMap<Long, List<GraphViewData>>();
				graphData.put(graphid, graphItems);
			}
			// each GraphItem has a set of data
			List<GraphViewData> data = graphItems.get(graphitemid);
			if (data == null) {
				data = new ArrayList<GraphViewData>();
				graphItems.put(graphitemid, data);
			}
			data.add(new GraphViewData(cursor.getInt(cursor.getColumnIndex(HistoryDetailData.COLUMN_CLOCK)), cursor.getDouble(cursor.getColumnIndex(HistoryDetailData.COLUMN_VALUE))));
		}

		layoutGraphs.removeAllViews();

		// build graphs
		final java.text.DateFormat dateTimeFormatter = DateFormat.getTimeFormat(mActivity);
		// iterate graphs
		for (Map.Entry<Long, String> entry : graphNames.entrySet()) {
			long graphid = entry.getKey();
			LineGraphView graphView = new LineGraphView(mActivity, entry.getValue()) {
				@Override
				protected String formatLabel(double value, boolean isValueX) {
					if (isValueX) {
						// transform number to time
						return dateTimeFormatter.format(new Date((long) value*1000));
					} else return super.formatLabel(value, isValueX);
				}
			};
			graphView.setShowLegend(true);
			graphView.setLegendAlign(LegendAlign.TOP);
			graphView.setLegendWidth(250);

			// iterate series
			long highestclock = Long.MIN_VALUE;
			long lowestclock = Long.MAX_VALUE;
			Map<Long, List<GraphViewData>> series = graphData.get(graphid);
			for (Map.Entry<Long, List<GraphViewData>> seriesEntry : series.entrySet()) {
				List<GraphViewData> data = seriesEntry.getValue();
				GraphViewData[] dataArray = data.toArray(new GraphViewData[data.size()]);
				highestclock = Math.max(highestclock, (long) dataArray[dataArray.length-1].valueX);
				lowestclock = Math.min(lowestclock, (long) dataArray[0].valueX);
				graphView.addSeries(new GraphViewSeries(
						graphItemNames.get(seriesEntry.getKey()),
						new GraphViewSeriesStyle(graphItemColors.get(seriesEntry.getKey()), 3),
						dataArray
				));
			}
			long size = (highestclock-lowestclock)*2/3; // we show 2/3
			graphView.setViewPort(highestclock - size, size);
			graphView.setScalable(true);

			LinearLayout l = new LinearLayout(mActivity);
			l.addView(graphView);
			layoutGraphs.addView(l, new LayoutParams(LayoutParams.FILL_PARENT, 200));
		}

		// if there are no graphs, show empty view
		emptyView.setVisibility(graphNames.size() == 0 ? View.VISIBLE : View.GONE);
	}
}
