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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inovex.zabbixmobile.OnSettingsMigratedReceiver;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.OnLoginProgressListener;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.pubnub.PubnubPushService;
import com.inovex.zabbixmobile.widget.WidgetUpdateBroadcastReceiver;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;

/**
 * Base class for all activities. Tasks performed in this class:
 * <p/>
 * * Show the navigation drawer
 * <p/>
 * * initiate the connection to the data service and perform the Zabbix login
 * <p/>
 * * open the settings dialog
 */
public abstract class BaseActivity extends AppCompatActivity implements
		ServiceConnection, OnLoginProgressListener,
		NavigationView.OnNavigationItemSelectedListener,
		AdapterView.OnItemSelectedListener, View.OnClickListener {

	private static final int REQUEST_CODE_PREFERENCES = 12345;
	public static final int RESULT_PREFERENCES_CHANGED = 1;

	protected ZabbixDataService mZabbixDataService;

	protected String mTitle;

	protected DrawerLayout mDrawerLayout;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected Toolbar mToolbar;
	protected Toolbar mHostgroupToolbar;
	protected NavigationView mNavigationView;

	private LoginProgressDialogFragment mLoginProgress;
	private boolean mPreferencesClosed = false;
	private boolean mPreferencesChangedServer = false;
	private boolean mPreferencesChangedPush = false;
	private boolean mPreferencesChangedWidget = false;

	private boolean mServerSelectMode = false;

	private boolean mPreferencesChangedTheme = false;

	private boolean mOnSaveInstanceStateCalled = false;
	private static final String TAG = BaseActivity.class.getSimpleName();
	private FinishReceiver finishReceiver;
	private boolean mDrawerOpened = false;
	public static final int ACTIVITY_SCREENS = 3;
	public static final int ACTIVITY_CHECKS = 2;
	public static final int ACTIVITY_EVENTS = 1;
	public static final int ACTIVITY_PROBLEMS = 0;
	public static final int MESSAGE_SSL_ERROR = 0;
	private static final String ACTION_FINISH = "com.inovex.zabbixmobile.BaseActivity.ACTION_FINISH";
	private OnSettingsMigratedReceiver mOnSettingsMigratedReceiver;
	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;
	private TextView mServerNameView;
	private ImageButton mServerSelectButton;
	private View mServerNameLayout;
	private long persistedServerSelection;


	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		Intent intent;
		if (menuItem.isChecked()) menuItem.setChecked(false);
		else menuItem.setChecked(true);

		if(mServerSelectMode){
			selectServerItem(menuItem.getItemId());
			toggleServerSelectionMode();
			return true;
		}

		//Closing drawer on item click
		switch (menuItem.getItemId()) {
			case R.id.navigation_item_problems:
				intent = new Intent(this, ProblemsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.navigation_item_events:
				intent = new Intent(this, EventsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.navigation_item_checks:
				intent = new Intent(this, ChecksActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.navigation_item_screens:
				intent = new Intent(this, ScreensActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				mDrawerLayout.closeDrawers();
				break;
			case R.id.navigation_settings:
				intent = new Intent(this, ZaxPreferenceActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivityForResult(intent, REQUEST_CODE_PREFERENCES);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				return true;
			case R.id.navigation_manage_servers:
				intent = new Intent(this, ServersActivity.class);
				break;
			case R.id.navigation_info:
				intent = new Intent(this, InfoActivity.class);
				break;
			default:
				return true;
		}
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);

		// update selected item and title, then close the drawer
		//mDrawerFragment.selectMenuItem(position);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		selectServerItem(id);
		// persist selection
//		ZaxPreferences.getInstance(getApplicationContext())
//				.setServerSelection(id);
		Log.d(TAG, "selected server id=" + id);

		this.refreshData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.navigation_button_serverselect:
			case R.id.server_name_layout:
				toggleServerSelectionMode();
				break;
		}
	}

	private void toggleServerSelectionMode() {
		Menu menu = mNavigationView.getMenu();
		if(mServerSelectMode){
			// show normal menu
			menu.setGroupVisible(R.id.grp1, true);
			menu.setGroupVisible(R.id.grp2, true);
			menu.removeGroup(R.id.grp0_server);
			mServerSelectButton.setImageDrawable(
					getResources().getDrawable(R.drawable.spinner_triangle));

		} else {
			// show server selection list
			menu.setGroupVisible(R.id.grp1, false);
			menu.setGroupVisible(R.id.grp2, false);
			for(int i = 0; i < mServersListAdapter.getCount(); i++){
				ZabbixServer server = mServersListAdapter.getItem(i);
				menu.add(R.id.grp0_server, (int) server.getId(), Menu.NONE, server.getName())
						.setIcon(R.drawable.ic_monitor_grey600_24dp);
			}
			mServerSelectButton.setImageDrawable(
					getResources().getDrawable(R.drawable.spinner_triangle_flipped));
		}
		mServerSelectMode = !mServerSelectMode;
	}

	/**
	 * Broadcast receiver that finishes an activity.
	 */
	private final class FinishReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_FINISH))
				finish();
		}
	}

	/**
	 * If the server settings have not been set yet by the user, a dialog is
	 * displayed. Otherwise, the Zabbix login is performed.
	 */
	@SuppressLint("ValidFragment")
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		Log.d(TAG, "onServiceConnected: " + this.getLocalClassName() + " "
				+ this.toString());
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
		mZabbixDataService.setActivityContext(BaseActivity.this);

		mZabbixDataService.loadZabbixServers(); // sync

		// if there are no servers configurated
		if (mZabbixDataService.getServersListManagementAdapter().isEmpty()) {
			new DialogFragment() {

				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					LayoutInflater inflater = getActivity().getLayoutInflater();

					View view = inflater.inflate(
							R.layout.dialog_default_settings, null);
					builder.setView(view);

					builder.setTitle(R.string.server_data_not_set);

					// Add action buttons
					builder.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int id) {
									// create default zabbix server
									ZabbixServer srv = mZabbixDataService.createNewZabbixServer("default");
									ZaxPreferences.getInstance(getApplicationContext()).setServerSelection(srv.getId());

									Intent intent = new Intent(
											getApplicationContext(),
											ZabbixServerPreferenceActivity.class);
									intent.putExtra(ZabbixServerPreferenceActivity.ARG_ZABBIX_SERVER_ID, srv.getId());
									getActivity().startActivityForResult(
											intent, REQUEST_CODE_PREFERENCES);
								}
							});
					return builder.create();
				}

			}.show(getSupportFragmentManager(), "DefaultSettingsDialogFragment");
			// return to avoid a login with incorrect credentials
			return;
		}
		mZabbixDataService.performZabbixLogin(this);
		mServersListAdapter = mZabbixDataService.getServersSelectionAdapter();
		ZabbixServer server = mServersListAdapter.getItem(mServersListAdapter.getCurrentPosition());
		setServerViews(server.getName());
		mServerSelectButton.setOnClickListener(this);
		mServerNameLayout.setOnClickListener(this);
		updateCurrentServerNameView();
