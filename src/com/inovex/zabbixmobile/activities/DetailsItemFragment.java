package com.inovex.zabbixmobile.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.support.DetailsItemFragmentSupport;
import com.inovex.zabbixmobile.model.ItemData;

public class DetailsItemFragment extends BaseDetailsFragment implements LoaderCallbacks<Cursor> {
	private static final int LOADER_ID_CHART = 1;
	private static final int LOADER_ID_ITEM = 2;
	private String currentGraphDescription;
	private String currentGraphText;
	private DetailsItemFragmentSupport support;
	private long loadTriggerDetails;

	private void loadChart(long itemid, String units, String description, String graphText) {
		showLoadingLayout();

		currentGraphDescription = description;
		currentGraphText = graphText;

		Bundle bundle = new Bundle(1);
		bundle.putString("uri", Uri.parse(ZabbixContentProvider.CONTENT_URI_HISTORY_DETAILS.toString()+"/"+itemid).toString());
		getLoaderManager().restartLoader(LOADER_ID_CHART, bundle, this);
	}

	public void loadFromItemId(long itemid) {
		Bundle bundle = new Bundle(1);
		bundle.putString("uri", Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_ITEMS, "/"+itemid).toString());
		getLoaderManager().restartLoader(LOADER_ID_ITEM, bundle, this);
	}

	/**
	 * loads trigger details
	 * @param triggerid
	 */
	public void loadFromTriggerId(long triggerid) {
		Bundle bundle = new Bundle(2);
		bundle.putString("uri", Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+triggerid+"/items").toString());
		bundle.putLong("loadTriggerDetails", triggerid);
		getLoaderManager().restartLoader(LOADER_ID_ITEM, bundle, this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		support = new DetailsItemFragmentSupport(getActivity());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		loadTriggerDetails = bundle.getLong("loadTriggerDetails", 0);
		return new CursorLoader(getActivity(), Uri.parse(bundle.getString("uri")), null, null, null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == LOADER_ID_ITEM) {
			if (loadTriggerDetails != 0) {
				// load trigger details
				DetailsTriggerFragment fr = (DetailsTriggerFragment) getFragmentManager().findFragmentById(R.id.fragment_details_trigger);
				fr.loadFromTriggerId(loadTriggerDetails);
			}
			if (cursor.moveToFirst()) {
				String txt = support.getItemText(cursor);
				loadChart(
						cursor.getLong(cursor.getColumnIndex(ItemData.COLUMN_ITEMID))
						, cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_UNITS))
						, cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_DESCRIPTION))
						, txt
				);
			} else {
				Toast.makeText(getActivity(), getResources().getString(R.string.no_items_to_display), Toast.LENGTH_SHORT).show();
				ViewGroup root = (ViewGroup) getView();
				root.removeAllViews();
			}
		} else if (loader.getId() == LOADER_ID_CHART) {
			showLayout(R.layout.details_item);

			TextView textGraph = (TextView) getActivity().findViewById(R.id.textview_graph);
			textGraph.setText(currentGraphText);

			support.showGraph(cursor, currentGraphDescription);
		}
	}
}
