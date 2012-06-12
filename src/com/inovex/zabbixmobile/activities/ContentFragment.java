package com.inovex.zabbixmobile.activities;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;
import com.inovex.zabbixmobile.activities.support.ContentFragmentSupport;
import com.inovex.zabbixmobile.activities.support.LoadContentSupport;
import com.inovex.zabbixmobile.model.ApplicationData;

public class ContentFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemClickListener, LoadContentSupport {
	private static final int LOADER_APPLICATIONS = 1;
	private static final int LOADER_ITEMS = 2;
	private static final int LOADER_EVENTS = 3;
	private static final int LOADER_PROBLEMS = 4;
	private static final int LOADER_GRAPHS = 5;

	private ContentFragmentSupport contentSupport;
	private Animation animationFadeIn;
	private AppView currentView;
	private ListView listChecksApplications;
	private ListView listChecksItems;
	private long currentItem__id;
	private ListView listEvents;
	private ListView listProblems;
	private LinearLayout layoutGraphs;

	private String lastView;

	public String getLastView() {
		return lastView;
	}

	private void hideAllViews() {
		View listProblems = getActivity().findViewById(R.id.list_problems);
		if (listProblems != null) {
			listProblems.setVisibility(View.GONE);
		}
		View layoutDetails = getActivity().findViewById(R.id.layout_content_details);
		if (layoutDetails != null) {
			layoutDetails.setVisibility(View.GONE);
		}
		View listEvents = getActivity().findViewById(R.id.listview_events);
		if (listEvents != null) {
			listEvents.setVisibility(View.GONE);
		}
		View listChecks = getActivity().findViewById(R.id.layout_content_checks);
		if (listChecks != null) {
			listChecks.setVisibility(View.GONE);
		}
		View layoutGraphs = getActivity().findViewById(R.id.layout_graphs);
		if (layoutGraphs != null) {
			((View) layoutGraphs.getParent()).setVisibility(View.GONE);
		}
	}

