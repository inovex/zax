package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.inovex.zabbixmobile.R;

public class ZaxPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
