package com.inovex.zabbixmobile;

import com.inovex.zabbixmobile.api.ZabbixService;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class ZabbixContentProvider extends ContentProvider {

	protected static final String TAG = ZabbixContentProvider.class
			.getSimpleName();
	private ZabbixService mZabbixService;
	private boolean mBound = false;
	private Context mContext;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder serviceBinder) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ZabbixService.ZabbixBinder binder = (ZabbixService.ZabbixBinder) serviceBinder;
			mZabbixService = binder.getService();
			Log.d(TAG, "service connected");
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "service disconnected");
			mBound = false;
			// we have lost the connection unexpectedly -> try to rebind to
			// service
			Intent intent = new Intent(mContext, ZabbixService.class);
			mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	};

	@Override
	public boolean onCreate() {
		mContext = getContext();
		// Bind to LocalService
		Intent intent = new Intent(mContext, ZabbixService.class);
		if (!mContext
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
			Log.e(TAG, "bindService() not successful.");
			return false;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void shutdown() {
		// Unbind from the service
		if (mBound) {
			mContext.unbindService(mConnection);
			mBound = false;
		}
	}

}
