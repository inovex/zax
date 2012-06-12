package com.inovex.zabbixmobile.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.support.MainActivitySupport;
import com.inovex.zabbixmobile.billing.BillingService.RequestPurchase;
import com.inovex.zabbixmobile.billing.BillingService.RestoreTransactions;
import com.inovex.zabbixmobile.billing.Consts.PurchaseState;
import com.inovex.zabbixmobile.billing.Consts.ResponseCode;

/**
 * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
 * messages to this application so that we can update the UI.
 */
public class ZabbixMobilePurchaseObserver extends PurchaseObserver {
	public static final String PRODUCT_ID_BONUS = "bonus";
	private static final String TAG = "ZabbixMobilePurchaseObserver";
	private final Activity mActivity;
	private final MainActivitySupport mMainActivitySupport;

	public ZabbixMobilePurchaseObserver(Activity activity, Handler handler, MainActivitySupport mainActivitySupport) {
		super(activity, handler);
		mActivity = activity;
		mMainActivitySupport = mainActivitySupport;
	}

	@Override
	public void onBillingSupported(boolean supported) {
		if (Consts.DEBUG) {
			Log.i(TAG, "supported: " + supported);
		}
		mMainActivitySupport.setMenuItemBuyBonusEnabled(supported);
		if (supported) {
			mMainActivitySupport.restoreTransactions();
		} else {
			Toast.makeText(mActivity, R.string.billing_not_supported_message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, long purchaseTime) {
		if (Consts.DEBUG) {
			Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
		}

		if (purchaseState == PurchaseState.PURCHASED) {
			if (itemId.equals(PRODUCT_ID_BONUS)) {
				mMainActivitySupport.enableBonusModus();

				SharedPreferences prefs = mActivity.getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean("bonus_purchased", true);
				edit.commit();
			}
		}
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		if (Consts.DEBUG) {
			Log.d(TAG, request.mProductId + ": " + responseCode);
		}
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.i(TAG, "purchase was successfully sent to server");
			}
		} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
			if (Consts.DEBUG) {
				Log.i(TAG, "user canceled purchase");
			}
		} else {
			if (Consts.DEBUG) {
				Log.i(TAG, "purchase failed, returned " + responseCode);
			}
		}
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.d(TAG, "completed RestoreTransactions request");
			}
			// Update the shared preferences so that we don't perform
			// a RestoreTransactions again.
			SharedPreferences prefs = mActivity.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putBoolean("_TRANSACTIONS_RESTORED_", true);
			edit.commit();
		} else if (Consts.DEBUG) {
			Log.d(TAG, "RestoreTransactions error: " + responseCode);
		}
	}
}
