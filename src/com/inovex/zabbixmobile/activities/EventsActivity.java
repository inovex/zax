	package com.inovex.zabbixmobile.activities;

import java.sql.SQLException;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnEventSelectedListener;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.inovex.zabbixmobile.model.Trigger;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class EventsActivity extends SherlockFragmentActivity implements OnEventSelectedListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private static final String FRAGMENT_EVENTS_LIST = "fragment_event_list";

	private DatabaseHelper databaseHelper = null;
	private FragmentManager fragmentManager;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	private DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,
					MockDatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);
		
		fragmentManager = getSupportFragmentManager();

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_events);
		
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.layout_events, new EventsListFragment(), FRAGMENT_EVENTS_LIST);
		ft.commit();

//		TextView textView = new TextView(this);
//		textView.setText("Events activity");
//		baseLayout.addView(textView);
//		
//		getHelper();
//		
//		try {
//			TextView textView2 = new TextView(this);
//			doAccountDatabaseStuff(textView2);
//			baseLayout.addView(textView2);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
	
	private void doAccountDatabaseStuff(TextView tv) throws SQLException {

		// clear database
		databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 0, 0);
		
		StringBuilder sb = new StringBuilder();
		
//		Event e = new Event(0, 0, System.currentTimeMillis(), 1, false, false);
//		databaseHelper.getDao(Event.class).create(e);

		sb.append("Events:\n");
		Dao<Event, Integer> eventDao = databaseHelper.getDao(Event.class);

		for (Event event : eventDao) {
			sb.append(event + "\n\n");
		}
		
		sb.append("\n\nTriggers:\n");
		Dao<Trigger, Integer> triggerDao = databaseHelper.getDao(Trigger.class);

		for (Trigger trigger : triggerDao) {
			sb.append(trigger + "\n\n");
		}
		tv.setText(sb.toString());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			try {
				finish();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
		return false;
	}

	@Override
	public void onEventSelected(int position, int severity, long id) {
		Log.d(TAG, "event selected: " + id + ",severity: " + severity + "(position: " + position + ")");
		EventsDetailsFragment f = new EventsDetailsFragment();
		Bundle args = new Bundle();
		args.putLong(EventsDetailsFragment.ARG_EVENT_ID, id);
		args.putInt(EventsDetailsFragment.ARG_EVENT_POSITION, position);
		args.putInt(EventsDetailsFragment.ARG_SEVERITY, severity);
		f.setArguments(args);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.remove(fragmentManager.findFragmentByTag(FRAGMENT_EVENTS_LIST));
		ft.add(R.id.layout_events, f);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onEventClicked(int position) {
		// TODO Auto-generated method stub
		
	}

}
