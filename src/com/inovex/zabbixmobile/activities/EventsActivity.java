package com.inovex.zabbixmobile.activities;

import java.sql.SQLException;
import java.util.Random;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class EventsActivity extends SherlockFragmentActivity {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private DatabaseHelper databaseHelper = null;

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
					DatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_events);

		TextView textView = new TextView(this);
		textView.setText("Events activity");
		baseLayout.addView(textView);
		
		getHelper();
		
		try {
			TextView textView2 = new TextView(this);
			doAccountDatabaseStuff(textView2);
			baseLayout.addView(textView2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void doAccountDatabaseStuff(TextView tv) throws SQLException {

		// clear database
//		databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), 0, 0);
		
		StringBuilder sb = new StringBuilder();
		
		Event e = new Event(databaseHelper, System.currentTimeMillis());

		Dao<Event, Integer> eventDao = databaseHelper.getDao(Event.class);

		for (Event event : eventDao) {
			sb.append(event + "\n");
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

}
