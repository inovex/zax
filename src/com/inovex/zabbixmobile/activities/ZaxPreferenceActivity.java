package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;

public class ZaxPreferenceActivity extends PreferenceActivity {

	private String zabbixUrl;
	private String userName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Save username and url to check if they were changed later.
		// Unfortunately, the OnSharedPreferenceChangeListener didn't work
		// properly, hence we do it this way.
		ZaxPreferences prefs = new ZaxPreferences(this);
		zabbixUrl = prefs.getZabbixUrl();
		userName = prefs.getUsername();
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onBackPressed() {
		ZaxPreferences prefs = new ZaxPreferences(this);
		Intent returnIntent = new Intent();
		if(prefs.getZabbixUrl() != zabbixUrl || prefs.getUsername() != userName)
			setResult(BaseActivity.RESULT_PREFERENCES_CHANGED, returnIntent);
		finish();
	}

}
