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

package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Item;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class ChecksItemsFragment extends BaseDetailsPage {

	public static final String TAG = ChecksItemsFragment.class.getSimpleName();

	private Item mItem;

	private boolean mLoadingSpinnerVisible = false;

	private MenuItem mMenuItemShare;

	private ShareActionProvider mShareActionProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void setHasOptionsMenu(boolean hasMenu) {
		super.setHasOptionsMenu(hasMenu);
		if (!hasMenu)
			return;
		updateMenu();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_items_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mLoadingSpinnerVisible)
			showLoadingSpinner();
	}

	@Override
	public void onResume() {
		super.onResume();
		fillDetailsText();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		if (mItem != null)
			mZabbixDataService.loadHistoryDetailsByItem(mItem, true, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.severity_details_fragment, menu);

		mMenuItemShare = menu.findItem(R.id.menuitem_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItemShare);
		updateShareIntent();
	}

	protected void setShareIntent(String text) {
		if (mShareActionProvider == null)
			return;
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, text);
		shareIntent.setType("text/plain");
		mShareActionProvider.setShareIntent(shareIntent);
	}

	public void updateShareIntent() {
		if (mItem != null)
			setShareIntent(mItem.getSharableString(this.getActivity()));
	}

	/**
	 * Sets the item for this page. This also triggers an import of history
	 * details for displaying the graph.
	 * 
	 * @param item
	 */
	public void setItem(Item item) {
		this.mItem = item;
		if (item == null) {
			if (getView() != null) {
				((TextView) getView().findViewById(R.id.item_details_name))
						.setText("");

				((TextView) getView().findViewById(R.id.latest_data))
						.setText("");

				ViewGroup layout = (LinearLayout) getView().findViewById(
						R.id.graphs);
				layout.removeAllViews();
			}
		}
		if (mZabbixDataService != null && item != null) {
			fillDetailsText();
			showGraphProgressBar();
			mZabbixDataService.loadHistoryDetailsByItem(mItem, true, this);
		}
	}

	protected void updateMenu() {
		if (mMenuItemShare == null)
			return;
		if (mItem != null)
			mMenuItemShare.setVisible(true);
		else
			mMenuItemShare.setVisible(false);
		updateShareIntent();
	}

	/**
	 * Shows a loading spinner instead of the item details.
	 */
	public void showLoadingSpinner() {
		mLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissLoadingSpinner() {
		mLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	@Override
	protected void showGraph() {
		showGraph(mItem);
	}

	@Override
	protected void fillDetailsText() {
		if (mItem != null && getView() != null) {
			((TextView) getView().findViewById(R.id.item_details_name))
					.setText(mItem.getDescription());

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mItem.getLastClock());
			java.text.DateFormat dateFormatter = SimpleDateFormat
					.getDateTimeInstance(SimpleDateFormat.SHORT,
							SimpleDateFormat.SHORT, Locale.getDefault());
			((TextView) getView().findViewById(R.id.latest_data)).setText(mItem
					.getLastValue()
					+ " "
					+ mItem.getUnits()
					+ " "
					+ getResources().getString(R.string.at)
					+ " "
					+ dateFormatter.format(cal.getTime()));
		}
	}

}
