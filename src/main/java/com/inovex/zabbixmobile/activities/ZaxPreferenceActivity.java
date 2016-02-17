/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.gcm.RegistrationIntentService;
import com.inovex.zabbixmobile.push.pubnub.PubnubPushService;
import com.inovex.zabbixmobile.widget.WidgetUpdateBroadcastReceiver;

/**
 * The preference activity.
 *
 */
public class ZaxPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final int REQUEST_CODE_PREFERENCES_THEMED = 958723;
	private static final String ARG_ACTIVITY_RESULT = "ACTIVITY_RESULT";
	public static final int PREFERENCES_CHANGED_SERVER = 1;
	public static final int PREFERENCES_CHANGED_PUSH = 2;
	public static final int PREFERENCES_CHANGED_WIDGET = 4;
	public static final int PREFERENCES_CHANGED_THEME = 8;

	private ZaxPreferences mPrefs;
	private int activityResult = 0;
	private Toolbar mToolbar;
	private String TAG = "ZaxPreferenceActivity";

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

		// the activity might have been started with a result code which we need
		// to adopt
		activityResult = getIntent().getIntExtra(ARG_ACTIVITY_RESULT,
				activityResult);

		addPreferencesFromResource(R.xml.preferences);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar_main, root, false);
		bar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		bar.setTitle(getString(R.string.preferences));
		root.addView(bar, 0); // insert at top
		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
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
		setPreferenceSummaries();
	}

	private void setPreferenceSummaries() {
		PreferenceManager preferenceManager = getPreferenceManager();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferenceManager.findPreference("gcm_sender_id").setSummary(sharedPreferences.getString("gcm_sender_id", ""));
		preferenceManager.findPreference("gcm_server_url").setSummary(sharedPreferences.getString("gcm_server_url", ""));
		preferenceManager.findPreference("pubnub_push_subscribe_key").setSummary(sharedPreferences.getString("pubnub_push_subscribe_key", ""));
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Intent intent;
		String gcm_sender_id = sharedPreferences.getString("gcm_sender_id", "");
		String gcm_server_url = sharedPreferences.getString("gcm_server_url", "");
		switch (key){
			case "pubnub_push_enabled":
				// show hint for pubsub configuration
				if (mPrefs.isPushEnabled() && sharedPreferences.getBoolean("show_push_info",true)) {
					showConfigInfo();
					sharedPreferences.edit().putBoolean("show_push_info",false).apply();
				}
			case "pubnub_push_subscribe_key":
//				if(sharedPreferences.getString("pubnub_push_subscribe_key","").length() == 0){
//					showSettingsIncompleteInfo();
//				}
			case "push_ringtone":
			case "push_old_icons":
				activityResult |= PREFERENCES_CHANGED_PUSH;
				getPreferenceManager().findPreference("pubnub_push_subscribe_key").setSummary(sharedPreferences.getString("pubnub_push_subscribe_key",""));
				if (!mPrefs.isPushEnabled()
						|| mPrefs.getPushSubscribeKey().length() > 0) {
					PubnubPushService.startOrStopPushService(getApplicationContext());
				}
				break;

			case "widget_refresh_interval_mins":
				activityResult |= PREFERENCES_CHANGED_WIDGET;
				intent = new Intent(getApplicationContext(),
						WidgetUpdateBroadcastReceiver.class);
				intent.putExtra(WidgetUpdateBroadcastReceiver.REFRESH_RATE_CHANGED,
						true);
				this.sendBroadcast(intent);
				break;

			case "dark_theme":
				activityResult |= PREFERENCES_CHANGED_THEME;
				// we start a new preference activity with changed theme
				intent = getIntent();
				// we have to pass through the result code
				intent.putExtra(ARG_ACTIVITY_RESULT, activityResult);
				// finish();
				startActivityForResult(intent, REQUEST_CODE_PREFERENCES_THEMED);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				break;

			case "gcm_push_enabled":
				activityResult |= PREFERENCES_CHANGED_PUSH;
				if(sharedPreferences.getBoolean("gcm_push_enabled",false)){
					sharedPreferences.edit().putBoolean("show_push_info",false).apply();
					Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
							GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
							, this, 0);
					boolean sentTokenToServer =
							sharedPreferences.getBoolean("sent_token_to_server", false);

					if(dialog == null){ // play services are available
						if(gcm_sender_id.length() > 0 && gcm_server_url.length() > 0){
							if(!sentTokenToServer){ // if not already registered
								intent = new Intent(this,RegistrationIntentService.class);
								intent.setAction("register");
								startService(intent);
							} else {
								Log.d(TAG, "already sent Token to server");
							}
						} else { // at least on of sender_id and server_url are not configured
							// handled in preference onchangelistener
						}
					} else { // show PlayServices unavailable dialog
						dialog.show();
					}
					if(sharedPreferences.getBoolean("show_push_info",true)){
						showConfigInfo();
					}
				} else {
					intent = new Intent(this,RegistrationIntentService.class);
					intent.setAction("unregister");
					startService(intent);
				}
				break;

			case "gcm_sender_id":
				getPreferenceManager().findPreference("gcm_sender_id").setSummary(gcm_sender_id);
				getPreferenceManager().findPreference("gcm_server_url").setSummary(gcm_server_url);
				activityResult |= PREFERENCES_CHANGED_PUSH;
				intent = new Intent(this,RegistrationIntentService.class);
				intent.setAction("unregister");
				startService(intent);
				if(gcm_sender_id.length() > 0 && gcm_server_url.length() > 0){
					intent = new Intent(this,RegistrationIntentService.class);
					intent.setAction("register");
					startService(intent);
				} else { // configuration incomplete
//					showSettingsIncompleteInfo();
				}
			break;
		}
	}

	private void showSettingsIncompleteInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				R.string.settings_incomplete)
				.setCancelable(false)
				.setNegativeButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
												int id) {
								setPreferenceScreen(null);
								addPreferencesFromResource(R.xml.preferences);
								setPreferenceSummaries();
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showConfigInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				R.string.push_dialog_info)
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
