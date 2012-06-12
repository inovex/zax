package com.inovex.zabbixmobile.activities;

import java.util.Date;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.AppPreferenceActivity;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;
import com.inovex.zabbixmobile.activities.support.ClearLocalDatabaseSupport;
import com.inovex.zabbixmobile.activities.support.ContentFragmentSupport;
import com.inovex.zabbixmobile.activities.support.CurrentViewSupport;
import com.inovex.zabbixmobile.activities.support.DetailsEventFragmentSupport;
import com.inovex.zabbixmobile.activities.support.DetailsItemFragmentSupport;
import com.inovex.zabbixmobile.activities.support.DetailsTriggerFragmentSupport;
import com.inovex.zabbixmobile.activities.support.LoadContentSupport;
import com.inovex.zabbixmobile.activities.support.MainActivitySupport;
import com.inovex.zabbixmobile.activities.support.ShowNoDataMessageSupport;
import com.inovex.zabbixmobile.model.ApplicationData;
import com.inovex.zabbixmobile.model.EventData;
import com.inovex.zabbixmobile.model.ItemData;
import com.inovex.zabbixmobile.model.ScreenData;
import com.inovex.zabbixmobile.model.TriggerData;
import com.inovex.zabbixmobile.view.HieraticalHostListView;
import com.inovex.zabbixmobile.view.HieraticalHostListView.OnChildEntryClickListener;
import com.inovex.zabbixmobile.view.PromptDialog;

/**
 * GUI
 */
public class MainActivitySmartphone extends TabActivity implements CurrentViewSupport, LoadContentSupport, ShowNoDataMessageSupport, ClearLocalDatabaseSupport {
	private static final int DIALOG_DETAILS = 0;

	/**
	 * trigger priority => text
	 * @param prio zabbix
	 * @return i18n text
	 */
	public static String getTriggerPriorityText(Context context, int prio) {
		String[] triggerPriorityText = new String[] {
				context.getResources().getString(R.string.priority_not_classified)
				, context.getResources().getString(R.string.priority_information)
				, context.getResources().getString(R.string.priority_warning)
				, context.getResources().getString(R.string.priority_average)
				, context.getResources().getString(R.string.priority_high)
				, context.getResources().getString(R.string.priority_disaster)
		};
		return triggerPriorityText[prio];
	}
	private int loadingDlgStack = 0;
	private Integer setupListViewEventsFinished;
	private Integer setupListViewChecksFinished;
	private Integer setupListViewScreensFinished;
	private Integer setupListViewProblemsFinished;

	private Dialog dlgDetails;
	private Long lastShowChart_itemid;
	private String lastShowChart_units;
	private String lastShowChart_description;
	private String lastShowChart_graphText;
	private long lastShowScreen_screen__id = -1;

	private String prevTabTag;
	private ContentFragmentSupport contentSupport;
	private DetailsEventFragmentSupport detailsEventSupport;
	private DetailsTriggerFragmentSupport detailsTriggerSupport;
	private DetailsItemFragmentSupport detailsItemSupport;
	private boolean showDialogDetails;
	private MainActivitySupport support;

