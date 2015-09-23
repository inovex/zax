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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Created by felix on 22/09/15.
 */
public class BaseDetailsActivity extends BaseHostGroupSpinnerActivity implements OnListItemSelectedListener {

	private BaseSeverityFilterDetailsFragment mDetailsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			finish();
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details_fragment);
		Bundle extras = getIntent().getExtras();
		TriggerSeverity severity = TriggerSeverity.getSeverityByPosition(extras.getInt("severity"));
		int position = extras.getInt("position");

		mDetailsFragment.setSeverity(severity);
		mDetailsFragment.selectItem(position);

		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mDrawerToggle.setDrawerIndicatorEnabled(false);
	}

	@Override
	public void onListItemSelected(int position, long id) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
