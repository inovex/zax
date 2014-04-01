package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersDetailsFragment extends BaseServiceConnectedFragment {

	private ZabbixServer mServer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.fragment_server_details, null);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		fillContent();
	}

	public void setServer(ZabbixServer server) {
		this.mServer = server;
		fillContent();
	}

	private void fillContent() {
		if (mServer != null && getView() != null) {
			((EditText) getView().findViewById(R.id.server_name))
					.setText(mServer.getName());
		}
	}

}
