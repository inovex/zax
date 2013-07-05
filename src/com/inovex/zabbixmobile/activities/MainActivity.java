package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.model.ZaxPreferences;

public class MainActivity extends BaseActivity {

	protected static final String TAG = MainActivity.class.getSimpleName();

	private ListView mProblemsList;
	private Button mProblemsButton;

	private MenuListAdapter mListAdapter;

	protected class MenuListAdapter extends ArrayAdapter<String> {

		private boolean mEnabled = true;

		public MenuListAdapter(Context context, int resource, String[] objects) {
			super(context, resource, objects);
		}

		@Override
		public boolean isEnabled(int position) {
			return mEnabled;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return mEnabled;
		}

		// TODO: adjust view!
		public void setEnabled(boolean enabled) {
			mEnabled = enabled;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setHomeButtonEnabled(false);

		ListView listView = (ListView) findViewById(R.id.main_activities);
		mListAdapter = new MenuListAdapter(this,
				android.R.layout.simple_expandable_list_item_1, getResources()
						.getStringArray(R.array.activities));
		listView.setAdapter(mListAdapter);

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

		mProblemsList = (ListView) findViewById(R.id.main_problems_list);
		mProblemsButton = (Button) findViewById(R.id.main_problems_button);

		mProblemsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						ProblemsActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});

	}

	@Override
	protected void disableUI() {
		mProblemsButton.setEnabled(false);
		mListAdapter.setEnabled(false);
	}

	@Override
	protected void enableUI() {
		mProblemsButton.setEnabled(true);
		mListAdapter.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuitem_preferences) {
			Intent intent = new Intent(getApplicationContext(),
					ZaxPreferenceActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		return false;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);

		if (new ZaxPreferences(this).isConfigurated()) {

//			mZabbixService.performZabbixLogin(this);

			BaseServiceAdapter<Trigger> adapter = mZabbixDataService
					.getProblemsListAdapter(TriggerSeverity.ALL);
			mProblemsList.setAdapter(adapter);
			mProblemsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Log.d(TAG, "onItemClick(pos: " + position + ", id: " + id);
					Bundle args = new Bundle();
					Intent intent = new Intent(MainActivity.this,
							ProblemsActivity.class);
					intent.putExtra(ProblemsActivity.ARG_ITEM_POSITION,
							position);
					intent.putExtra(ProblemsActivity.ARG_ITEM_ID, id);
					startActivity(intent);
				}
			});
			mZabbixDataService.loadTriggersBySeverityAndHostGroup(
					TriggerSeverity.ALL, HostGroup.GROUP_ID_ALL, true);
		}

	}

	@Override
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		boolean useMockData = getIntent().getBooleanExtra(
				ZabbixDataService.EXTRA_IS_TESTING, false);
		intent.putExtra(ZabbixDataService.EXTRA_IS_TESTING, useMockData);
		getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

}
