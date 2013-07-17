package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;

public class ScreensActivity extends BaseActivity {

	private static final String TAG = ScreensActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screens);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_screens);

		TextView textView = new TextView(this);
		textView.setText("Screens activity");
		baseLayout.addView(textView);

	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		mZabbixDataService.loadScreens();
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
	protected void disableUI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void enableUI() {
		// TODO Auto-generated method stub
		
	}

}
