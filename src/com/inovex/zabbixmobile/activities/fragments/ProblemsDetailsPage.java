package com.inovex.zabbixmobile.activities.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class ProblemsDetailsPage extends BaseServiceConnectedFragment {

	private static final String TAG = ProblemsDetailsPage.class.getSimpleName();

	private static final String ARG_TRIGGER_ID = "arg_trigger_id";
	private Trigger mTrigger;
	private String mTitle = "";
	private long mTriggerId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_problems_details, null);
		if (savedInstanceState != null)
			mTriggerId = savedInstanceState.getLong(ARG_TRIGGER_ID, -1);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		fillDetailsText();
	}

	private void fillDetailsText() {
		if (mTrigger != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nTrigger:\n\n");
			sb.append("ID: " + mTrigger.getId() + "\n");
			sb.append("severity: " + mTrigger.getPriority() + "\n");
			sb.append("status: " + mTrigger.getStatus() + "\n");
			sb.append("description: " + mTrigger.getDescription() + "\n");
			sb.append("comments: " + mTrigger.getComments() + "\n");
			sb.append("expression: " + mTrigger.getExpression() + "\n");
			sb.append("URL: " + mTrigger.getUrl() + "\n");
			sb.append("value: " + mTrigger.getValue() + "\n");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mTrigger.getLastChange());
			DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
					SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
					Locale.getDefault());
			sb.append("lastchange: "
					+ String.valueOf(dateFormatter.format(cal.getTime()))
					+ "\n");
			TextView text = (TextView) getView().findViewById(
					R.id.trigger_details);
			text.setText(sb.toString());
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		// if the trigger is not set, this fragment was apparently restored and
		// we
		// need to refresh the event data
		if (mTrigger == null) {
			Log.d(TAG, "trigger was null, loading trigger from database.");
			this.mTrigger = mZabbixDataService.getTriggerById(mTriggerId);
			fillDetailsText();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(ARG_TRIGGER_ID, mTriggerId);
		super.onSaveInstanceState(outState);
	}

	public void setTrigger(Trigger trigger) {
		this.mTrigger = trigger;
		this.mTriggerId = trigger.getId();
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

}
