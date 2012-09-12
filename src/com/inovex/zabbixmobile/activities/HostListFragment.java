package com.inovex.zabbixmobile.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.ToggleButton;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;
import com.inovex.zabbixmobile.activities.support.HostListFragmentSupport;
import com.inovex.zabbixmobile.model.ScreenData;

public class HostListFragment extends Fragment implements OnItemClickListener, LoaderCallbacks<Cursor> {
	private static final int LOADER_HOSTGROUPS = 1;
	private static final int LOADER_HOSTS = 2;
	private static final int LOADER_SCREENS = 3;

	private HostListFragmentSupport hostListSupport;
	private ListView listHosts;
	private ListView listHostGroups;
	private ListView listScreens;
	private AppView currentView;
	private Animation animationFragmentSlideOut;
	private Animation animationFragmentSlideIn;
	private Animation animationFragmentFadeIn;
	private int restoreHostGroupPos = -1;
	private int restoreHostPos = -1;
	private int restoreScreenPos = -1;
	private final List<Integer> problemsFilterPriority = new ArrayList<Integer>();

	public int getListHostCheckedItemPosition() {
		return listHosts.getCheckedItemPosition();
	}

	public long getListHostGroupCheckedItemId() {
		long[] ids = listHostGroups.getCheckedItemIds();
		if (ids != null && ids.length>0) {
			return ids[0];
		}
		return -1;
	}

	public int getListHostGroupCheckedItemPosition() {
		return listHostGroups.getCheckedItemPosition();
	}

	public int getListScreensCheckedItemPosition() {
		return listScreens.getCheckedItemPosition();
	}

