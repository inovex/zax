package com.inovex.zabbixmobile.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.support.DetailsEventFragmentSupport;
import com.inovex.zabbixmobile.activities.support.ShowNoDataMessageSupport;
import com.inovex.zabbixmobile.model.EventData;
import com.inovex.zabbixmobile.view.PromptDialog;

public class DetailsEventFragment extends BaseDetailsFragment implements LoaderCallbacks<Cursor>, ShowNoDataMessageSupport {
	private DetailsEventFragmentSupport support;

	private void acknowledgeEvent(final long eventid, String input) {
		Uri uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_EVENTS, "/"+eventid);
		ContentValues values = new ContentValues(2);
		values.put(EventData.COLUMN_ACK, 1);
		values.put("_comment", input);

		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				if (result > 0) {
					// ok
					loadFromEventId(eventid);
				} else {
					// error
					Toast.makeText(getActivity(), getResources().getString(R.string.could_not_acknowledge_event), Toast.LENGTH_LONG);
				}
			}
		};
		queryHandler.startUpdate(0, null, uri, values, null, null);
	}

	public void loadFromEventId(long eventid) {
		Cursor cur = getActivity().getContentResolver().query(Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_EVENTS, "/"+eventid), null, null, null, null);
		showLayout(R.layout.fragment_content_details_event);
		support.setData(null, cur);
	}

	public void loadFromTriggerId(final long triggerid) {
		showLoadingLayout();

		Bundle bundle = new Bundle(1);
		bundle.putLong("triggerid", triggerid);

		getLoaderManager().restartLoader(0, bundle, this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		support = new DetailsEventFragmentSupport(getActivity(), this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(
				getActivity()
				, Uri.withAppendedPath(
						ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+bundle.getLong("triggerid")+"/events"
				), null, null, null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		showLayout(R.layout.fragment_content_details_event);
		support.setData(null, cursor);
	}

	public boolean onMenuItemAcknowledgeSelected() {
		new PromptDialog(getActivity(), R.string.acknowledge, R.string.enter_comment) {
			@Override
			public boolean onOkClicked(String input) {
				acknowledgeEvent(support.getLastEventId(), input);
				return true;
			}
		}.show();
		return true;
	}

	@Override
	public void showNoDataMessage(String kind) {
		getActivity().findViewById(R.id.menuitem_acknowledge).setEnabled(false);
		showLayout(R.layout.details_no_data);
		((TextView) getView().findViewById(R.id.details_no_data_text)).setText(R.string.no_event_data);
	}
}
