package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.OnEventLoadedListener;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsDetailsPage extends SherlockFragment implements
		ServiceConnection, OnEventLoadedListener {

	private long mEventId;
	private String mTitle = "";
	private ZabbixDataService mZabbixDataService;
	private boolean mZabbixServiceBound;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_events_details, null);
		// if (savedInstanceState != null)
		// mEventId = savedInstanceState.getLong(ARG_EVENT_ID, -1);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		// we need to do this after the view was created!!
		Intent intent = new Intent(getSherlockActivity(),
				ZabbixDataService.class);
		getSherlockActivity().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		getSherlockActivity().unbindService(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// outState.putLong(ARG_EVENT_ID, mEventId);
		super.onSaveInstanceState(outState);
	}

	public void setEventId(long eventId) {
		this.mEventId = eventId;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// We've bound to LocalService, cast the IBinder and get
		// LocalService instance
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();
		mZabbixServiceBound = true;

		mZabbixDataService.loadEventById(mEventId, this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixServiceBound = false;

	}

	@Override
	public void onEventLoaded(Event e) {
		StringBuilder sb = new StringBuilder();
		sb.append("Event: \n\n");
		sb.append("ID: " + e.getId() + "\n");
		sb.append("source: " + e.getSource() + "\n");
		sb.append("value: " + e.getValue() + "\n");
		sb.append("clock: " + e.getClock() + "\n");
		Trigger t = e.getTrigger();
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
			sb.append("lastchange: " + t.getLastchange() + "\n");
		}
		TextView text = (TextView) getView().findViewById(R.id.details_text);
		text.setText(sb.toString());
	}

}
