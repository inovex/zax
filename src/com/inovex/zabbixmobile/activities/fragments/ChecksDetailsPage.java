package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Application;

public class ChecksDetailsPage extends SherlockFragment {

	private Application mApplication;
	private String mTitle = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_applications_details, null);
		// if (savedInstanceState != null)
		// mEventId = savedInstanceState.getLong(ARG_EVENT_ID, -1);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO: on orientation change, mEvent is not set ->
		// NullPointerException
		StringBuilder sb = new StringBuilder();
		sb.append("Application: \n\n");
		sb.append("ID: " + mApplication.getId() + "\n");
		sb.append("name: " + mApplication.getName() + "\n");
		sb.append("host: " + mApplication.getHost().getName() + " (" + mApplication.getHost().getId() + ")\n");
		TextView text = (TextView) getView().findViewById(R.id.details_text);
		text.setText(sb.toString());
	}

	public void setApplication(Application app) {
		this.mApplication = app;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}
	
}
