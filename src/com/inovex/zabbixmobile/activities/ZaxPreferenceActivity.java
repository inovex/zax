package com.inovex.zabbixmobile.activities;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.R.xml;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ZaxPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	
	
}
