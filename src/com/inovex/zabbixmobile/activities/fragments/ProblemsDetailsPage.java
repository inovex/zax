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
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class ProblemsDetailsPage extends BaseDetailsPage {

	private static final String TAG = ProblemsDetailsPage.class.getSimpleName();

	private static final String ARG_TRIGGER_ID = "arg_trigger_id";
	Trigger mTrigger;
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
	protected void fillDetailsText() {
		if (mTrigger != null) {

			((TextView) getView().findViewById(R.id.trigger_details_host))
					.setText(mTrigger.getHostNames());
			((TextView) getView().findViewById(R.id.trigger_details_trigger))
					.setText(mTrigger.getDescription());
			((TextView) getView().findViewById(R.id.trigger_details_severity))
					.setText(mTrigger.getPriority().getNameResourceId());
			((TextView) getView().findViewById(R.id.trigger_details_expression))
					.setText(mTrigger.getExpression());

			Item i = mTrigger.getItem();
			if (i != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(i.getLastClock());
				DateFormat dateFormatter = SimpleDateFormat
						.getDateTimeInstance(SimpleDateFormat.SHORT,
								SimpleDateFormat.SHORT, Locale.getDefault());
				cal.setTimeInMillis(i.getLastClock());
				((TextView) getView().findViewById(R.id.latest_data)).setText(i
						.getLastValue()
						+ i.getUnits()
						+ " "
						+ getResources().getString(R.string.at)
						+ " "
						+ dateFormatter.format(cal.getTime()));
			}
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
		}
		if (mTrigger != null) {
			fillDetailsText();
			if (!mHistoryDetailsImported && mTrigger.getItem() != null)
				mZabbixDataService.loadHistoryDetailsByItem(mTrigger.getItem(),
						false, this);
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
		if (!mHistoryDetailsImported && getView() != null)
			mZabbixDataService.loadHistoryDetailsByItem(mTrigger.getItem(),
					false, this);
	}

	@Override
	protected void showGraph() {
		showGraph(mTrigger.getItem());
	}

	/**
	 * Refreshes this page's view by reloading the trigger from the database.
	 */
	public void refresh() {
		this.mTrigger = mZabbixDataService.getTriggerById(mTriggerId);
		fillDetailsText();
	}

}