	private void hideLoading() {
		getActivity().findViewById(R.id.fragment_host_list_loading).setVisibility(View.GONE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		hostListSupport = new HostListFragmentSupport(getActivity());

		listHostGroups = (ListView) getActivity().findViewById(R.id.list_hostgroups);
		listHostGroups.setOnItemClickListener(this);
		listHostGroups.setEmptyView(getActivity().findViewById(R.id.fragment_host_list_empty));

		listHosts = (ListView) getActivity().findViewById(R.id.list_hosts);
		listHosts.setOnItemClickListener(this);

		listScreens = (ListView) getActivity().findViewById(R.id.list_screens);
		listScreens.setOnItemClickListener(this);
		listScreens.setEmptyView(getActivity().findViewById(R.id.fragment_host_list_empty));
		listScreens.setAdapter(new SimpleCursorAdapter(
				getActivity(),
				android.R.layout.simple_list_item_single_choice,
				null,
				new String[] {ScreenData.COLUMN_NAME},
				new int[] {android.R.id.text1}
		));

		hostListSupport.setupLists(listHosts, listHostGroups);

		// toggle buttons problems filter
		OnCheckedChangeListener tplList = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onProblemsFilterChanged();
			}
		};
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_0)).setOnCheckedChangeListener(tplList);
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_1)).setOnCheckedChangeListener(tplList);
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_2)).setOnCheckedChangeListener(tplList);
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_3)).setOnCheckedChangeListener(tplList);
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_4)).setOnCheckedChangeListener(tplList);
		((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_5)).setOnCheckedChangeListener(tplList);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Uri uri;
		if (id == LOADER_HOSTGROUPS) {
			uri = ZabbixContentProvider.CONTENT_URI_HOSTGROUPS;
		} else if (id == LOADER_HOSTS) {
			uri = Uri.withAppendedPath(ZabbixContentProvider.CONTENT_URI_HOSTGROUPS, "/"+bundle.getLong("hostgroup__id")+"/hosts");
		} else { // LOADER_SCREENS
			uri = ZabbixContentProvider.CONTENT_URI_SCREENS;
		}

		// problem filter
		String[] selectionArgs = null;
		if (currentView==AppView.PROBLEMS) {
			if (!problemsFilterPriority.isEmpty()) {
				StringBuffer filterstr = new StringBuffer();
				for (int i : problemsFilterPriority) {
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

		return new CursorLoader(
				getActivity()
				, uri
				, null
				, null
				, selectionArgs
				, null
		);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_host_list, container);
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int arg2, long id) {
		if (list == listHosts) {
			// redirect to content fragment
			MainActivityTablet activity = (MainActivityTablet) getActivity();
			ContentFragment cont = activity.getContentFragment();
			cont.onItemSelected(id);
		} else if (list == listHostGroups) {
			// hosts for the hostgroup
			Bundle bundle = new Bundle(1);
			bundle.putLong("hostgroup__id", id);
			getLoaderManager().restartLoader(LOADER_HOSTS, bundle, this);

			if (listHosts.getVisibility() == View.GONE) {
				if (animationFragmentFadeIn == null) {
					animationFragmentFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
				}
				listHosts.setVisibility(View.VISIBLE);
				listHosts.startAnimation(animationFragmentFadeIn);
			}
		} else if (list == listScreens) {
			// redirect to content fragment
			MainActivityTablet activity = (MainActivityTablet) getActivity();
			ContentFragment cont = activity.getContentFragment();
			cont.onItemSelected(id);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		ListAdapter castAdapter = listHosts.getAdapter();
		if (castAdapter instanceof ResourceCursorAdapter) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) castAdapter;
			adapter.swapCursor(null);
		}

		if (loader.getId() == LOADER_HOSTGROUPS) {
			castAdapter = listHostGroups.getAdapter();
			if (castAdapter instanceof ResourceCursorAdapter) {
				ResourceCursorAdapter adapter = (ResourceCursorAdapter) castAdapter;
				adapter.swapCursor(null);
			}
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		hideLoading();

		if (loader.getId() == LOADER_HOSTGROUPS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listHostGroups.getAdapter();
			adapter.swapCursor(cursor);
			if (restoreHostGroupPos != -1) {
				listHostGroups.setItemChecked(restoreHostGroupPos, true);
				restoreHostGroupPos = -1;
			}
		} else if (loader.getId() == LOADER_HOSTS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listHosts.getAdapter();
			adapter.swapCursor(cursor);
			if (restoreHostPos != -1) {
				listHosts.setItemChecked(restoreHostPos, true);
				restoreHostPos = -1;
			}
		} else if (loader.getId() == LOADER_SCREENS) {
			ResourceCursorAdapter adapter = (ResourceCursorAdapter) listScreens.getAdapter();
			adapter.swapCursor(cursor);
			if (restoreScreenPos != -1) {
				listScreens.setItemChecked(restoreScreenPos, true);
				restoreScreenPos = -1;
			}
		}
	}

	private void onProblemsFilterChanged() {
		problemsFilterPriority.clear();
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_0)).isChecked()) {
			problemsFilterPriority.add(0);
		}
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_1)).isChecked()) {
			problemsFilterPriority.add(1);
		}
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_2)).isChecked()) {
			problemsFilterPriority.add(2);
		}
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_3)).isChecked()) {
			problemsFilterPriority.add(3);
		}
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_4)).isChecked()) {
			problemsFilterPriority.add(4);
		}
		if (((ToggleButton) getActivity().findViewById(R.id.tgl_trigger_filter_5)).isChecked()) {
			problemsFilterPriority.add(5);
		}
		showView(currentView);
		((MainActivityTablet) getActivity()).getContentFragment().showView(currentView);
	}

	public void restoreLastSelection(int hostGroupPos, long hostGroupId, int hostPos) {
		restoreHostGroupPos = hostGroupPos;
		restoreHostPos = hostPos;
		onItemClick(listHostGroups, null, 0, hostGroupId);
	}

	public void restoreLastSelectionScreens(int screenPos) {
		restoreScreenPos  = screenPos;
	}

	public void setLoadingProgress(int progress) {
		ProgressBar p = (ProgressBar) ((RelativeLayout) getActivity().findViewById(R.id.fragment_host_list_loading)).getChildAt(0);
		p.setProgress(progress);
	}

	private void showLoading() {
		setLoadingProgress(1);
		getActivity().findViewById(R.id.fragment_host_list_loading).setVisibility(View.VISIBLE);
	}

	public void showView(AppView view) {
		getLoaderManager().destroyLoader(LOADER_HOSTGROUPS);
		getLoaderManager().destroyLoader(LOADER_HOSTS);
		getLoaderManager().destroyLoader(LOADER_SCREENS);

		currentView = view;

		if (currentView == AppView.EVENTS) {
			// hide myself
			if (animationFragmentSlideOut == null) {
				animationFragmentSlideOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
				animationFragmentSlideOut.setInterpolator(new Interpolator() {
					@Override
					public float getInterpolation(float input) {
						return Math.abs(input -1f);
					}
				});
				animationFragmentSlideOut.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						getView().setVisibility(View.GONE);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					@Override
					public void onAnimationStart(Animation animation) {
					}
				});
			}
			getView().startAnimation(animationFragmentSlideOut);
			return;
		} else if (getView().getVisibility() == View.GONE) {
			// show myself
			if (animationFragmentSlideIn == null) {
				animationFragmentSlideIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
			}
			getView().setVisibility(View.VISIBLE);
			getView().startAnimation(animationFragmentSlideIn);
		}

		// problems - show filter
		getActivity().findViewById(R.id.problems_filter_severity).setVisibility(
				view == AppView.PROBLEMS?View.VISIBLE:View.GONE);

		// if there are no settings, we do nothing
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String url = prefs.getString("zabbix_url", "").trim();
		String user = prefs.getString("zabbix_username", "").trim();
		String pwd = prefs.getString("zabbix_password", "");
		if (url.length()==0 || user.length()==0 || pwd.length()==0) {
			return;
		}

		showLoading();

		// reset selection
		if (listHostGroups != null) {
			listHostGroups.setItemChecked(listHostGroups.getCheckedItemPosition(), false);
			listHosts.setItemChecked(listHosts.getCheckedItemPosition(), false);

			// hide list hosts
			listHosts.setVisibility(View.GONE);

			listHostGroups.setVisibility(view == AppView.SCREENS ? View.GONE : View.VISIBLE);
		}
		if (listScreens != null) {
			listScreens.setVisibility(view == AppView.SCREENS ? View.VISIBLE : View.GONE);
			listScreens.setItemChecked(listScreens.getCheckedItemPosition(), false);
		}

		// caution: loaderID must be different for screens or hostgroups
		getLoaderManager().restartLoader(
				view == AppView.SCREENS ? LOADER_SCREENS : LOADER_HOSTGROUPS
				, null, this);
	}
}
