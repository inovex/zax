package com.inovex.zabbixmobile.activities.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class EventsDetailsPage extends BaseDetailsPage {

	private static final String TAG = EventsDetailsPage.class.getSimpleName();

	private static final String ARG_EVENT_ID = "arg_event_id";
	private Event mEvent;
	private String mTitle = "";
	private long mEventId = -1;
	
	private boolean mHistoryDetailsImported = false;

	private Collection<HistoryDetail> mHistoryDetails;

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
	protected void fillDetailsText() {

		if (mEvent != null) {
			StringBuilder sb = new StringBuilder();
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
			TextView detailsText = (TextView) getView().findViewById(
					R.id.event_details);
			detailsText.setText(sb.toString());

		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		// if the event is not set, this fragment was apparently restored and we
		// need to refresh the event data
		if (mEvent == null) {
			Log.d(TAG, "event was null, loading event from database.");
			this.mEvent = mZabbixDataService.getEventById(mEventId);
			fillDetailsText();
		}
		
		if(!mHistoryDetailsImported && mEvent.getTrigger().getItem() != null)
			mZabbixDataService.loadHistoryDetailsByItemId(mEvent.getTrigger().getItem().getId(), this);
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
		if(!mHistoryDetailsImported && getView() != null)
			mZabbixDataService.loadHistoryDetailsByItemId(mEvent.getTrigger().getItem().getId(), this);
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}
	
	@Override
	protected void showGraph() {
		showGraph(mEvent.getTrigger().getItem());
	}
}
