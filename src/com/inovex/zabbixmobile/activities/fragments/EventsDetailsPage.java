package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Event;

public class EventsDetailsPage extends SherlockFragment {
	
	public static final String ARG_CATEGORY_NUMBER = "category_number";
	public static final String ARG_EVENT_NUMBER = "event_number";
	public static final String ARG_EVENT_DETAILS_TEXT = "event_details_text";
	
	int categoryNumber;
	private Event event;
	private String eventDetailsText;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	public void setEvent(Event e) {
		this.event = e;
//		eventDetailsText = e.getDetails();
		System.out.println("DEBUG: EventDetailsFragment.setEvent(): " + e);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_events_details, null);
		if(savedInstanceState != null) 
			eventDetailsText = savedInstanceState.getString(ARG_EVENT_DETAILS_TEXT);
		if(eventDetailsText == null)
			eventDetailsText = "default details text";
			
		System.out.println("Dummy: onCreateView()");
		Bundle args = getArguments();
		categoryNumber = args.getInt(ARG_CATEGORY_NUMBER);
		TextView text = (TextView) rootView.findViewById(R.id.details_text);
		text.setText(eventDetailsText);
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_EVENT_DETAILS_TEXT, eventDetailsText);
	}
	
	public String getTitle() {
		System.out.println("Dummy: getTitle()");
		Bundle args = this.getArguments();
		return args.getString(ARG_EVENT_NUMBER);
	}
	
}
