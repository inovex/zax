package com.inovex.zabbixmobile.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.support.DetailsTriggerFragmentSupport;
import com.inovex.zabbixmobile.activities.support.ShowNoDataMessageSupport;
import com.inovex.zabbixmobile.model.TriggerData;

public class DetailsTriggerFragment extends BaseDetailsFragment implements LoaderCallbacks<Cursor>, ShowNoDataMessageSupport {
	private boolean loadEventData;
	private DetailsTriggerFragmentSupport support;

	public void loadFromItemId(final long itemid) {
		showLoadingLayout();

		Bundle bundle = new Bundle(1);
		bundle.putString("uri", Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_ITEMS, "/"+itemid+"/triggers").toString());
		bundle.putBoolean("loadEventData", true);

		getLoaderManager().restartLoader(0, bundle, this);
	}

	public void loadFromTriggerId(long triggerid) {
		// load trigger details. here it is not necessary to load it asynchronous
		Cursor cursor = getActivity().getContentResolver().query(Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+triggerid), null, null, null, null);
		loadEventData = false;
		showLayout(R.layout.details_trigger);
		support.setData(null, cursor);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		support = new DetailsTriggerFragmentSupport(getActivity(), this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		loadEventData = bundle.getBoolean("loadEventData", false);
		return new CursorLoader(getActivity(), Uri.parse(bundle.getString("uri")), null, null, null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		showLayout(R.layout.details_trigger);
		support.setData(null, cursor);

		if (loadEventData) {
			loadEventData = false;
			// if there are data, the events can be loaded
			DetailsEventFragment eventFrag = (DetailsEventFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_details_event);
			if (cursor.moveToFirst()) {
				eventFrag.loadFromTriggerId(cursor.getLong(cursor.getColumnIndex(TriggerData.COLUMN_TRIGGERID)));
			} else {
				eventFrag.showNoDataMessage(null);
			}
		}
	}

	@Override
	public void showNoDataMessage(String kind) {
		showLayout(R.layout.details_no_data);
		((TextView) getView().findViewById(R.id.details_no_data_text)).setText(R.string.no_trigger_data);
	}
}

