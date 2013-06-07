package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;

public class ChecksActivity extends SherlockFragmentActivity {
	
	private static final String TAG = ChecksActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checks);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_checks);

		TextView textView = new TextView(this);
		textView.setText("Checks activity");
		baseLayout.addView(textView);

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
