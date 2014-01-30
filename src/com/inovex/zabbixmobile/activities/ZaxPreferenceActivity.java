package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;

/**
 * The preference activity.
 * 
 */
public class ZaxPreferenceActivity extends PreferenceActivity {

	private String zabbixUrl;
	private String userName;
	private String password;

	// We use the deprecated method because it is compatible to old Android
	// versions.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Save username and url to check if they were changed later.
		// Unfortunately, the OnSharedPreferenceChangeListener didn't work
		// properly, hence we do it this way.
		ZaxPreferences prefs = ZaxPreferences.getInstance(this);
		zabbixUrl = prefs.getZabbixUrl();
		userName = prefs.getUsername();
		password = prefs.getPassword();
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		ZaxPreferences prefs = ZaxPreferences.getInstance(this);
		prefs.apply();
		Intent returnIntent = new Intent();
		if (!prefs.getZabbixUrl().equals(zabbixUrl)
				|| !prefs.getUsername().equals(userName)
				|| !prefs.getPassword().equals(password))
			setResult(BaseActivity.RESULT_PREFERENCES_CHANGED, returnIntent);
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		ZaxPreferences prefs = ZaxPreferences.getInstance(this);
		Intent returnIntent = new Intent();
		if (!prefs.getZabbixUrl().equals(zabbixUrl)
				|| !prefs.getUsername().equals(userName)
				|| !prefs.getPassword().equals(password))
			setResult(BaseActivity.RESULT_PREFERENCES_CHANGED, returnIntent);
		finish();
	}

}
