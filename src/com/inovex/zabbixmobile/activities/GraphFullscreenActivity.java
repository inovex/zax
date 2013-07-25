package com.inovex.zabbixmobile.activities;

import java.util.Collection;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnGraphsLoadedListener;
import com.inovex.zabbixmobile.listeners.OnHistoryDetailsLoadedListener;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

public class GraphFullscreenActivity extends BaseActivity {
	private LinearLayout mLayout;
	private long mItemId;
	private long mGraphId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		mItemId = getIntent().getLongExtra("itemid", -1);
		mGraphId = getIntent().getLongExtra("graphid", -1);
		if (mItemId == -1 && mGraphId == -1) {
			finish();
			return;
		}
		
		mLayout = new LinearLayout(this);

		setContentView(mLayout);
	}
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		loadGraphData();
	}
	
	private void loadGraphData() {
		FragmentManager fm = getSupportFragmentManager();
		final LoadingDialogFragment loadingDlg = new LoadingDialogFragment();
		loadingDlg.show(fm, LoadingDialogFragment.TAG);
		

		// load data by item
		if (mItemId != -1) {
			final Item item = mZabbixDataService.getItemById(mItemId);
			mZabbixDataService.loadHistoryDetailsByItem(item, false, new OnHistoryDetailsLoadedListener() {
				@Override
				public void onHistoryDetailsLoaded() {
					Collection<HistoryDetail> historyDetails = item.getHistoryDetails();
					LineGraphView graph = GraphUtil.createItemGraphFullscreen(GraphFullscreenActivity.this, historyDetails, item.getDescription());
					if (mLayout != null) {
						mLayout.addView(graph);
					}
					if (loadingDlg != null) {
						loadingDlg.dismiss();
					}
				}
			});
		}
		
		// load data by graph
		else if (mGraphId != -1) {
			final Graph graph = mZabbixDataService.getGraphById(mGraphId);
			mZabbixDataService.loadGraph(graph, new OnGraphsLoadedListener() {
				@Override
				public void onGraphsLoaded() {
					LineGraphView graphview = GraphUtil.createScreenGraphFullscreen(GraphFullscreenActivity.this, graph);
					if (mLayout != null) {
						mLayout.addView(graphview);
					}
					if (loadingDlg != null) {
						loadingDlg.dismiss();
					}
				}
			});
		}
	}

	@Override
	protected void loadData() {
	}

	@Override
	protected void disableUI() {
	}

	@Override
	protected void enableUI() {
	}
	
	/**
	 * The dialog displayed when an event shall be acknowledged.
	 * 
	 */
	public static class LoadingDialogFragment extends DialogFragment {

		public static final String TAG = LoadingDialogFragment.class
				.getSimpleName();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog mLoadingDlg = new ProgressDialog(getActivity());
			mLoadingDlg.setMessage(getActivity().getResources().getString(R.string.loading));
			return mLoadingDlg;
		}
	}

}
