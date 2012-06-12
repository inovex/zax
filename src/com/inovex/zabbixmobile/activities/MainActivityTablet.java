package com.inovex.zabbixmobile.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.inovex.zabbixmobile.AppPreferenceActivity;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.support.ClearLocalDatabaseSupport;
import com.inovex.zabbixmobile.activities.support.CurrentViewSupport;
import com.inovex.zabbixmobile.activities.support.MainActivitySupport;
import com.inovex.zabbixmobile.activities.support.SetLoadingProgressSupport;

public class MainActivityTablet extends Activity implements CurrentViewSupport, ClearLocalDatabaseSupport, SetLoadingProgressSupport {
	public enum AppView {
		PROBLEMS, EVENTS, CHECKS, SCREENS
	}

	private AppView currentView;
	private MainActivitySupport support;
	private ListView listviewMenu;

	@Override
	public void clearLocalDatabase() {
		getContentFragment().onMenuItemClearSelected();
	}

	public ContentFragment getContentFragment() {
		return (ContentFragment) getFragmentManager().findFragmentById(R.id.fragment_content);
	}

	@Override
	public AppView getCurrentView() {
		return currentView;
	}

	public HostListFragment getHostListFragment() {
		return (HostListFragment) getFragmentManager().findFragmentById(R.id.fragment_host_list);
	}

	@Override
	public void onBackPressed() {
		if (!getContentFragment().onBackPressed()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tablet);

		listviewMenu = (ListView) findViewById(R.id.listview_menu);
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> itm = new HashMap<String, Object>();
		itm.put("text", "Problems");
		data.add(itm);
		itm = new HashMap<String, Object>();
		itm.put("text", "Events");
		data.add(itm);
		itm = new HashMap<String, Object>();
		itm.put("text", "Checks");
		data.add(itm);
		itm = new HashMap<String, Object>();
		itm.put("text", "Screens");
		data.add(itm);
		listviewMenu.setAdapter(new SimpleAdapter(this, data, android.R.layout.simple_list_item_single_choice, new String[] {"text"}, new int[] {android.R.id.text1}));
		listviewMenu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
				if (arg3 == 0) {
					showView(AppView.PROBLEMS);
				} else if (arg3 == 1) {
					showView(AppView.EVENTS);
				} else if (arg3 == 2) {
					showView(AppView.CHECKS);
				} else if (arg3 == 3) {
					showView(AppView.SCREENS);
				}
			}
		});

		support = new MainActivitySupport(this, getContentFragment(), this);

		// first selection
		listviewMenu.setItemChecked(0, true);
		showView(AppView.PROBLEMS);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == MainActivitySupport.DIALOG_FAILURE_MESSAGE) {
			return support.getDlgFailureMessage();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu_tablet, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuitem_preferences) {
			Intent intent = new Intent(getApplicationContext(), AppPreferenceActivity.class);
			startActivityForResult(intent, 0);
			return true;
		} else if (item.getItemId() == R.id.menuitem_acknowledge) {
			return getContentFragment().onMenuItemAcknowledgeSelected();
		} else if (item.getItemId() == R.id.menuitem_clear) {
			return getContentFragment().onMenuItemClearSelected();
		} else if (item.getItemId() == R.id.menuitem_buy_bonus) {
			support.menuItemBuyBonusSelected();
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		support.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		support.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.getString("contentLastView") != null) {
				getContentFragment().restoreLastView(savedInstanceState.getString("contentLastView"));
			}
			if (getCurrentView() == AppView.SCREENS) {
				int screenPos = savedInstanceState.getInt("screenList_screenPos", -1);
				if (screenPos != -1) {
					getHostListFragment().restoreLastSelectionScreens(screenPos);
				}
			} else {
				int hostGroupPos = savedInstanceState.getInt("hostList_hostGroupPos", -1);
				long hostGroupId = savedInstanceState.getLong("hostList_hostGroupId", -1);
				int hostPos = savedInstanceState.getInt("hostList_hostPos", -1);
				if (hostGroupPos > -1 && hostGroupId > -1) {
					getHostListFragment().restoreLastSelection(hostGroupPos, hostGroupId, hostPos);
				}
			}
		}
		// workaround - MenuItem was not initialized here
		// and if the menuitem gets disabled here, it can not be enabled later because onSelect will not work.
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (findViewById(R.id.menuitem_acknowledge) != null) {
					findViewById(R.id.menuitem_acknowledge).setEnabled(false);
				}
			}
		}, 1000);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean b = super.onPrepareOptionsMenu(menu);
		support.onPrepareOptionsMenu(menu);
		return b;
	}

	@Override
	protected void onResume() {
		super.onResume();
		support.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		support.onSaveInstanceState(outState);
		outState.putString("contentLastView", getContentFragment().getLastView());
		outState.putInt("hostList_hostGroupPos", getHostListFragment().getListHostGroupCheckedItemPosition());
		outState.putLong("hostList_hostGroupId", getHostListFragment().getListHostGroupCheckedItemId());
		outState.putInt("hostList_hostPos", getHostListFragment().getListHostCheckedItemPosition());
		outState.putInt("screenList_screenPos", getHostListFragment().getListScreensCheckedItemPosition());
	}

	@Override
	protected void onStop() {
		super.onStop();
		support.onStop();
	}

	public void reloadCurrentView() {
		showView(currentView);
	}

	@Override
	public void setCurrentView(AppView view) {
		showView(view);
	}

	@Override
	public void setLoadingProgress(int progress) {
		getHostListFragment().setLoadingProgress(progress);
		getContentFragment().setLoadingProgress(progress);
	}

	private void showView(AppView view) {
		currentView = view;
		getContentFragment().showView(view);
		getHostListFragment().showView(view);

		int pos;
		if (view == AppView.PROBLEMS) pos = 0;
		else if (view == AppView.EVENTS) pos = 1;
		else if (view == AppView.CHECKS) pos = 2;
		else pos = 3; // SCREENS
		listviewMenu.setItemChecked(pos, true);
	}
}
