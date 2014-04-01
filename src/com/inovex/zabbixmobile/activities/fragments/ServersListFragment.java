package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
<<<<<<< HEAD
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
=======
import android.view.View;
import android.widget.ListView;

>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.listeners.OnServerSelectedListener;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersListFragment extends BaseServiceConnectedListFragment {

	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;
	private OnServerSelectedListener mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (OnServerSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnServerSelectedListener.");
		}
	}
<<<<<<< HEAD

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.zabbix_servers_list_context, menu);
	}

=======
	
>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mActivity.onServerSelected(mServersListAdapter.getItem(position));
	}

	@Override
	protected void setupListAdapter() {
		mServersListAdapter = mZabbixDataService
				.getServersListManagementAdapter();
		setListAdapter(mServersListAdapter);
	}

<<<<<<< HEAD
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.item_change_name:
			changeNameDialog(mServersListAdapter.getItem(info.position));
			return true;
		case R.id.item_remove:
			deleteServerDialog(mServersListAdapter.getItem(info.position));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteServerDialog(final ZabbixServer item) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(R.string.remove);
		alert.setMessage("Are you sure to remove the server?");

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				mZabbixDataService.removeZabbixServer(item);
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	private void changeNameDialog(final ZabbixServer zabbixServer) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(R.string.change_name);
		alert.setMessage("Type the name of the server");

		// Set an EditText view to get user input
		final EditText input = new EditText(getActivity());
		input.setText(zabbixServer.getName());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value.length()>0) {
					zabbixServer.setName(value);
					mZabbixDataService.updateZabbixServer(zabbixServer);
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}
=======
>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
}
