package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;

/**
 * The preference activity.
 * 
 */
public class ZaxPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private String zabbixUrl;
	private String userName;
	private String password;
	private ZaxPreferences mPrefs;
	private boolean trustSSL;
	private boolean httpAuth;
	private String httpUser;
	private String httpPassword;

	// We use the deprecated method because it is compatible to old Android
	// versions.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = ZaxPreferences.getInstance(this);
		zabbixUrl = mPrefs.getZabbixUrl();
		userName = mPrefs.getUsername();
		password = mPrefs.getPassword();
		trustSSL = mPrefs.isTrustAllSSLCA();
		httpAuth = mPrefs.isHttpAuthEnabled();
		httpUser = mPrefs.getHttpAuthUsername();
		httpPassword = mPrefs.getHttpAuthPassword();
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		inferResult();
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		inferResult();
		finish();
	}

	private void inferResult() {
		ZaxPreferences prefs = ZaxPreferences.getInstance(this);
		Intent returnIntent = new Intent();
		if (!prefs.getZabbixUrl().equals(zabbixUrl)
				|| !prefs.getUsername().equals(userName)
				|| !prefs.getPassword().equals(password)
				|| prefs.isTrustAllSSLCA() != trustSSL
				|| prefs.isHttpAuthEnabled() != httpAuth
				|| !prefs.getHttpAuthUsername().equals(httpUser)
				|| !prefs.getHttpAuthPassword().equals(httpPassword))
			setResult(BaseActivity.RESULT_PREFERENCES_CHANGED, returnIntent);
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
		if (key.equals("widget_refresh_interval_mins")) {
			Intent intent = new Intent();
			intent.setAction("com.inovex.zabbixmobile.WIDGET_UPDATE");
			this.sendBroadcast(intent);
		}
	}

}