	private void hideLoading() {
		getActivity().findViewById(R.id.fragment_content_loading).setVisibility(View.GONE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		contentSupport = new ContentFragmentSupport(getActivity());

		listChecksApplications = (ListView) getActivity().findViewById(R.id.listview_checks_applications);
		listChecksApplications.setOnItemClickListener(this);
		listChecksApplications.setAdapter(
				new SimpleCursorAdapter(
						getActivity()
						, android.R.layout.simple_list_item_single_choice
						, null
						, new String[] {ApplicationData.COLUMN_NAME}
						, new int[] {android.R.id.text1}
				) {
					@Override
					public void bindView(View view, Context context, Cursor cursor) {
						super.bindView(view, context, cursor);
						view.setTag(cursor.getLong(cursor.getColumnIndex(ApplicationData.COLUMN_APPLICATIONID)));
					}
				}
		);

		listChecksItems = (ListView) getActivity().findViewById(R.id.listview_checks_items);
		listChecksItems.setOnItemClickListener(this);
		listChecksItems.setAdapter(
				new ResourceCursorAdapter(
						getActivity()
						, R.layout.list_checks_child_entry
						, null
				) {
					@Override
					public void bindView(View view, Context context, Cursor cursor) {
						contentSupport.setupListChecksItemsAdapterBindView(view, context, cursor);
					}
				}
		);

		listEvents = (ListView) getActivity().findViewById(R.id.listview_events);
		contentSupport.setupListEventsAdapter(listEvents);
		listEvents.setOnItemClickListener(this);

		layoutGraphs = (LinearLayout) getActivity().findViewById(R.id.layout_graphs);

		hideLoading();
	}

	/**
	 * @return true if event was processed. false, if not yet.
	 */
	public boolean onBackPressed() {
		// if details are shown, go back to the list
		View layout = getActivity().findViewById(R.id.layout_content_details);
		if (layout.getVisibility() == View.VISIBLE) {
			layout.setVisibility(View.GONE);

			if (currentView == AppView.PROBLEMS) {
				View listProblems = getActivity().findViewById(R.id.list_problems);
				if (listProblems != null) {
					listProblems.setVisibility(View.VISIBLE);
					listProblems.startAnimation(animationFadeIn);
				}
			} else if (currentView == AppView.EVENTS) {
				View listEvents = getActivity().findViewById(R.id.listview_events);
				listEvents.setVisibility(View.VISIBLE);
				listEvents.startAnimation(animationFadeIn);
			} else if (currentView == AppView.CHECKS) {
				View listChecks = getActivity().findViewById(R.id.layout_content_checks);
				listChecks.setVisibility(View.VISIBLE);
				listChecks.startAnimation(animationFadeIn);
			}

			lastView = null;
			setMenuItemAckEnabled(false);
			return true;
		}

		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		animationFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(getActivity(), Uri.parse(bundle.getString("uri")), null, null, null, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_content, container);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int arg2, long arg3) {
		if (list == listChecksApplications) {
			Uri uri = Uri.parse(
					ZabbixContentProvider.CONTENT_URI_HOSTS.toString()
					+"/"+currentItem__id
					+"/applications"
					+"/"+view.getTag()
					+"/items");
			Bundle bundle = new Bundle(1);
			bundle.putString("uri", uri.toString());
			getLoaderManager().restartLoader(LOADER_ITEMS, bundle, this);
		} else if (list == listChecksItems) {
			contentSupport.onChecksItemClick(this, view);
		} else if (list == listEvents) {
			contentSupport.onEventItemClick(this, view);
		} else if (list == listProblems) {
			contentSupport.onProblemItemClick(this, view);
		}
	}

	public void onItemSelected(long item__id) {
		lastView = "onItemSelected//"+item__id;

		if (currentView == AppView.PROBLEMS) {
			onItemSelectedProblems(item__id);
		} else if (currentView == AppView.CHECKS) {
			onItemSelectedChecks(item__id);
		} else if (currentView == AppView.SCREENS) {
			onItemSelectedScreens(item__id);
		}
	}

	private void onItemSelectedChecks(long host__id) {
		// jump back to list (if so)
		View layoutContentChecks = getActivity().findViewById(R.id.layout_content_checks);
		if (!onBackPressed()) {
			layoutContentChecks.setVisibility(View.VISIBLE);
			layoutContentChecks.startAnimation(animationFadeIn);
		}

		// reset list, if another host was selected
		if (listChecksApplications.getCheckedItemPosition() != ListView.INVALID_POSITION && currentItem__id != host__id) {
			listChecksApplications.setItemChecked(listChecksApplications.getCheckedItemPosition(), false);
			listChecksItems.setVisibility(View.GONE);
		}

		currentItem__id = host__id;
		showLoading();
		Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_HOSTS.toString()+"/"+host__id+"/applications");
		Bundle bundle = new Bundle(1);
		bundle.putString("uri", uri.toString());
		getLoaderManager().restartLoader(LOADER_APPLICATIONS, bundle, this);
	}