	void acknowledgeEvent(long eventid, String input) {
		showLoading();

		Uri uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_EVENTS, "/"+eventid);
		ContentValues values = new ContentValues(2);
		values.put(EventData.COLUMN_ACK, 1);
		values.put("_comment", input);

		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onUpdateComplete(int token, Object cookie, int result) {
				hideLoading();
				if (result > 0) {
					// no error
					dlgDetails.hide();
				} else {
					// error
					support.showFailureMessage(R.string.could_not_acknowledge_event, false);
				}
			}
		};
		queryHandler.startUpdate(0, null, uri, values, null, null);
	}

	/**
	 * sqlite db clear and if applicable reload lists
	 */
	@Override
	public void clearLocalDatabase() {
		showLoading();
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				getContentResolver().delete(ZabbixContentProvider.CONTENT_URI__ALL_DATA, null, null);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// reload all lists
				setupListViewChecksFinished = null;
				setupListViewEventsFinished = null;
				setupListViewProblemsFinished = null;
				setupListViewScreensFinished = null;

				setCurrentView(getCurrentView());
				hideLoading();
			}
		}.execute();
	}

	@Override
	public AppView getCurrentView() {
		if (getTabHost().getCurrentTabTag().equals("tab_status")) {
			return AppView.PROBLEMS;
		} else if (getTabHost().getCurrentTabTag().equals("tab_events")) {
			return AppView.EVENTS;
		} else if (getTabHost().getCurrentTabTag().equals("tab_checks")) {
			return AppView.CHECKS;
		} else if (getTabHost().getCurrentTabTag().equals("tab_screens")) {
			return AppView.SCREENS;
		}
		return null;
	}

	/**
	 * only for unit test
	 * @return
	 */
	public Dialog getDlgEventDetails() {
		return dlgDetails;
	}

	/**
	 * hides loading-dlg, if the loading stack is empty
	 */
	public void hideLoading() {
		hideLoading(false);
	}

	/**
	 * @param force loading stack to 0
	 */
	public void hideLoading(boolean force) {
		if (force) loadingDlgStack = 1;
		if ((loadingDlgStack = Math.max(--loadingDlgStack, 0)) == 0) {
			support.hideLoading();
			// possibly open details dlg
			if (showDialogDetails) {
				showDialogDetails = false;
				showDialog(DIALOG_DETAILS);
			}
		}
	}

	private void loadEventDetailsFromTriggerId(long triggerid) {
		// event details - async
		showLoading();
		AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				hideLoading();

				if (cursor.moveToFirst()) {
					dlgDetails.findViewById(R.id.menuitem_acknowledge).setTag(cursor.getLong(cursor.getColumnIndex(EventData.COLUMN_EVENTID))); // für ack
				}
				detailsEventSupport.setData(dlgDetails, cursor);
			}
		};
		handler.startQuery(0, null, Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+triggerid+"/events"), null, null, null, null);
	}

	private void loadItemDetailsFromItemIdOrTriggerId(final Long triggerid, Long itemid) {
		showLoading();
		AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				hideLoading();
				if (cursor.moveToFirst()) {
					TextView tv = (TextView) dlgDetails.findViewById(R.id.textview_graph);
					tv.setText(detailsItemSupport.getItemText(cursor));

					dlgDetails.findViewById(R.id.btn_show_item_details).setVisibility(View.VISIBLE);
					dlgDetails.findViewById(R.id.btn_show_item_details).setTag(cursor.getLong(cursor.getColumnIndex(ItemData.COLUMN_ITEMID))); // fuer item details
				} else {
					showNoDataMessage(ItemData.TABLE_NAME);
				}

				if (triggerid != null) {
					// load trigger details, because now the dependent data were loaded
					// trigger details
					Cursor cur = getContentResolver().query(Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+triggerid), null, null, null, null);
					detailsTriggerSupport.setData(dlgDetails, cur);
				}
			}
		};
		Uri uri;
		if (triggerid == null) {
			uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_ITEMS, "/"+itemid);
		} else {
			uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_TRIGGERS, "/"+triggerid+"/items");
		}
		handler.startQuery(0, null, uri, null, null, null, null);
	}

	@Override
	public void onBackPressed() {
		// if we are in hieratical list, go back one level
		HieraticalHostListView hlv = null;
		if (getTabHost().getCurrentTabTag().equals("tab_status")) {
			hlv = (HieraticalHostListView) findViewById(R.id.hosts_status);
		} else if (getTabHost().getCurrentTabTag().equals("tab_checks")) {
			hlv = (HieraticalHostListView) findViewById(R.id.hosts_checks);
		}

		ViewFlipper vf_screens = (ViewFlipper) findViewById(R.id.view_switcher_screens);

		if (hlv != null && hlv.getDisplayedChild() > 0) {
			hlv.showPrevious();
		} else if (getTabHost().getCurrentTabTag().equals("tab_graphs")) {
			// if we are at graphs, jump to the previsous tab
			getTabHost().setCurrentTabByTag(prevTabTag != null ? prevTabTag : "tab_checks");
		} else if (getTabHost().getCurrentTabTag().equals("tab_screens") && vf_screens.getDisplayedChild() == 1) {
			// if we are at screens, jump back to the list
			vf_screens.showPrevious();
			// hide empty view for sure
			findViewById(R.id.listview_screens_empty).setVisibility(View.GONE);
			lastShowScreen_screen__id = -1;
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		contentSupport = new ContentFragmentSupport(this);
		detailsEventSupport = new DetailsEventFragmentSupport(this, this);
		detailsTriggerSupport = new DetailsTriggerFragmentSupport(this, this);
		detailsItemSupport = new DetailsItemFragmentSupport(this);
		support = new MainActivitySupport(this, this, this);

		setupTabs();

		// if so, restore graph
		if (savedInstanceState != null) {
			lastShowChart_units = savedInstanceState.getString("lastShowChart_units");
			if (lastShowChart_units != null) {
				lastShowChart_itemid = savedInstanceState.getLong("lastShowChart_itemid");
				lastShowChart_description = savedInstanceState.getString("lastShowChart_description");
				lastShowChart_graphText = savedInstanceState.getString("lastShowChart_graphText");
				showChart(lastShowChart_itemid, lastShowChart_units, lastShowChart_description, lastShowChart_graphText);
				prevTabTag = savedInstanceState.getString("prevTabTag");
			}
			lastShowScreen_screen__id = savedInstanceState.getLong("lastShowScreen_screenid", -1);
			if (lastShowScreen_screen__id > -1) {
				final ListView listScreens = (ListView) findViewById(R.id.list_screens);
				listScreens.setVisibility(View.GONE);
				showScreenGraphs(lastShowScreen_screen__id);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_DETAILS) {
			return dlgDetails;
		} else if (id == MainActivitySupport.DIALOG_FAILURE_MESSAGE) {
			return support.getDlgFailureMessage();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuitem_preferences) {
			Intent intent = new Intent(getApplicationContext(), AppPreferenceActivity.class);
			startActivityForResult(intent, 0);
			return true;
		} else if (item.getItemId() == R.id.menuitem_clear) {
			clearLocalDatabase();
			return true;
		} else if (item.getItemId() == R.id.menuitem_buy_bonus) {
			support.menuItemBuyBonusSelected();
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		support.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// if the graph was not already restored
		if (!getTabHost().getCurrentTabTag().equals("tab_graphs")) {
			support.onCreate(savedInstanceState);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean b = super.onPrepareOptionsMenu(menu);
		support.onPrepareOptionsMenu(menu);
		return b;
	}

	@Override
	protected void onResume() {
		super.onResume();
		support.onResume();

		// workaround for a strange bug: if you go to second level in the HieraticalHostListView,
		// then open the Preferences-Activity and close it again, the 2 lists will show overlapped.
		HieraticalHostListView hlv = (HieraticalHostListView) findViewById(R.id.hosts_status);
		if (hlv != null) {
			hlv.setDisplayedChild(0);
		}
		hlv = (HieraticalHostListView) findViewById(R.id.hosts_checks);
		if (hlv != null) {
			hlv.setDisplayedChild(0);
		}
		ViewFlipper vf_screens = (ViewFlipper) findViewById(R.id.view_switcher_screens);
		if (vf_screens != null) {
			vf_screens.setDisplayedChild(0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		support.onSaveInstanceState(outState);

		// if applicable, store the graph, so that after
		// a orientation change the graph could be restored
		if (getTabHost().getCurrentTabTag().equals("tab_graphs") && lastShowChart_itemid != null) {
			outState.putLong("lastShowChart_itemid", lastShowChart_itemid);
			outState.putString("lastShowChart_units", lastShowChart_units);
			outState.putString("lastShowChart_description", lastShowChart_description);
			outState.putString("lastShowChart_graphText", lastShowChart_graphText);
			outState.putString("prevTabTag", prevTabTag);
		}
		// also for screen tab
		if (getTabHost().getCurrentTabTag().equals("tab_screens") && lastShowScreen_screen__id != -1) {
			outState.putLong("lastShowScreen_screenid", lastShowScreen_screen__id);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		support.onStop();
	}

	@Override
	public void setCurrentView(AppView view) {
		if (view == getCurrentView()) {
			// on change is not called
			if (view == AppView.PROBLEMS) {
				setupListViewProblems();
			} else if (view == AppView.EVENTS) {
				setupListViewEvents();
			} else if (view == AppView.CHECKS) {
				setupListViewChecks();
			} else if (view == AppView.SCREENS) {
				setupListViewScreens();
			}

			return;
		}
		if (view == AppView.PROBLEMS) {
			getTabHost().setCurrentTabByTag("tab_status");
		} else if (view == AppView.EVENTS) {
			getTabHost().setCurrentTabByTag("tab_events");
		} else if (view == AppView.SCREENS) {
			getTabHost().setCurrentTabByTag("tab_screens");
		} else { // Checks
			getTabHost().setCurrentTabByTag("tab_checks");
		}
	}

	private void setupDialogDetails() {
		if (dlgDetails == null) {
			dlgDetails = new Dialog(this);
			dlgDetails.setContentView(R.layout.dialog_details);
			dlgDetails.setTitle(R.string.details);
			dlgDetails.setCanceledOnTouchOutside(true);

			// show item details / history graph
			dlgDetails.findViewById(R.id.btn_show_item_details).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dlgDetails.dismiss();
					showItemDetails((Long) v.getTag());
				}
			});

			// acknowlegde event
			dlgDetails.findViewById(R.id.menuitem_acknowledge).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					new PromptDialog(MainActivitySmartphone.this, R.string.acknowledge, R.string.enter_comment) {
						@Override
						public boolean onOkClicked(String input) {
							acknowledgeEvent((Long) v.getTag(), input);
							return true;
						}
					}.show();
				}
			});
		}
		dlgDetails.findViewById(R.id.dlg_details_event_container).setVisibility(View.VISIBLE);
		dlgDetails.findViewById(R.id.dlg_details_item_container).setVisibility(View.VISIBLE);
		dlgDetails.findViewById(R.id.dlg_details_trigger_container).setVisibility(View.VISIBLE);

		showDialogDetails = true;
	}

	private void setupListViewChecks() {
		int now = (int) (new Date().getTime() / 1000);
		if (setupListViewChecksFinished != null && setupListViewChecksFinished>now-200) return;
		setupListViewChecksFinished = now;

		HieraticalHostListView hlv = (HieraticalHostListView) findViewById(R.id.hosts_checks);
		hlv.setEmptyView(findViewById(R.id.listview_checks_empty));
		hlv.loadData(false, this);
		hlv.setOnChildEntryClickListener(new OnChildEntryClickListener() {
			@Override
			public void onChildEntryClick(final HieraticalHostListView hlv, final long host__id) {
				Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_HOSTS.toString()+"/"+host__id+"/applications");
				AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
					@Override
					protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
						startManagingCursor(cursor);
						ExpandableListView lv = (ExpandableListView) findViewById(R.id.listview_checks);
						lv.setEmptyView(findViewById(R.id.listview_checks_empty));
						ResourceCursorTreeAdapter adapter = new ResourceCursorTreeAdapter(getApplicationContext(), cursor, R.layout.list_checks_group_entry, R.layout.list_checks_child_entry) {
							@Override
							protected void bindChildView(View view, Context arg1, Cursor cursor, boolean arg3) {
								contentSupport.setupListChecksItemsAdapterBindView(view, arg1, cursor);
							}

							@Override
							protected void bindGroupView(View view, Context arg1, Cursor cursor, boolean arg3) {
								TextView tv = (TextView) view.findViewById(R.id.checks_group_entry_name);
								tv.setText(cursor.getString(cursor.getColumnIndex(ApplicationData.COLUMN_NAME)));
							}

							@Override
							protected Cursor getChildrenCursor(Cursor arg0) {
								Uri uri = Uri.parse(
										ZabbixContentProvider.CONTENT_URI_HOSTS.toString()
										+"/"+host__id
										+"/applications"
										+"/"+arg0.getLong(arg0.getColumnIndex(ApplicationData.COLUMN_APPLICATIONID))
										+"/items");
								Cursor cursor = getContentResolver().query(uri, null, null, null, null);
								startManagingCursor(cursor);
								return cursor;
							}
						};
						lv.setAdapter(adapter);
						lv.setOnChildClickListener(new OnChildClickListener() {
							@Override
							public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
								contentSupport.onChecksItemClick(MainActivitySmartphone.this, v);
								return true;
							}
						});
						hlv.showNext();
						hideLoading();
					}
				};
				queryHandler.startQuery(0, null, uri, null, null, null, null);
				showLoading();
			}
		});
	}

	private void setupListViewEvents() {
		int now = (int) (new Date().getTime() / 1000);
		if (setupListViewEventsFinished != null && setupListViewEventsFinished>now-100) return;
		setupListViewEventsFinished = now;

		ListView lv = (ListView) findViewById(R.id.listview_events);
		lv.setEmptyView(findViewById(R.id.listview_events_empty));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long event__id) {
				contentSupport.onEventItemClick(MainActivitySmartphone.this, view);
			}
		});
		contentSupport.setupListEventsAdapter(lv);

		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				startManagingCursor(cursor);

				ListView lv = (ListView) findViewById(R.id.listview_events);
				ResourceCursorAdapter adapter = (ResourceCursorAdapter) lv.getAdapter();
				adapter.changeCursor(cursor);

				hideLoading();
			}
		};
		queryHandler.startQuery(0, null, ZabbixContentProvider.CONTENT_URI_EVENTS, null, null, null, null);
		showLoading();
	}

	public void setupListViewProblems() {
		int now = (int) (new Date().getTime() / 1000);
		if (setupListViewProblemsFinished != null && setupListViewProblemsFinished>now-100) return;
		setupListViewProblemsFinished = now;

		ListView lv = (ListView) findViewById(R.id.listview_triggers);
		lv.setEmptyView(findViewById(R.id.listview_triggers_empty));
		contentSupport.setupListProblemsAdapter(lv);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long trigger__id) {
				contentSupport.onProblemItemClick(MainActivitySmartphone.this, view);
			}
		});

		HieraticalHostListView hlv = (HieraticalHostListView) findViewById(R.id.hosts_status);
		hlv.setEmptyView(findViewById(R.id.listview_triggers_empty));
		hlv.loadData(true, this);
		hlv.setOnChildEntryClickListener(new OnChildEntryClickListener() {
			@Override
			public void onChildEntryClick(final HieraticalHostListView hlv, final long host__id) {
				Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_HOSTS.toString()+"/"+host__id+"/triggers");
				AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
					@Override
					protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
						startManagingCursor(cursor);
						ListView lv = (ListView) findViewById(R.id.listview_triggers);
						ResourceCursorAdapter adapter = (ResourceCursorAdapter) lv.getAdapter();
						adapter.changeCursor(cursor);

						hlv.showNext();
						hideLoading();
					}
				};
				queryHandler.startQuery(0, null, uri, null, null, null, null);
				showLoading();
			}
		});
	}

	private void setupListViewScreens() {
		int now = (int) (new Date().getTime() / 1000);
		if (setupListViewScreensFinished != null && setupListViewScreensFinished>now-2000) return;
		setupListViewScreensFinished = now;

		final ListView listScreens = (ListView) findViewById(R.id.list_screens);
		listScreens.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long screen__id) {
				showScreenGraphs(screen__id);
			}
		});
		listScreens.setEmptyView(findViewById(R.id.listview_screens_empty));
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				hideLoading();
				listScreens.setAdapter(new SimpleCursorAdapter(
						getApplicationContext(),
						android.R.layout.simple_list_item_single_choice,
						cursor,
						new String[] {ScreenData.COLUMN_NAME},
						new int[] {android.R.id.text1}
				));

				// bug workaround: after orientation change, the listitems were displayed in the graph
				ViewFlipper vf_screens = (ViewFlipper) findViewById(R.id.view_switcher_screens);
				vf_screens.showNext();
				vf_screens.showPrevious();
			}
		};
		showLoading();
		queryHandler.startQuery(0, null, ZabbixContentProvider.CONTENT_URI_SCREENS, null, null, null, null);
	}

	/**
	 * create tabs and set listeners
	 * data will not be loaded here
	 */
	private void setupTabs() {
		// create tabs
		TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
		TabSpec tab1 = tabHost.newTabSpec("tab_status")
			.setIndicator(getResources().getString(R.string.problems), getResources().getDrawable(android.R.drawable.ic_menu_info_details))
			.setContent(R.id.tab_status);
		tabHost.addTab(tab1);
		TabSpec tab2 = tabHost.newTabSpec("tab_events")
			.setIndicator(getResources().getString(R.string.events), getResources().getDrawable(android.R.drawable.ic_menu_more))
			.setContent(R.id.tab_events);
		tabHost.addTab(tab2);
		TabSpec tab3 = tabHost.newTabSpec("tab_checks")
			.setIndicator(getResources().getString(R.string.checks), getResources().getDrawable(android.R.drawable.ic_menu_agenda))
			.setContent(R.id.tab_checks);
		tabHost.addTab(tab3);
		TabSpec tab4 = tabHost.newTabSpec("tab_screens")
			.setIndicator(getResources().getString(R.string.screens), getResources().getDrawable(android.R.drawable.ic_menu_slideshow))
			.setContent(R.id.tab_screens);
		tabHost.addTab(tab4);
		TabSpec tab5 = tabHost.newTabSpec("tab_graphs")
			.setIndicator(getResources().getString(R.string.item_details), getResources().getDrawable(android.R.drawable.ic_menu_slideshow))
			.setContent(R.id.tab_graphs);
		tabHost.addTab(tab5);

		// setup click listener
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals("tab_status")) {
					setupListViewProblems();
				} else if (tabId.equals("tab_events")) {
					setupListViewEvents();
				} else if (tabId.equals("tab_checks")) {
					setupListViewChecks();
				} else if (tabId.equals("tab_screens")) {
					setupListViewScreens();
				}
			}
		});
	}

	/**
	 * opens graphs for Item.
	 * Tab will change and history data will be loaded
	 *
	 * @param itemid
	 * @param units
	 * @param description
	 * @param graphText
	 */
	private void showChart(long itemid, final String units, final String description, String graphText) {
		// store graph data, so that the graph can be restored after an orientation change
		lastShowChart_itemid = itemid;
		lastShowChart_units = units;
		lastShowChart_description = description;
		lastShowChart_graphText = graphText;
		TextView textGraph = (TextView) findViewById(R.id.textview_graph);
		textGraph.setText(graphText);

		// store previous tab
		prevTabTag = getTabHost().getCurrentTabTag();
		// open graph tab
		getTabHost().setCurrentTabByTag("tab_graphs");
		showLoading();
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				detailsItemSupport.showGraph(cursor, description);
				hideLoading();
			}
		};
		Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_HISTORY_DETAILS.toString()+"/"+itemid);
		queryHandler.startQuery(0, null, uri, null, null, null, null);
	}

	@Override
	public void showDetailsFromEventAndTriggerId(long eventid, long triggerid) {
		// dialog details init
		setupDialogDetails();

		// event details
		Cursor cur = getContentResolver().query(Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_EVENTS, "/"+eventid), null, null, null, null);
		detailsEventSupport.setData(dlgDetails, cur);
		dlgDetails.findViewById(R.id.menuitem_acknowledge).setTag(eventid); // for ack

		// item details, asynchron + trigger details
		loadItemDetailsFromItemIdOrTriggerId(triggerid, null);
	}

	@Override
	public void showDetailsFromItemId(long itemid) {
		// dialog details init
		setupDialogDetails();
		showLoading(); // for trigger

		// item details
		loadItemDetailsFromItemIdOrTriggerId(null, itemid);

		// trigger details + events
		AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				detailsTriggerSupport.setData(dlgDetails, cursor);

				// now the events can be loaded
				if (cursor.moveToFirst()) {
					loadEventDetailsFromTriggerId(cursor.getLong(cursor.getColumnIndex(TriggerData.COLUMN_TRIGGERID)));
				} else {
					showNoDataMessage(EventData.TABLE_NAME);
				}
				hideLoading();
			}
		};
		handler.startQuery(0, null, Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_ITEMS, "/"+itemid+"/triggers"), null, null, null, null);
	}

	@Override
	public void showDetailsFromTriggerId(long triggerid) {
		// dialog details init
		setupDialogDetails();

		// event details - async
		loadEventDetailsFromTriggerId(triggerid);

		// item details - async + trigger details
		loadItemDetailsFromItemIdOrTriggerId(triggerid, null);
	}

	/**
	 * loads item details and opens the graph tab
	 * @param triggerid
	 */
	private void showItemDetails(long itemid) {
		showLoading();
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				hideLoading();
				if (cursor.moveToFirst()) {
					showChart(
							cursor.getLong(cursor.getColumnIndex(ItemData.COLUMN_ITEMID))
							, cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_UNITS))
							, cursor.getString(cursor.getColumnIndex(ItemData.COLUMN_DESCRIPTION))
							, detailsItemSupport.getItemText(cursor)
					);
				} else {
					Toast.makeText(MainActivitySmartphone.this, getResources().getString(R.string.no_items_to_display), Toast.LENGTH_SHORT).show();
				}
			}
		};
		Uri uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_ITEMS, "/"+itemid);
		queryHandler.startQuery(0, null, uri, null, null, null, null);
	}

	/**
	 * opens the loading-dlg and pushes the loading stack
	 */
	public void showLoading() {
		if (++loadingDlgStack > 0) {
			support.showLoading();
		}
	}

	@Override
	public void showNoDataMessage(String kind) {
		if (kind.equals(ItemData.TABLE_NAME)) {
			dlgDetails.findViewById(R.id.btn_show_item_details).setVisibility(View.GONE);
			dlgDetails.findViewById(R.id.dlg_details_item_container).setVisibility(View.GONE);
		} else if (kind.equals(TriggerData.TABLE_NAME)) {
			dlgDetails.findViewById(R.id.dlg_details_trigger_container).setVisibility(View.GONE);
		} else if (kind.equals(EventData.TABLE_NAME)) {
			dlgDetails.findViewById(R.id.menuitem_acknowledge).setVisibility(View.GONE);
			dlgDetails.findViewById(R.id.dlg_details_event_container).setVisibility(View.GONE);
		}
	}

	private void showScreenGraphs(long screen__id) {
		lastShowScreen_screen__id = screen__id;
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				hideLoading();
				ViewFlipper vf = (ViewFlipper) findViewById(R.id.view_switcher_screens);
				vf.showNext();
				contentSupport.showScreenGraphs(cursor, findViewById(R.id.listview_screens_empty), (LinearLayout) findViewById(R.id.layout_graphs));
			}
		};
		showLoading();
		Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_SCREENS.toString()+"/"+screen__id+"/graphs/historydetails");
		queryHandler.startQuery(0, null, uri, null, null, null, null);
	}

	/**
	 * sometimes it happends, that a null-cursor comes.
	 * We have to catch this, otherweise the app will fail.
	 * @param c
	 */
	@Override
	public void startManagingCursor(Cursor c) {
		if (c != null) {
			super.startManagingCursor(c);
		}
	}
}
