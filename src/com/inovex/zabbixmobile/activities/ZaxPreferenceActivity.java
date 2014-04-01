package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;

/**
 * The preference activity.
 *
 */
public class ZaxPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final int REQUEST_CODE_PREFERENCES_THEMED = 958723;
	private static final String ARG_ACTIVITY_RESULT = "ACTIVITY_RESULT";
	public static final int PREFERENCES_CHANGED_SERVER = 1;
	public static final int PREFERENCES_CHANGED_PUSH = 2;
	public static final int PREFERENCES_CHANGED_WIDGET = 4;
	public static final int PREFERENCES_CHANGED_THEME = 8;

	private ZaxPreferences mPrefs;
	private int activityResult = 0;
	private ActionBar mActionBar;

	// We use the deprecated method because it is compatible to old Android
	// versions.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPrefs = ZaxPreferences.getInstance(getApplicationContext());
		if (mPrefs.isDarkTheme())
			setTheme(R.style.AppThemeDark);
		else
			setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		mActionBar = getSupportActionBar();

		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(true);
		}

		// the activity might have been started with a result code which we need
		// to adopt
		activityResult = getIntent().getIntExtra(ARG_ACTIVITY_RESULT,
				activityResult);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		setResult(activityResult, new Intent());
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(activityResult, new Intent());
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		if (key.equals("dark_theme")) {
			activityResult |= PREFERENCES_CHANGED_THEME;
			// we start a new preference activity with changed theme
			Intent intent = getIntent();
			// we have to pass through the result code
			intent.putExtra(ARG_ACTIVITY_RESULT, activityResult);
			// finish();
			startActivityForResult(intent, REQUEST_CODE_PREFERENCES_THEMED);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// BaseActivity expects a result code, so we pass it through from the
		// started themed preference activity
		if (requestCode == REQUEST_CODE_PREFERENCES_THEMED) {
			setResult(resultCode, new Intent());
			finish();
		}
	}
}
