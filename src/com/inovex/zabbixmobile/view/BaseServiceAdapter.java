package com.inovex.zabbixmobile.view;

import java.util.Collection;
import java.util.TreeSet;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.inovex.zabbixmobile.data.ZabbixDataService;

public abstract class BaseServiceAdapter<T> extends BaseAdapter {

	protected ZabbixDataService mZabbixDataService;
	protected TreeSet<T> mObjects;
	
	public BaseServiceAdapter(ZabbixDataService service) {
		this.mZabbixDataService = service;
		this.mObjects = new TreeSet<T>();
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

	@SuppressWarnings("unchecked")
	@Override
	public T getItem(int position) {
		return (T)mObjects.toArray()[position];
	}

	/**
	 * @return
	 */
	protected LayoutInflater getInflater() {
		return mZabbixDataService.getInflater();
	}

}
