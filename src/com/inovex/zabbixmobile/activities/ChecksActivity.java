package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;

public class ChecksActivity extends BaseActivity {
	
	private static final String TAG = ChecksActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checks);

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_checks);

		TextView textView = new TextView(this);
		textView.setText("Checks activity");
		baseLayout.addView(textView);

	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);
		mZabbixService.loadApplications();
	}

}
