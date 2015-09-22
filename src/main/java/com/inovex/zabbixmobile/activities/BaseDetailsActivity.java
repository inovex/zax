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

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

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
	protected void onPause() {
		super.onPause();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	@Override
	public void onListItemSelected(int position, long id) {

	}
}
