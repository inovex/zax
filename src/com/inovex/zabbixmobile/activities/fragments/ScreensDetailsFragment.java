package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;

public class ScreensDetailsFragment extends SherlockFragment {
	
	private long mScreenId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_screen_details, container);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView tv = (TextView)getView().findViewById(R.id.screens_details_text);
		tv.setText(getResources().getString(R.string.screens) + " " + mScreenId);
	}

	public void setScreenId(long id) {
		this.mScreenId = id;
		if(getView() != null) {
			TextView tv = (TextView)getView().findViewById(R.id.screens_details_text);
			tv.setText(getResources().getString(R.string.screens) + " " + mScreenId);
		}
	}

}
