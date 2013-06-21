package com.inovex.zabbixmobile.view;

import java.util.ArrayList;
import java.util.Collection;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.inovex.zabbixmobile.data.ZabbixDataService;

public abstract class BaseServiceAdapter<T> extends BaseAdapter {

	protected ZabbixDataService mZabbixDataService;
	protected ArrayList<T> mObjects;
	
	public BaseServiceAdapter(ZabbixDataService service) {
		this.mZabbixDataService = service;
		this.mObjects = new ArrayList<T>();
	}
	
	public void addAll(Collection<T> collection) {
		mObjects.addAll(collection);
	}
	
	public void clear() {
		mObjects.clear();
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	/**
	 * @return
	 */
	protected LayoutInflater getInflater() {
		return mZabbixDataService.getInflater();
	}

}
