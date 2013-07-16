package com.inovex.zabbixmobile.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ChecksActivity;
import com.inovex.zabbixmobile.activities.EventsActivity;
import com.inovex.zabbixmobile.activities.ScreensActivity;

public class MainMenuFragment extends SherlockListFragment {

	private MenuListAdapter mListAdapter;

	protected class MenuListAdapter extends ArrayAdapter<String> {

		private boolean mEnabled = true;

		public MenuListAdapter(Context context, int resource, String[] objects) {
			super(context, resource, objects);
		}

		@Override
		public boolean isEnabled(int position) {
			return mEnabled;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return mEnabled;
		}

		// TODO: adjust view!
		public void setEnabled(boolean enabled) {
			mEnabled = enabled;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListAdapter = new MenuListAdapter(getSherlockActivity(),
				android.R.layout.simple_expandable_list_item_1, getResources()
						.getStringArray(R.array.activities));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setListAdapter(mListAdapter);

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				Intent intent = null;
				switch (position) {
				case 0:
					intent = new Intent(getSherlockActivity(),
							EventsActivity.class);
					break;
				case 1:
					intent = new Intent(getSherlockActivity(),
							ChecksActivity.class);
					break;
				case 2:
					intent = new Intent(getSherlockActivity(),
							ScreensActivity.class);
					break;
				default:
					return;
				}
				getSherlockActivity().startActivity(intent);
			}
		});
	}

}
