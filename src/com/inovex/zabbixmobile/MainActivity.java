package com.inovex.zabbixmobile;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.inovex.zabbixmobile.activities.ChecksActivity;
import com.inovex.zabbixmobile.activities.EventsActivity;
import com.inovex.zabbixmobile.activities.ScreensActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends SherlockFragmentActivity {

	protected static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		ListView listView = (ListView) findViewById(R.id.main_activities);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.activities,
				android.R.layout.simple_expandable_list_item_1);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				Intent intent = null;
				switch (position) {
				case 0:
					intent = new Intent(MainActivity.this, EventsActivity.class);
					break;
				case 1:
					intent = new Intent(MainActivity.this, ChecksActivity.class);
					break;
				case 2:
					intent = new Intent(MainActivity.this,
							ScreensActivity.class);
					break;
				default:
					return;
				}
				MainActivity.this.startActivity(intent);
			}
		});

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_main);

		Button serviceButton = new Button(this);
		serviceButton.setText(R.string.call_service);
		serviceButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// int num = mZabbixService.getRandomNumber();
				// Toast.makeText(MainActivity.this, "number: " + num,
				// Toast.LENGTH_SHORT).show();
			}
		});

		baseLayout.addView(serviceButton);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
