package com.inovex.zabbixmobile.activities.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class EventsDetailsPage extends SherlockFragment {

	private static final String ARG_EVENT_ID = "arg_event_id";
	private static final String TAG = EventsDetailsPage.class.getSimpleName();
	private Event mEvent;
	private String mTitle = "";
	private long mEventId = -1;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
			mEventId = savedInstanceState.getLong(ARG_EVENT_ID, -1);
		Log.d(TAG, "onCreate: " + this.toString());
		Log.d(TAG, "mEventId: " + mEventId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_events_details, null);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO: on orientation change, mEvent is not set ->
		// NullPointerException
		StringBuilder sb = new StringBuilder();
		if (mEvent != null) {
			sb.append("Event: \n\n");
			sb.append("ID: " + mEvent.getId() + "\n");
			sb.append("source: " + mEvent.getObjectId() + "\n");
			sb.append("value: " + mEvent.getValue() + "\n");
			sb.append("clock: " + mEvent.getClock() + "\n");
			Trigger t = mEvent.getTrigger();
			if (t != null) {
				sb.append("\nTrigger:\n\n");
				sb.append("ID: " + t.getId() + "\n");
				sb.append("severity: " + t.getPriority() + "\n");
				sb.append("status: " + t.getStatus() + "\n");
				sb.append("description: " + t.getDescription() + "\n");
				sb.append("comments: " + t.getComments() + "\n");
				sb.append("expression: " + t.getExpression() + "\n");
				sb.append("URL: " + t.getUrl() + "\n");
				sb.append("value: " + t.getValue() + "\n");
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(t.getLastChange());
				DateFormat dateFormatter = SimpleDateFormat
						.getDateTimeInstance(SimpleDateFormat.SHORT,
								SimpleDateFormat.SHORT, Locale.getDefault());
				sb.append("lastchange: "
						+ String.valueOf(dateFormatter.format(cal.getTime()))
						+ "\n");
			}
			TextView text = (TextView) getView()
					.findViewById(R.id.checks_title);
			text.setText(sb.toString());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(ARG_EVENT_ID, mEventId);
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState: " + this.toString());
	}

	public void setEvent(Event event) {
		this.mEvent = event;
		this.mEventId = event.getId();
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach: " + this.toString());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy: " + this.toString());
	}

}