	private void onItemSelectedProblems(long host__id) {
		View stub = getActivity().findViewById(R.id.fragment_content_stub_problems);
		if (stub != null) {
			stub.setVisibility(View.VISIBLE);
		}

		listProblems = (ListView) getActivity().findViewById(R.id.list_problems);
		listProblems.setVisibility(View.VISIBLE);
		if (!onBackPressed()) {
			listProblems.startAnimation(animationFadeIn);
		}

		// init
		if (listProblems.getAdapter() == null) {
			listProblems.setOnItemClickListener(this);
			contentSupport.setupListProblemsAdapter(listProblems);
		}

		showLoading();
		Uri uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_HOSTS, "/"+host__id+"/triggers");
		Bundle bundle = new Bundle(1);
		bundle.putString("uri", uri.toString());
		getLoaderManager().restartLoader(LOADER_PROBLEMS, bundle, this);
	}

	private void onItemSelectedScreens(long item__id) {
		// show the graphs
		currentItem__id = item__id;

		showLoading();
		Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_SCREENS.toString()+"/"+item__id+"/graphs/historydetails");
		Bundle bundle = new Bundle(1);
		bundle.putString("uri", uri.toString());
		getLoaderManager().restartLoader(LOADER_GRAPHS, bundle, this);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == LOADER_ITEMS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listChecksItems.getAdapter();
			adapter.swapCursor(null);
		} else if (loader.getId() == LOADER_APPLICATIONS) {
			ResourceCursorAdapter adapter = (SimpleCursorAdapter) listChecksApplications.getAdapter();
			adapter.swapCursor(null);
		} else if (loader.getId() == LOADER_EVENTS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listEvents.getAdapter();
			adapter.swapCursor(null);
		} else if (loader.getId() == LOADER_PROBLEMS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listEvents.getAdapter();
			adapter.swapCursor(null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		ResourceCursorAdapter adapter;
		if (loader.getId() == LOADER_APPLICATIONS) {
			adapter = (ResourceCursorAdapter) listChecksApplications.getAdapter();
		} else if (loader.getId() == LOADER_ITEMS) {
			adapter = (ResourceCursorAdapter) listChecksItems.getAdapter();
			listChecksItems.setVisibility(View.VISIBLE);
		} else if (loader.getId() == LOADER_EVENTS) {
			adapter = (ResourceCursorAdapter) listEvents.getAdapter();
		} else if (loader.getId() == LOADER_PROBLEMS) {
			adapter = (ResourceCursorAdapter) listProblems.getAdapter();
		} else if (loader.getId() == LOADER_GRAPHS) {
			adapter = null;
			((View) layoutGraphs.getParent()).setVisibility(View.VISIBLE);
			contentSupport.showScreenGraphs(cursor, getActivity().findViewById(R.id.listview_events_empty), layoutGraphs);
		} else {
			throw new IllegalStateException();
		}

		if (adapter != null) adapter.swapCursor(cursor);
		hideLoading();
	}

	public boolean onMenuItemAcknowledgeSelected() {
		DetailsEventFragment eventFragment = (DetailsEventFragment) getFragmentManager().findFragmentById(R.id.fragment_details_event);
		return eventFragment.onMenuItemAcknowledgeSelected();
	}

	public boolean onMenuItemClearSelected() {
		AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {
			@Override
			protected void onDeleteComplete(int token, Object cookie, int result) {
				getActivity().findViewById(R.id.menuitem_clear).setEnabled(true);
				((MainActivityTablet) getActivity()).reloadCurrentView();
			}
		};
		handler.startDelete(0, null, ZabbixContentProvider.CONTENT_URI__ALL_DATA, null, null);
		View mClear = getActivity().findViewById(R.id.menuitem_clear);
		if (mClear != null) {
			mClear.setEnabled(false);
		}
		return true;
	}

	public void restoreLastView(final String lastView) {
		// restore last state
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (lastView != null) {
						String[] sub = lastView.split("//");
						if (lastView.startsWith("onItemSelected//")) {
							onItemSelected(Long.parseLong(sub[1]));
						} else if (lastView.startsWith("showDetailsFromEventAndTriggerId")) {
							showDetailsFromEventAndTriggerId(Long.parseLong(sub[1]), Long.parseLong(sub[2]));
						} else if (lastView.startsWith("showDetailsFromItemId")) {
							showDetailsFromItemId(Long.parseLong(sub[1]));
						} else if (lastView.startsWith("showDetailsFromTriggerId")) {
							showDetailsFromTriggerId(Long.parseLong(sub[1]));
						}
					}
				} catch (Throwable x) {
					// ignore
					x.printStackTrace();
				}
			}
		}, 500); // 0,5 sec. delay workaround
	}

	public void setLoadingProgress(int progress) {
		ProgressBar p = (ProgressBar) ((RelativeLayout) getActivity().findViewById(R.id.fragment_content_loading)).getChildAt(0);
		p.setProgress(progress);
	}

	private void setMenuItemAckEnabled(boolean b) {
		View v = getActivity().findViewById(R.id.menuitem_acknowledge);
		if (v != null) v.setEnabled(b);
	}

	@Override
	public void showDetailsFromEventAndTriggerId(long eventid, long triggerid) {
		lastView = "showDetailsFromEventAndTriggerId//"+eventid+"//"+triggerid;
		hideAllViews();

		DetailsItemFragment itemFragment = (DetailsItemFragment) getFragmentManager().findFragmentById(R.id.fragment_details_item);
		itemFragment.loadFromTriggerId(triggerid); // + trigger details

		DetailsEventFragment eventFragment = (DetailsEventFragment) getFragmentManager().findFragmentById(R.id.fragment_details_event);
		eventFragment.loadFromEventId(eventid);

		View layout = getActivity().findViewById(R.id.layout_content_details);
		layout.setVisibility(View.VISIBLE);
	}

	@Override
	public void showDetailsFromItemId(long itemid) {
		lastView = "showDetailsFromItemId//"+itemid;
		hideAllViews();

		DetailsTriggerFragment triggerFragment = (DetailsTriggerFragment) getFragmentManager().findFragmentById(R.id.fragment_details_trigger);
		triggerFragment.loadFromItemId(itemid);

		DetailsItemFragment itemFragment = (DetailsItemFragment) getFragmentManager().findFragmentById(R.id.fragment_details_item);
		itemFragment.loadFromItemId(itemid);

		View layout = getActivity().findViewById(R.id.layout_content_details);
		layout.setVisibility(View.VISIBLE);
	}

	@Override
	public void showDetailsFromTriggerId(long triggerid) {
		lastView = "showDetailsFromTriggerId//"+triggerid;
		hideAllViews();

		DetailsItemFragment itemFragment = (DetailsItemFragment) getFragmentManager().findFragmentById(R.id.fragment_details_item);
		itemFragment.loadFromTriggerId(triggerid); // + trigger details

		DetailsEventFragment eventFragment = (DetailsEventFragment) getFragmentManager().findFragmentById(R.id.fragment_details_event);
		eventFragment.loadFromTriggerId(triggerid);

		View layout = getActivity().findViewById(R.id.layout_content_details);
		layout.setVisibility(View.VISIBLE);
	}

	private void showEventsList() {
		// jump back to list (if so)
		final ListView listEvents = (ListView) getActivity().findViewById(R.id.listview_events);
		if (!onBackPressed()) {
			listEvents.setVisibility(View.VISIBLE);
			listEvents.startAnimation(animationFadeIn);
		}

		showLoading();
		Bundle bundle = new Bundle(1);
		bundle.putString("uri", ZabbixContentProvider.CONTENT_URI_EVENTS.toString());
		getLoaderManager().restartLoader(LOADER_EVENTS, bundle, this);
	}

	private void showLoading() {
		setLoadingProgress(1);
		getActivity().findViewById(R.id.fragment_content_loading).setVisibility(View.VISIBLE);
	}

	public void showView(AppView view) {
		getLoaderManager().destroyLoader(LOADER_APPLICATIONS);
		getLoaderManager().destroyLoader(LOADER_EVENTS);
		getLoaderManager().destroyLoader(LOADER_ITEMS);
		getLoaderManager().destroyLoader(LOADER_PROBLEMS);

		currentView = view;
		lastView = null;
		hideAllViews();
		hideLoading();

		if (currentView == AppView.EVENTS) {
			listEvents.setEmptyView(getActivity().findViewById(R.id.listview_events_empty));
			showEventsList();
		} else {
			getActivity().findViewById(R.id.listview_events_empty).setVisibility(View.GONE);
		}

		// disable ack button
		setMenuItemAckEnabled(false);
	}
}