//		restoreServerSelection();
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "onServiceDisconnected()");
		mZabbixDataService = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		ZaxPreferences.getInstance(this).setServerSelection(this.persistedServerSelection);
		Log.d(TAG, "onPause");
	}

	/**
	 * If settings have been changed, this method clears all cached data and
	 * initiates a fresh login.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		ZaxPreferences preferences = ZaxPreferences.getInstance(this);
		this.persistedServerSelection = preferences.getServerSelection();
		this.setServerViews(preferences.getPersistedServerName());
		// if the preferences activity has been closed, we check which
		// preferences have been changed and perform necessary actions
		if (mPreferencesClosed) {
			if (mPreferencesChangedServer && mZabbixDataService != null) {
				mZabbixDataService.performZabbixLogout();
				mZabbixDataService.clearAllData();
				mZabbixDataService.initConnection();
				// update widget because server data has changed
				mServersListAdapter = mZabbixDataService.getServersSelectionAdapter();
				Intent intent = new Intent(getApplicationContext(),
						WidgetUpdateBroadcastReceiver.class);
				this.sendBroadcast(intent);
				mZabbixDataService.performZabbixLogin(this);
				mPreferencesChangedServer = false;
				restoreServerSelection();
			}
			if (mPreferencesChangedTheme) {
				mPreferencesChangedTheme = false;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					// clear activity history and restart this activity
					Intent restartIntent = getIntent();
					restartIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					startActivity(restartIntent);
					overridePendingTransition(android.R.anim.fade_in,
							android.R.anim.fade_out);
				} else {
					// as FLAG_ACTIVITY_CLEAR_TASK is not available on Android <
					// version 11, we manually send a broadcast to clear the
					// activity history
					sendBroadcast(new Intent(ACTION_FINISH));
					Intent restartIntent = getIntent();
					restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(restartIntent);
				}
			}
			mPreferencesClosed = false;
		} else {
			PubnubPushService.startOrStopPushService(getApplicationContext());
		}
		updateCurrentServerNameView();
	}

	private void updateCurrentServerNameView() {
		if(mZabbixDataService != null){
			ZabbixServer server = mZabbixDataService.getServerById(persistedServerSelection);
			if(server != null){
				setServerViews(server.getName());
			} else {
				Log.d(TAG,"Error: Persisted ServerID was not found!");
			}
		}
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		boolean useMockData = getIntent().getBooleanExtra(
				ZabbixDataService.EXTRA_IS_TESTING, false);
		intent.putExtra(ZabbixDataService.EXTRA_IS_TESTING, useMockData);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * Binds the data service and sets up the action bar.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ZaxPreferences prefs = ZaxPreferences
				.getInstance(getApplicationContext());
		if (prefs.isDarkTheme())
			setTheme(R.style.AppThemeDark);
		else
			setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);

		finishReceiver = new FinishReceiver();
		registerReceiver(finishReceiver, new IntentFilter(ACTION_FINISH));

		bindService();

		// (re-) instantiate progress dialog
		mLoginProgress = (LoginProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(LoginProgressDialogFragment.TAG);

		if (mLoginProgress == null) {
			mLoginProgress = LoginProgressDialogFragment.getInstance();
		}

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			MemorizingTrustManager mtm = new MemorizingTrustManager(this);
			sc.init(null, new X509TrustManager[] {mtm}, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(
					mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inflates a base layout containing the navigation drawer and incorporates
	 * the given layout resource into this base layout.
	 */
	@Override
	public void setContentView(final int layoutResID) {
		mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(
				R.layout.activity_base, null);
		FrameLayout content = (FrameLayout) mDrawerLayout
				.findViewById(R.id.content_frame);
		getLayoutInflater().inflate(layoutResID, content, true);

		mServerNameView = (TextView) mDrawerLayout.findViewById(R.id.navigation_text_servername);
		mServerSelectButton = (ImageButton) mDrawerLayout.findViewById(R.id.navigation_button_serverselect);
		mServerNameLayout = mDrawerLayout.findViewById(R.id.server_name_layout);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		mNavigationView = (NavigationView) mDrawerLayout.findViewById(R.id.navigation);

		//Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
		mNavigationView.setNavigationItemSelectedListener(this);



		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this, /* host Activity */
				mDrawerLayout, /* DrawerLayout object */
				mToolbar, /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open, /* "open drawer" description for accessibility */
				R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View view) {
				onNavigationDrawerClosed();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				onNavigationDrawerOpened();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		setContentView(mDrawerLayout);
		mDrawerToggle.syncState();

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mHostgroupToolbar = (Toolbar) findViewById(R.id.toolbar_hostgroup);
//        mToolbar.setTitle(mTitle);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * This unbinds the service if the application is exiting.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		unregisterReceiver(finishReceiver);
// TODO commented out because registration above is commented out
//        unregisterReceiver(mOnSettingsMigratedReceiver);

		if (isFinishing()) {
			Log.d(TAG, "unbindService");
			getApplicationContext().unbindService(this);
		}
	}

	@Override
	public void onBackPressed() {
//        finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.menuitem_clear:
				refreshData();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Clears all cached data and performs a fresh login.
	 */
	public void refreshData(){
		refreshData(true);
	}

	public void refreshData(boolean logout) {
		mZabbixDataService.clearAllData(logout);
		if(logout){
			mZabbixDataService.performZabbixLogout();
			// re-login and load host groups
			mZabbixDataService.performZabbixLogin(this);
		}
	}

	/**
	 * Loads all data necessary for a view from Zabbix.
	 */
	protected abstract void loadData();

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mOnSaveInstanceStateCalled = true;
	}

	@Override
	public void onLoginStarted() {
		if (!this.isFinishing() && !mOnSaveInstanceStateCalled && !mLoginProgress.isAdded()) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.show(mLoginProgress);
			ft.commitAllowingStateLoss();
//			mLoginProgress.show(getSupportFragmentManager(),
//					LoginProgressDialogFragment.TAG);
		}
	}

	@Override
	public void onLoginFinished(boolean success, boolean showToast) {
		if (mLoginProgress != null && mLoginProgress.isAdded()) {
			try {
				mLoginProgress.dismiss();
			} catch (NullPointerException e) {
				// in case the progress is not being shown right now, a
				// NullPointerException will be thrown, which we can safely
				// ignore.
			} catch (IllegalStateException e) {
				// if the activity has been canceled, this exception will be
				// thrown
				e.printStackTrace();
			}
		}
		if (success) {
//			if (showToast)
//				Toast.makeText(this, R.string.zabbix_login_successful,
//						Toast.LENGTH_LONG).show();
			loadData();
		}
	}

	/**
	 * If the finished activity is the {@link PreferenceActivity}, save the
	 * status (preferences changed or not) to be used in onResume().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult: " + requestCode + " - " + resultCode);

		if (requestCode == REQUEST_CODE_PREFERENCES) {
			if ((resultCode & ZaxPreferenceActivity.PREFERENCES_CHANGED_SERVER) == ZaxPreferenceActivity.PREFERENCES_CHANGED_SERVER) {
				mPreferencesChangedServer = true;
				Log.d(TAG, "preferences changed: server");
			}
			if ((resultCode & ZaxPreferenceActivity.PREFERENCES_CHANGED_PUSH) == ZaxPreferenceActivity.PREFERENCES_CHANGED_PUSH) {
				mPreferencesChangedPush = true;
				Log.d(TAG, "preferences changed: push");
			}
			if ((resultCode & ZaxPreferenceActivity.PREFERENCES_CHANGED_WIDGET) == ZaxPreferenceActivity.PREFERENCES_CHANGED_WIDGET) {
				mPreferencesChangedWidget = true;
				Log.d(TAG, "preferences changed: widget");
			}
			if ((resultCode & ZaxPreferenceActivity.PREFERENCES_CHANGED_THEME) == ZaxPreferenceActivity.PREFERENCES_CHANGED_THEME) {
				mPreferencesChangedTheme = true;
				Log.d(TAG, "preferences changed: theme");
			}
			mPreferencesClosed = true;
		}
	}

	protected void onNavigationDrawerClosed() {
		//mToolbar.setTitle(mTitle);
		mDrawerOpened = false;
		supportInvalidateOptionsMenu();
	}

	protected void onNavigationDrawerOpened() {
		//mToolbar.setTitle(R.string.app_name);
		mDrawerOpened = true;
		supportInvalidateOptionsMenu();
	}

	/**
	 * The login progress dialog.
	 */
	public static class LoginProgressDialogFragment extends DialogFragment {

		public static final String TAG = LoginProgressDialogFragment.class
				.getSimpleName();

		public static LoginProgressDialogFragment getInstance() {
			return new LoginProgressDialogFragment();
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			setRetainInstance(true);
			ProgressDialog loginProgress = new ProgressDialog(getActivity());
			loginProgress.setTitle(R.string.zabbix_login);
			loginProgress.setMessage(getResources().getString(
					R.string.zabbix_login_in_progress));
			loginProgress.setIndeterminate(true);
			return loginProgress;
		}

	}

	public void restoreServerSelection() {
		persistedServerSelection = ZaxPreferences.getInstance(getApplicationContext()).getServerSelection();
		selectServerItem(persistedServerSelection);
	}

	protected void selectServerItem(long zabbixServerId) {
		for (int i = 0; i < mServersListAdapter.getCount(); i++) {
			if (mServersListAdapter.getItemId(i) == zabbixServerId
					&& mServersListAdapter.getItemId(i) != persistedServerSelection) {
				mServersListAdapter.setCurrentPosition(i);
				setServerViews(mServersListAdapter.getItem(i).getName());
				this.persistedServerSelection = mServersListAdapter.getItem(i).getId();
				ZaxPreferences preferences = ZaxPreferences.getInstance(this);
				preferences.setServerSelection(persistedServerSelection);
				preferences.setPersistedServerName(mServersListAdapter.getItem(i).getName());
				mZabbixDataService.setZabbixServer(persistedServerSelection);
//				this.mZabbixDataService.clearAllData(false);
				this.refreshData();
				break;
			}
		}
	}

	protected void setServerViews(String name){
		this.mServerNameView.setText(name);
	}

}
