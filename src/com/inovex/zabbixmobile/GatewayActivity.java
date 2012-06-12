package com.inovex.zabbixmobile;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.inovex.zabbixmobile.activities.MainActivitySmartphone;
import com.inovex.zabbixmobile.activities.MainActivityTablet;

public class GatewayActivity extends Activity {
	private boolean isTabletDevice() {
		if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
			// test screen size, use reflection because isLayoutSizeAtLeast is only available since 11
			Configuration con = getResources().getConfiguration();
			try {
				Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
				Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con, 0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
				return r;
			} catch (Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent();

		// was the activity started from a push notification?
		Intent startedIntent = getIntent();
		if (startedIntent != null) {
			long triggerid = startedIntent.getLongExtra("pushNotificationTriggerid", 0);
			if (triggerid != 0) {
				intent.putExtra("pushNotificationTriggerid", triggerid);
				startedIntent.removeExtra("pushNotificationTriggerid");
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		if (isTabletDevice()) {
			intent.setClass(this, MainActivityTablet.class);
		} else {
			intent.setClass(this, MainActivitySmartphone.class);
		}
		startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		finish();
	}
}
