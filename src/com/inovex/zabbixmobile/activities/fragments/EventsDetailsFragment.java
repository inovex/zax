package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.listeners.OnAcknowledgeEventListener;
import com.inovex.zabbixmobile.model.Event;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class EventsDetailsFragment extends
		BaseSeverityFilterDetailsFragment<Event> {

	public static final String TAG = EventsDetailsFragment.class
			.getSimpleName();

	MenuItem mMenuItemAcknowledge;

	@Override
	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getEventsDetailsPagerAdapter(mSeverity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.events_details, menu);
		mMenuItemAcknowledge = menu.findItem(R.id.menuitem_acknowledge_event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_acknowledge_event:
			Event e = mDetailsPagerAdapter.getCurrentItem();
			Log.d(TAG, "Acknowledge event: " + e);
			// show dialog
			AcknowledgeDialogFragment dialog = AcknowledgeDialogFragment
					.getInstance();
			dialog.setEvent(e);
			dialog.show(getChildFragmentManager(),
					AcknowledgeDialogFragment.TAG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Enables/disables the menu button for event acknowledgement.
	 * 
	 * @param enabled
	 *            true: enable; false: disable the button
	 */
	public void setAcknowledgeButtonEnabled(boolean enabled) {
		mMenuItemAcknowledge.setVisible(enabled);
	}
	
	@Override
	public void selectItem(int position, long id) {
		super.selectItem(position, id);
		Event e = mDetailsPagerAdapter.getCurrentItem();
		if (e.isAcknowledged())
			this.setAcknowledgeButtonEnabled(false);
		else
			this.setAcknowledgeButtonEnabled(true);
	}

	/**
	 * The dialog displayed when an event shall be acknowledged.
	 * 
	 */
	public static class AcknowledgeDialogFragment extends DialogFragment {

		public static final String TAG = AcknowledgeDialogFragment.class
				.getSimpleName();

		private OnAcknowledgeEventListener mCallback;
		private Event mEvent;

		public static AcknowledgeDialogFragment getInstance() {
			return new AcknowledgeDialogFragment();
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			try {
				mCallback = (OnAcknowledgeEventListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement OnAcknowledgeEventListener.");
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();

			View view = inflater.inflate(R.layout.dialog_acknowledge, null);
			builder.setView(view);
			
			final EditText comment = (EditText) view
					.findViewById(R.id.acknowledge_comment);
			// add title
			builder.setTitle(R.string.acknowledge_event);
			// Add action buttons
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							mCallback.acknowledgeEvent(mEvent, comment
									.getText().toString());
						}
					});
			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							AcknowledgeDialogFragment.this.getDialog().cancel();
						}
					});
			return builder.create();
		}

		public void setEvent(Event event) {
			this.mEvent = event;
		}

	}

}
