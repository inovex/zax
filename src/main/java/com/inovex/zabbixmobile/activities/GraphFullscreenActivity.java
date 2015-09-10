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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.listeners.OnGraphDataLoadedListener;
import com.inovex.zabbixmobile.listeners.OnGraphsLoadedListener;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

/**
 * Activity showing a graph in fullscreen landscape mode.
 */
public class GraphFullscreenActivity extends AppCompatActivity implements
		ServiceConnection {
	private LinearLayout mLayout;
	private long mItemId;
	private long mGraphId;

	protected ZabbixDataService mZabbixDataService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ZaxPreferences prefs = ZaxPreferences
				.getInstance(getApplicationContext());
		if (prefs.isDarkTheme())
			setTheme(R.style.AppThemeDark);
		else
			setTheme(R.style.AppTheme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			return;

		bindService();

		mItemId = getIntent().getLongExtra("itemid", -1);
		mGraphId = getIntent().getLongExtra("graphid", -1);
		if (mItemId == -1 && mGraphId == -1) {
			finish();
			return;
		}

		mLayout = new LinearLayout(this);

		setContentView(mLayout);
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item))
			return true;

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
		loadGraphData();
	}

	private void loadGraphData() {
		FragmentManager fm = getSupportFragmentManager();
		final LoadingDialogFragment loadingDlg = new LoadingDialogFragment();
		loadingDlg.show(fm, LoadingDialogFragment.TAG);

		// load data by item
		if (mItemId != -1) {
			final Item item = mZabbixDataService.getItemById(mItemId);
			if (item == null)
				return;
			mZabbixDataService.loadHistoryDetailsByItem(item, false,
					new OnGraphDataLoadedListener() {
						@Override
						public void onGraphDataLoaded() {
							LineGraphView graph = GraphUtil
									.createItemGraphFullscreen(
											GraphFullscreenActivity.this, item);
							if (mLayout != null) {
								mLayout.addView(graph);
							}
							if (loadingDlg != null) {
								loadingDlg.dismiss();
							}
						}

						@Override
						public void onGraphDataProgressUpdate(int progress) {
							// TODO Auto-generated method stub

						}
					});
		}

		// load data by graph
		else if (mGraphId != -1) {
			final Graph graph = mZabbixDataService.getGraphById(mGraphId);
			if (graph == null)
				return;
			mZabbixDataService.loadGraph(graph, new OnGraphsLoadedListener() {
				@Override
				public void onGraphsLoaded() {
					LineGraphView graphview = GraphUtil
							.createScreenGraphFullscreen(
									GraphFullscreenActivity.this, graph);
					if (mLayout != null) {
						mLayout.addView(graphview);
					}
					if (loadingDlg != null) {
						loadingDlg.dismiss();
					}
				}

				@Override
				public void onGraphsProgressUpdate(int progress) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	/**
	 * The dialog displayed when an event shall be acknowledged.
	 * 
	 */
	public static class LoadingDialogFragment extends DialogFragment {

		public static final String TAG = LoadingDialogFragment.class
				.getSimpleName();

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog mLoadingDlg = new ProgressDialog(getActivity());
			mLoadingDlg.setMessage(getActivity().getResources().getString(
					R.string.loading));
			return mLoadingDlg;
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub

	}

}
