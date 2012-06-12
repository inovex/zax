package com.inovex.zabbixmobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * activity for preferences
 */
public class AppPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference pref = findPreference("zabbix_push_enabled");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// alert
				if (newValue != null && (Boolean) newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
					builder.setMessage("To use Push Notifications, you have to configure your Zabbix server. Please read the Howto at http://apps.inovex.de/zax/#howto_push")
						.setCancelable(false)
						.setPositiveButton("View Howto in Browser", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://apps.inovex.de/zax/#howto_push"));
								startActivity(viewIntent);
							}
						})
						.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}

				return true;
			}
		});

		pref = findPreference("widget_refresh_interval_mins");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// alert
				AlertDialog.Builder builder = new AlertDialog.Builder(AppPreferenceActivity.this);
				builder.setMessage("If you are already using the widget, you have to remove it and add it again, so that the new refresh interval will be used.")
					.setCancelable(true)
					.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
				});
				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});
	}
}
