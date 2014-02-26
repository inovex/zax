package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.PushService;
import com.inovex.zabbixmobile.widget.WidgetUpdateBroadcastReceiver;

/**
 * The preference activity.
 * 
 */
public class ZaxPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private ZaxPreferences mPrefs;

	// We use the deprecated method because it is compatible to old Android
	// versions.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPrefs = ZaxPreferences.getInstance(getApplicationContext());
		if(mPrefs.isDarkTheme())
			setTheme(R.style.AppThemeDark);
		else
			setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		setResult(BaseActivity.RESULT_PREFERENCES_CHANGED, new Intent());
		if (key.equals("widget_refresh_interval_mins")) {
			Intent intent = new Intent(getApplicationContext(),
					WidgetUpdateBroadcastReceiver.class);
			intent.putExtra(WidgetUpdateBroadcastReceiver.REFRESH_RATE_CHANGED,
					true);
			this.sendBroadcast(intent);
		}
		if (key.equals("dark_theme")) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
		if (key.equals("zabbix_push_enabled")
				|| key.equals("zabbix_push_subscribe_key")
				|| key.equals("zabbix_push_ringtone")
				|| key.equals("zabbix_push_old_icons")) {
			PushService.killPushService(getApplicationContext());
			PushService.startOrStopPushService(getApplicationContext());
		}

	}
}
