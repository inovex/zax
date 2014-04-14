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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.NavigationDrawerFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.OnLoginProgressListener;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.PushService;
import com.inovex.zabbixmobile.widget.WidgetUpdateBroadcastReceiver;

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
		ServiceConnection, OnLoginProgressListener,
		ListView.OnItemClickListener {

	private static final int REQUEST_CODE_PREFERENCES = 12345;
	public static final int RESULT_PREFERENCES_CHANGED = 1;

	protected ZabbixDataService mZabbixDataService;

	protected String mTitle;

	private DrawerLayout mDrawerLayout;
	private FrameLayout mDrawerFrame;
	protected NavigationDrawerFragment mDrawerFragment;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected ActionBar mActionBar;
	private LoginProgressDialogFragment mLoginProgress;

	private boolean mPreferencesClosed = false;
	private boolean mPreferencesChangedServer = false;
	private boolean mPreferencesChangedPush = false;
	private boolean mPreferencesChangedWidget = false;
	private boolean mPreferencesChangedTheme = false;

	private boolean mOnSaveInstanceStateCalled = false;

	private static final String TAG = BaseActivity.class.getSimpleName();

	private FinishReceiver finishReceiver;
	private boolean mDrawerOpened = false;
	public static final int ACTIVITY_SCREENS = 3;
	public static final int ACTIVITY_CHECKS = 2;
	public static final int ACTIVITY_EVENTS = 1;
	public static final int ACTIVITY_PROBLEMS = 0;
	private static final String ACTION_FINISH = "com.inovex.zabbixmobile.BaseActivity.ACTION_FINISH";

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
		// if the preferences activity has been closed, we check which
		// preferences have been changed and perform necessary actions
		if (mPreferencesClosed) {
			if (mPreferencesChangedServer == true && mZabbixDataService != null) {
				mZabbixDataService.performZabbixLogout();
				mZabbixDataService.clearAllData();
				mZabbixDataService.initConnection();
				// update widget because server data has changed
				Intent intent = new Intent(getApplicationContext(),
						WidgetUpdateBroadcastReceiver.class);
				this.sendBroadcast(intent);
				mZabbixDataService.performZabbixLogin(this);
				mPreferencesChangedServer = false;
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
			PushService.startOrStopPushService(getApplicationContext());
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
		mDrawerFrame = (FrameLayout) mDrawerLayout
				.findViewById(R.id.left_drawer);
		mDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		// this is a hack to set the text color of the overflow menu (apparently
		// the only sane solution, according to
		// http://www.techrepublic.com/article/style-androids-overflow-menu-using-this-sanity-saving-workaround/)
		for (int i = 0; i < menu.size(); i++) {
			MenuItem mi = menu.getItem(i);
			if (mDrawerOpened) {
				mi.setVisible(false);
				continue;
			}
			String title = mi.getTitle().toString();
			Spannable newTitle = new SpannableString(title);
			TypedArray a = obtainStyledAttributes(R.attr.actionMenuColor,
					R.styleable.CustomTheme);

			int color = a.getColor(R.styleable.CustomTheme_actionMenuColor,
					getResources().getColor(R.color.white));
			a.recycle();
			newTitle.setSpan(new ForegroundColorSpan(color), 0,
					newTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mi.setTitle(newTitle);
		}
		return true;
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
			if (mDrawerLayout.isDrawerOpen(mDrawerFrame)) {
				mDrawerLayout.closeDrawer(mDrawerFrame);
			} else {
				mDrawerLayout.openDrawer(mDrawerFrame);
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
	public void refreshData() {
		mZabbixDataService.clearAllData();
		mZabbixDataService.performZabbixLogout();
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
			if (showToast)
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
		getSupportActionBar().setTitle(mTitle);
		mDrawerOpened = false;
		supportInvalidateOptionsMenu();
	}

	protected void onNavigationDrawerOpened() {
		getSupportActionBar().setTitle(R.string.app_name);
		mDrawerOpened = true;
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

	protected void selectDrawerItem(int index) {
		mDrawerFragment.selectMenuItem(index);
		mDrawerLayout.closeDrawer(mDrawerFrame);
		mDrawerToggle.syncState();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = null;
		switch (position) {
		case BaseActivity.ACTIVITY_PROBLEMS:
			intent = new Intent(this, ProblemsActivity.class);
			break;
		case BaseActivity.ACTIVITY_EVENTS:
			intent = new Intent(this, EventsActivity.class);
			break;
		case BaseActivity.ACTIVITY_CHECKS:
			intent = new Intent(this, ChecksActivity.class);
			break;
		case BaseActivity.ACTIVITY_SCREENS:
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
		mDrawerFragment.selectMenuItem(position);
		mDrawerLayout.closeDrawer(mDrawerFrame);
	}
}
