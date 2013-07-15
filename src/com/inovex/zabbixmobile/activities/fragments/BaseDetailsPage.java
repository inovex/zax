package com.inovex.zabbixmobile.activities.fragments;

import java.util.Collection;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnHistoryDetailsLoadedListener;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.util.GraphUtil;
import com.jjoe64.graphview.LineGraphView;

public abstract class BaseDetailsPage extends BaseServiceConnectedFragment implements OnHistoryDetailsLoadedListener {

	protected boolean mHistoryDetailsImported = false;
	protected Collection<HistoryDetail> mHistoryDetails;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("OC", this + "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("OC", this + "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d("OC", this + "onViewCreated");

		fillDetailsText();
		
		if (mHistoryDetails != null)
			showGraph();
	}

	protected abstract void fillDetailsText();

	protected void showGraph(ViewGroup layout, Item item) {
		// create graph and add it to the layout
		int numEntries = mHistoryDetails.size();
		if (numEntries > 0 && item != null) {
			LineGraphView graph = GraphUtil.createItemGraph(getSherlockActivity(), mHistoryDetails, item.getDescription());
			layout.removeAllViews();
			layout.addView(graph);
		} else {
			// no history data available
			layout.removeAllViews();
			TextView noGraphDataView = new TextView(getSherlockActivity());
			noGraphDataView.setText(R.string.no_history_data_found);
			layout.addView(noGraphDataView);
		}
	}

	protected abstract void showGraph();

	@Override
	public void onHistoryDetailsLoaded(Collection<HistoryDetail> historyDetails) {
		mHistoryDetailsImported  = true;
		mHistoryDetails = historyDetails;
		if (getView() != null) {
			showGraph();
		}
	}

}
