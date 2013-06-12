package com.inovex.zabbixmobile.activities.fragments;

import java.sql.SQLException;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.DataAccess;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsDetailsPage extends SherlockFragment {

	public static final String ARG_EVENT_ID = "event_id";

	private long mEventId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_events_details, null);
		if (savedInstanceState != null)
			mEventId = savedInstanceState.getLong(ARG_EVENT_ID, -1);
		Bundle args = getArguments();
		if (args != null)
			mEventId = args.getLong(ARG_EVENT_ID, -1);

		DataAccess dataAccess = DataAccess.getInstance(getSherlockActivity());
		StringBuilder sb = new StringBuilder();
		try {
			Event e = dataAccess.getEventById(mEventId);
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextView text = (TextView) rootView.findViewById(R.id.details_text);
		text.setText(sb.toString());
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(ARG_EVENT_ID, mEventId);
	}

	public String getTitle() {
		System.out.println("Dummy: getTitle()");
		Bundle args = this.getArguments();
		return args.getString(ARG_EVENT_ID);
	}

}
