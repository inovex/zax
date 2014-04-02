package com.inovex.zabbixmobile.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.model.ZaxServerPreferences;
import com.inovex.zabbixmobile.push.PushService;
import com.inovex.zabbixmobile.widget.WidgetUpdateBroadcastReceiver;

/**
 * The preference activity.
 *
 */
public class ZabbixServerPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final int REQUEST_CODE_PREFERENCES_THEMED = 958723;
	private static final String ARG_ACTIVITY_RESULT = "ACTIVITY_RESULT";
	public static final int PREFERENCES_CHANGED_SERVER = 1;
	public static final int PREFERENCES_CHANGED_PUSH = 2;
	public static final int PREFERENCES_CHANGED_WIDGET = 4;
	public static final int PREFERENCES_CHANGED_THEME = 8;
	private static final String TAG = "ZabbixServerPreferenceActivity";
	public static final String ARG_ZABBIX_SERVER_ID = "ZABBIX_SERVER_ID";

	private ZaxServerPreferences mPrefs;
	private int activityResult = 0;
	private ActionBar mActionBar;
	private boolean recursion;

	// We use the deprecated method because it is compatible to old Android
	// versions.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ZaxPreferences prefs = ZaxPreferences.getInstance(getApplicationContext());
		if (prefs.isDarkTheme())
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

		long zabbixServer = getIntent().getLongExtra(ARG_ZABBIX_SERVER_ID, -1);
		mPrefs = new ZaxServerPreferences(getApplicationContext(), zabbixServer, false);

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
		if (recursion) {
			return;
		}
		recursion = true;
		mPrefs.savePrefs();
		recursion = false;

		if (key.equals("zabbix_url") || key.equals("zabbix_username")
				|| key.equals("zabbix_password")
				|| key.equals("zabbix_trust_all_ssl_ca")
				|| key.equals("http_auth_enabled")
				|| key.equals("http_auth_username")
				|| key.equals("http_auth_password")) {
			activityResult |= PREFERENCES_CHANGED_SERVER;
		}
		if (key.equals("zabbix_push_enabled")
				|| key.equals("zabbix_push_subscribe_key")
				|| key.equals("zabbix_push_ringtone")
				|| key.equals("zabbix_push_old_icons")) {
			activityResult |= PREFERENCES_CHANGED_PUSH;
			PushService.killPushService(getApplicationContext());
			if (!mPrefs.isPushEnabled()
					|| mPrefs.getPushSubscribeKey().length() > 0) {
				PushService.startOrStopPushService(getApplicationContext(), false);
			}
		}
		// show hint for pubsub configuration
		if (key.equals("zabbix_push_enabled") && mPrefs.isPushEnabled()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"To use Push Notifications, you have to configure your Zabbix server. Please read the Howto at http://inovex.github.io/zax/#howto_push")
					.setCancelable(false)
					.setPositiveButton("View Howto in Browser",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									Intent viewIntent = new Intent(
											"android.intent.action.VIEW",
											Uri.parse("http://inovex.github.io/zax/#howto_push"));
									startActivity(viewIntent);
								}
							})
					.setNegativeButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}

		if (key.equals("widget_refresh_interval_mins")) {
			activityResult |= PREFERENCES_CHANGED_WIDGET;
			Intent intent = new Intent(getApplicationContext(),
					WidgetUpdateBroadcastReceiver.class);
			intent.putExtra(WidgetUpdateBroadcastReceiver.REFRESH_RATE_CHANGED,
					true);
			this.sendBroadcast(intent);
		}
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
