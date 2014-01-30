package com.inovex.zabbixmobile.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.OnLoginProgressListener;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.PushService;

/**
 * Base class for all activities. Tasks performed in this class:
 * 
 * * Show the navigation drawer
 * 
 * * initiate the connection to the data service and perform the Zabbix login
 * 
 * * open the settings dialog
 * 
 * 
 */
public abstract class BaseActivity extends SherlockFragmentActivity implements
		ServiceConnection, OnLoginProgressListener {

	private static final int REQUEST_CODE_PREFERENCES = 12345;
	public static final int RESULT_PREFERENCES_CHANGED = 1;

	protected ZabbixDataService mZabbixDataService;

	protected String mTitle;

	private DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected ActionBar mActionBar;
	private LoginProgressDialogFragment mLoginProgress;

	private boolean mPreferencesClosed = false;
	private boolean mPreferencesChanged = false;
	private boolean mOnSaveInstanceStateCalled = false;

	private static final String TAG = BaseActivity.class.getSimpleName();
	protected static final int ACTIVITY_PROBLEMS = 0;
	protected static final int ACTIVITY_EVENTS = 1;
	protected static final int ACTIVITY_CHECKS = 2;
	protected static final int ACTIVITY_SCREENS = 3;

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

		ZaxPreferences preferences = ZaxPreferences.getInstance(this);
		if (preferences.isDefault()) {
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
									Intent intent = new Intent(
											getApplicationContext(),
											ZaxPreferenceActivity.class);
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
		if (!mZabbixDataService.isLoggedIn())
			mZabbixDataService.performZabbixLogin(this);

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "onServiceDisconnected()");
		mZabbixDataService = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		PushService.startOrStopPushService(getApplicationContext());
		// if the preferences activity has been closed (and possibly preferences
		// have been changed), we start a relogin.
		if (mPreferencesClosed && mZabbixDataService != null) {
			if (mPreferencesChanged == true) {
				mZabbixDataService.setLoggedIn(false);
				mZabbixDataService.clearAllData();
				mPreferencesChanged = false;
			}
			mZabbixDataService.performZabbixLogin(this);
			mPreferencesClosed = false;
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
		super.onCreate(savedInstanceState);

		bindService();

		mActionBar = getSupportActionBar();

		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(true);
		}

		// (re-) instantiate progress dialog
		mLoginProgress = (LoginProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(LoginProgressDialogFragment.TAG);
		if (mLoginProgress == null)
			mLoginProgress = LoginProgressDialogFragment.getInstance();
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
		mDrawerList = (ListView) mDrawerLayout.findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// // set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.list_item_main_menu, getResources().getStringArray(
						R.array.activities)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /*
							 * "open drawer" description for accessibility
							 */
		R.string.drawer_close /*
							 * "close drawer" description for accessibility
							 */
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
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		setContentView(mDrawerLayout);
	}

	/**
	 * The click listener for ListView in the navigation drawer
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		Intent intent = null;
		switch (position) {
		case ACTIVITY_PROBLEMS:
			intent = new Intent(this, ProblemsActivity.class);
			break;
		case ACTIVITY_EVENTS:
			intent = new Intent(this, EventsActivity.class);
			break;
		case ACTIVITY_CHECKS:
			intent = new Intent(this, ChecksActivity.class);
			break;
		case ACTIVITY_SCREENS:
			intent = new Intent(this, ScreensActivity.class);
			break;
		default:
			return;
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		// setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
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

		if (isFinishing()) {
			Log.d(TAG, "unbindService");
			getApplicationContext().unbindService(this);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
			return true;
		case R.id.menuitem_preferences:
			Intent intent = new Intent(getApplicationContext(),
					ZaxPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_CODE_PREFERENCES);
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
	protected void refreshData() {
		mZabbixDataService.clearAllData();
		// re-login and load host groups
		mZabbixDataService.performZabbixLogin(this);
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
		if (!this.isFinishing() && !mOnSaveInstanceStateCalled)
			mLoginProgress.show(getSupportFragmentManager(),
					LoginProgressDialogFragment.TAG);
	}

	@Override
	public void onLoginFinished(boolean success) {
		if (mLoginProgress != null && mLoginProgress.isAdded()) {
			try {
				mLoginProgress.dismiss();
			} catch (NullPointerException e) {
				// in case the progress is not being shown right now, a
				// NullPointerException will be thrown, which we can safely
				// ignore.
			}
		}
		if (success) {
			Toast.makeText(this, R.string.zabbix_login_successful,
					Toast.LENGTH_LONG).show();
			loadData();
		}
	}

	/**
	 * If the finished activity is the {@link PreferenceActivity}, save the
	 * status (preferences changed or not) to be used in onResume().
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");

		if (requestCode == REQUEST_CODE_PREFERENCES) {
			mPreferencesClosed = true;
			Log.d(TAG, "onActivityResult: " + requestCode + " - " + resultCode);
			if (resultCode == RESULT_PREFERENCES_CHANGED)
				mPreferencesChanged = true;
			else
				mPreferencesChanged = false;
		}
	}

	protected void onNavigationDrawerClosed() {
		getSupportActionBar().setTitle(mTitle);
		supportInvalidateOptionsMenu();
	}

	protected void onNavigationDrawerOpened() {
		getSupportActionBar().setTitle(R.string.app_name);
		supportInvalidateOptionsMenu();
	}

	/**
	 * The login progress dialog.
	 * 
	 */
	public static class LoginProgressDialogFragment extends DialogFragment {

		public static final String TAG = LoginProgressDialogFragment.class
				.getSimpleName();

		public static LoginProgressDialogFragment getInstance() {
			return new LoginProgressDialogFragment();
		}

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
}
