package com.inovex.zabbixmobile.view;

import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.MainActivitySmartphone;
import com.inovex.zabbixmobile.activities.support.HostListFragmentSupport;

/**
 * 2 listviews: HostGroup, Host
 */
public class HieraticalHostListView extends ViewFlipper implements OnItemClickListener {
	public interface OnChildEntryClickListener {
		public void onChildEntryClick(HieraticalHostListView hlv, long itemid);
	}

	private HostListFragmentSupport hostListFragment;
	private boolean triggerFlag;
	private final ListView listViewHostGroups;
	private final ListView listViewHosts;
	private OnChildEntryClickListener onChildEntryClickListener;
	private MainActivitySmartphone mActivity;
	private List<Integer> priorityFilter;

	public HieraticalHostListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setInAnimation(context, android.R.anim.slide_in_left);
		setOutAnimation(context, android.R.anim.slide_out_right);

		listViewHostGroups = new ListView(context);
		listViewHostGroups.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addView(listViewHostGroups);

		listViewHosts = new ListView(context);
		listViewHosts.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addView(listViewHosts);
	}

	/**
	 * load data for the first listview (hostgroups)
	 * @param triggerFlag if true, "problem"-trigger filter will be set
	 * @param parent
	 */
	public void loadData(boolean triggerFlag, MainActivitySmartphone parent) {
		this.mActivity = parent;
		if (hostListFragment == null) {
			hostListFragment = new HostListFragmentSupport(parent);
			hostListFragment.setupLists(listViewHosts, listViewHostGroups);

			listViewHostGroups.setOnItemClickListener(this);
			listViewHosts.setOnItemClickListener(this);
		}

		this.triggerFlag = triggerFlag;
		setDisplayedChild(0);

		// hostGroups
		String[] selectionArgs = null;
		if (triggerFlag) {
			if (priorityFilter != null) {
				StringBuffer filterstr = new StringBuffer();
				for (int i : priorityFilter) {
					filterstr.append(i);
				}
				selectionArgs = new String[] {
						"triggerFlag"
						, filterstr.toString()
				};
			} else {
				selectionArgs = new String[] { "triggerFlag" };
			}
		}
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContext().getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				mActivity.hideLoading();
				mActivity.startManagingCursor(cursor);

				ResourceCursorAdapter adapter = (ResourceCursorAdapter) listViewHostGroups.getAdapter();
				adapter.changeCursor(cursor);
			}
		};
		queryHandler.startQuery(0, null, ZabbixContentProvider.CONTENT_URI_HOSTGROUPS, null, null, selectionArgs, null);
		parent.showLoading();
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int arg2, long id) {
		if (list == listViewHostGroups) {
			// show children => hosts
			Uri uri = Uri.parse(ZabbixContentProvider.CONTENT_URI_HOSTGROUPS.toString()+"/"+id+"/hosts");
			String[] selectionArgs = null;
			if (HieraticalHostListView.this.triggerFlag) {
				if (priorityFilter != null) {
					StringBuffer filterstr = new StringBuffer();
					for (int i : priorityFilter) {
						filterstr.append(i);
					}
					selectionArgs = new String[] {
							"triggerFlag"
							, filterstr.toString()
					};
				} else {
					selectionArgs = new String[] { "triggerFlag" };
				}
			}
			AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContext().getContentResolver()) {
				@Override
				protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
					mActivity.hideLoading();
					mActivity.startManagingCursor(cursor);
					ResourceCursorAdapter adapter = (ResourceCursorAdapter) listViewHosts.getAdapter();
					adapter.changeCursor(cursor);
					showNext();
				}
			};
			queryHandler.startQuery(0, null, uri, null, null, selectionArgs, null);
			mActivity.showLoading();
		} else if (list == listViewHosts) {
			if (onChildEntryClickListener != null) {
				onChildEntryClickListener.onChildEntryClick(HieraticalHostListView.this, id);
			} else {
				showNext();
			}
		}
	}

	/**
	 * set the view that should be displayed when the list is empty
	 * @param emptyView
	 */
	public void setEmptyView(View emptyView) {
		listViewHostGroups.setEmptyView(emptyView);
		listViewHosts.setEmptyView(emptyView);
	}

	/**
	 * callback if a host was selected
	 * @param onChildEntryClickListener
	 */
	public void setOnChildEntryClickListener(OnChildEntryClickListener onChildEntryClickListener) {
		this.onChildEntryClickListener = onChildEntryClickListener;
	}

	public void setPriorityFilter(List<Integer> priorityFilter) {
		this.priorityFilter = priorityFilter;
	}

	@Override
	public void showPrevious() {
		super.showPrevious();
		if (listViewHosts.getEmptyView() != null) {
			listViewHosts.getEmptyView().setVisibility(View.GONE);
		}
	}
}
