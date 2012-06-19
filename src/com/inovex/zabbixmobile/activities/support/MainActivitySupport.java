package com.inovex.zabbixmobile.activities.support;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.inovex.zabbixmobile.AppPreferenceActivity;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.MainActivitySmartphone;
import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;
import com.inovex.zabbixmobile.billing.BillingService;
import com.inovex.zabbixmobile.billing.Consts;
import com.inovex.zabbixmobile.billing.ResponseHandler;
import com.inovex.zabbixmobile.billing.ZabbixMobilePurchaseObserver;
import com.inovex.zabbixmobile.push.PushAlarm;
import com.inovex.zabbixmobile.push.PushService;

public class MainActivitySupport {
	private final Activity mActivity;
	private final LoadContentSupport loadContentSupport;
	private boolean authError;

	public static final int DIALOG_FAILURE_MESSAGE = 101;

	private Dialog dlgFailureMessage;
	private ProgressDialog loadingDlg;
	private Handler mHandler;
	private final CurrentViewSupport currentViewSupport;

	private BillingService mBillingService;
	private ZabbixMobilePurchaseObserver mZabbixMobilePurchaseObserver;

	/**
	 * receives information from service
	 */
	private final BroadcastReceiver contentProviderReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getIntExtra("flag", 0)) {
			case ZabbixContentProvider.INTENT_FLAG_CONNECTION_FAILED:
				authError = true;
				if (mActivity instanceof MainActivitySmartphone) {
					((MainActivitySmartphone) mActivity).hideLoading(true);
				} else {
					hideLoading();
				}
				showFailureMessage(R.string.internet_connection_failed+" ("+intent.getStringExtra("value")+")", false);
				break;
			case ZabbixContentProvider.INTENT_FLAG_AUTH_FAILED:
				authError = true;
				if (mActivity instanceof MainActivitySmartphone) {
					((MainActivitySmartphone) mActivity).hideLoading(true);
				} else {
					hideLoading();
				}
				if (intent.getBooleanExtra("httpAuthRequired", false)) {
					showFailureMessage(R.string.http_auth_failed, true);
				} else if (intent.getBooleanExtra("noApiAccess", false)) {
					showFailureMessage(R.string.zabbix_no_api_access, false);
				} else if (intent.getBooleanExtra("preconditionFailed", false)) {
					showFailureMessage(R.string.preconditionFailed, true);
				} else {
					showFailureMessage(R.string.zabbix_login_unsuccessful, true);
				}
				break;
			case ZabbixContentProvider.INTENT_FLAG_SHOW_PROGRESS:
				final int progress = intent.getIntExtra("value", 1);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mActivity instanceof SetLoadingProgressSupport) {
							((SetLoadingProgressSupport) mActivity).setLoadingProgress(progress);
						} else if (loadingDlg != null && loadingDlg.isShowing()){
							loadingDlg.setProgress(progress);
						}
					}
				});
				break;
			case ZabbixContentProvider.INTENT_FLAG_SHOW_EXCEPTION:
				if (mActivity instanceof MainActivitySmartphone) {
					((MainActivitySmartphone) mActivity).hideLoading(true);
				} else {
					hideLoading();
				}

				showFailureMessage(intent.getStringExtra("value"), false);
				break;
			case ZabbixContentProvider.INTENT_FLAG_SSL_NOT_TRUSTED:
				if (mActivity instanceof MainActivitySmartphone) {
					((MainActivitySmartphone) mActivity).hideLoading(true);
				} else {
					hideLoading();
				}

				authError = true;
				showFailureMessage(R.string.ssl_certificate_not_trusted, true);
				break;
			case ZabbixContentProvider.INTENT_FLAG_AUTH_SUCCESSFUL:
				authError = false;

				// all okay
				Toast.makeText(mActivity.getBaseContext(), R.string.zabbix_login_successful, Toast.LENGTH_SHORT).show();
				// save data
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("zabbix_last_prefs", prefs.getString("zabbix_url", null)+"#"+prefs.getString("zabbix_username", null)+"#"+prefs.getString("zabbix_password", null));
				editor.commit();

				hideLoading();

				// load "problems" tab
				currentViewSupport.setCurrentView(currentViewSupport.getCurrentView());
				break;
			}
		}
	};
	private Dialog dlgSetPreferences;
	private boolean menuItemBuyBonusEnabled;

	public MainActivitySupport(Activity activity, LoadContentSupport loadContentSupport, CurrentViewSupport currentViewSupport) {
		mActivity = activity;
		this.loadContentSupport = loadContentSupport;
		this.currentViewSupport = currentViewSupport;
	}

	/**
	 * checks the zabbix login preferences
	 * if it fails, a message will be shown
	 * @return true if check was successful
	 */
	private boolean _checkZabbixLogin() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		String url = prefs.getString("zabbix_url", "").trim();
		String user = prefs.getString("zabbix_username", "").trim();
		String pwd = prefs.getString("zabbix_password", "");
		if (url.length()==0 || user.length()==0 || pwd.length()==0) {
			// no data
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("zabbix_last_prefs", "");
			editor.commit();
			return false;
		} else {
			// if preferences were changed
			String lastPrefs = prefs.getString("zabbix_last_prefs", null);
			if (authError || lastPrefs == null || !lastPrefs.equals(url+"#"+user+"#"+pwd)) {
				authError = false;
				// reload data
				((ClearLocalDatabaseSupport) mActivity).clearLocalDatabase();
				showLoading();
			} else if (mActivity instanceof MainActivitySmartphone) {
				if (((MainActivitySmartphone) mActivity).getTabHost().getCurrentTabTag().equals("tab_status")) {
					((MainActivitySmartphone) mActivity).setupListViewProblems();
				}
			}
		}
		return true;
	}

	private void checkZabbixLogin() {
		if (!_checkZabbixLogin()) {
			// no data
			if (dlgSetPreferences == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setMessage(R.string.set_preferences).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(mActivity.getApplicationContext(), AppPreferenceActivity.class);
						mActivity.startActivityForResult(intent, 0);
					}
				});
				dlgSetPreferences = builder.create();
			}
			dlgSetPreferences.show();
		}
	}

	public void enableBonusModus() {
		Log.i("MainActivitySupport", "bonus bought, enable");
		setMenuItemBuyBonusEnabled(false);
		AdView ad = (AdView) mActivity.findViewById(R.id.adView);
		ad.setVisibility(View.GONE);
	}

	public Dialog getDlgFailureMessage() {
		return dlgFailureMessage;
	}

	public void hideLoading() {
		if (loadingDlg != null) {
			loadingDlg.hide();
		}
	}

	public void menuItemBuyBonusSelected() {
		if (Consts.DEBUG) {
			Log.d("MainActivitySmartphone", "buying productId: " + ZabbixMobilePurchaseObserver.PRODUCT_ID_BONUS);
		}
		if (!mBillingService.requestPurchase(ZabbixMobilePurchaseObserver.PRODUCT_ID_BONUS)) {
			Toast.makeText(mActivity, R.string.billing_not_supported_message, Toast.LENGTH_LONG).show();
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		if (mHandler == null) mHandler = new Handler();

		if (savedInstanceState != null) {
			AppView view = (AppView) savedInstanceState.getSerializable("currentView");
			((CurrentViewSupport) mActivity).setCurrentView(view);
		}

		// in-app billing
		mZabbixMobilePurchaseObserver = new ZabbixMobilePurchaseObserver(mActivity, mHandler, this);
		mBillingService = new BillingService();
		mBillingService.setContext(mActivity);
		// Check if billing is supported.
		ResponseHandler.register(mZabbixMobilePurchaseObserver);
		if (!mBillingService.checkBillingSupported()) {
			Log.e("MainActivitySmartphone", "cannot connect");
			Toast.makeText(mActivity, R.string.cannot_connect_message, Toast.LENGTH_LONG).show();
		}
	}

	public void onPause() {
		mActivity.unregisterReceiver(contentProviderReceiver);
	}

	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuitem_buy_bonus).setEnabled(menuItemBuyBonusEnabled);
	}

	public void onResume() {
		if (mHandler == null) mHandler = new Handler();

		// check zabbix login
		checkZabbixLogin();

		// broadcast receiver for handling problems
		mActivity.registerReceiver(
			contentProviderReceiver, new IntentFilter(ZabbixContentProvider.CONTENT_PROVIDER_INTENT_ACTION)
		);

		// push notifications
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		boolean push = prefs.getBoolean("zabbix_push_enabled", false);
		AlarmManager am = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mActivity, PushAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if (!push) {
			// stop service
			am.cancel(pendingIntent);
		} else if (push) {
			// start service
			String subscribe_key = prefs.getString("zabbix_push_subscribe_key", "").trim();
			if (!subscribe_key.startsWith("sub-")) {
				// invalid subscribe key
				showFailureMessage(R.string.invalid_subscribe_key, false);
			} else {
				Intent messageservice = new Intent(mActivity, PushService.class);
				mActivity.startService(messageservice);

				am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
						(5 * 60 * 1000), pendingIntent); //wake up every 5 minutes to ensure service stays alive
			}
		}

		// was the activity started from a push notification?
		Intent startedIntent = mActivity.getIntent();
		if (startedIntent != null) {
			long triggerid = startedIntent.getLongExtra("pushNotificationTriggerid", 0);
			startedIntent.removeExtra("pushNotificationTriggerid");
			if (triggerid != 0) {
				((CurrentViewSupport) mActivity).setCurrentView(AppView.PROBLEMS);
				loadContentSupport.showDetailsFromTriggerId(triggerid);
			}
			NotificationManager mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel((int) triggerid);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("currentView", ((CurrentViewSupport) mActivity).getCurrentView());
	}

	public void onStop() {
		if (loadingDlg != null) {
			// cleanup to prevent android.view.WindowLeaked error
			loadingDlg.dismiss();
			loadingDlg = null;
		}
	}

	/**
	 * If the database has not been initialized, we send a
	 * RESTORE_TRANSACTIONS request to Android Market to get the list of purchased items
	 * for this user. This happens if the application has just been installed
	 * or the user wiped data. We do not want to do this on every startup, rather, we want to do
	 * only when the database needs to be initialized.
	 */
	public void restoreTransactions() {
		SharedPreferences prefs = mActivity.getPreferences(Activity.MODE_PRIVATE);
		boolean bonus = prefs.getBoolean("bonus_purchased", false);
		if (bonus) {
			enableBonusModus();
		} else {
			showAds(); // die ads werden evtl. nach dem restore wieder ausgeblendet

			boolean initialized = prefs.getBoolean("_TRANSACTION_RESTORED_", false);
			if (!initialized) {
				mBillingService.restoreTransactions();
				Toast.makeText(mActivity, R.string.restoring_transactions, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void setMenuItemBuyBonusEnabled(boolean supported) {
		menuItemBuyBonusEnabled = supported;
		// only for API level >= 11
		try {
			Method m_invalidateOptionsMenu = mActivity.getClass().getMethod("invalidateOptionsMenu");
			m_invalidateOptionsMenu.invoke(mActivity);
		} catch (Exception e) {
		}
	}

	private void showAds() {
		// ads
		AdView ad = (AdView) mActivity.findViewById(R.id.adView);
		ad.setVisibility(View.VISIBLE);
		AdRequest req = new AdRequest();
		req.addTestDevice("A3CEC6A7C076E3806760FB8D9FC9B2B2"); //smartphone
		req.addTestDevice("546B38B3635BB0C39CC6107BCE24AB9C"); //tablet
		ad.loadAd(req);
	}

	/**
	 * show error message popup
	 * @param textresid resource id
	 */
	public void showFailureMessage(int textresid, boolean btnSettings) {
		showFailureMessage(mActivity.getResources().getString(textresid), btnSettings);
	}

	public void showFailureMessage(String content, boolean btnSettings) {
		if (dlgFailureMessage == null) {
			dlgFailureMessage = new Dialog(mActivity);
			dlgFailureMessage.setTitle(R.string.failure);
			dlgFailureMessage.setContentView(R.layout.dialog_failure_message);
			dlgFailureMessage.setCanceledOnTouchOutside(true);
			dlgFailureMessage.findViewById(R.id.btn_refresh_current_view).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							((ClearLocalDatabaseSupport) mActivity).clearLocalDatabase();
						}
					}, 4000);
					dlgFailureMessage.dismiss();
					Toast.makeText(mActivity, R.string.refreshing_current_view, Toast.LENGTH_LONG).show();
				}
			});
			dlgFailureMessage.findViewById(R.id.btn_show_preferences).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dlgFailureMessage.dismiss();
					Intent intent = new Intent(mActivity.getApplicationContext(), AppPreferenceActivity.class);
					mActivity.startActivityForResult(intent, 0);
				}
			});
		}
		dlgFailureMessage.findViewById(R.id.btn_refresh_current_view).setVisibility(btnSettings?View.GONE:View.VISIBLE);
		dlgFailureMessage.findViewById(R.id.btn_show_preferences).setVisibility(btnSettings?View.VISIBLE:View.GONE);

		TextView txt = (TextView) dlgFailureMessage.findViewById(R.id.dlg_failuremessage_message);
		txt.setText(content);
		mActivity.showDialog(DIALOG_FAILURE_MESSAGE);
	}

	public void showLoading() {
		if (loadingDlg == null) {
			loadingDlg = new ProgressDialog(mActivity);
			loadingDlg.setTitle(R.string.loading);
			loadingDlg.setMessage(mActivity.getResources().getString(R.string.fetching_remote_data));
			loadingDlg.setIndeterminate(false);
			loadingDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		loadingDlg.show();
		loadingDlg.setProgress(0);
	}
}
