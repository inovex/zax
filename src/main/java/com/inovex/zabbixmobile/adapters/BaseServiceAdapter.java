/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.adapters;

import java.util.Collection;
import java.util.TreeSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.inovex.zabbixmobile.data.ZabbixDataService;

/**
 * Base class for adapters maintained by a {@link ZabbixDataService}.
 * 
 * @param <T>
 *            class of the items in this adapter's data set
 */
public abstract class BaseServiceAdapter<T> extends BaseAdapter {

	protected ZabbixDataService mZabbixDataService;
	protected TreeSet<T> mObjects;
	private int mPosition;

	/**
	 * Creates an adapter.
	 * 
	 * @param service
	 *            the service maintaining this adapter.
	 */
	public BaseServiceAdapter(ZabbixDataService service) {
		this.mZabbixDataService = service;
		this.mObjects = new TreeSet<T>();
	}

	/**
	 * Wrapper for {@link TreeSet#addAll(Collection)}.
	 * 
	 * @param collection
	 */
	public void addAll(Collection<T> collection) {
		mObjects.addAll(collection);
	}

	/**
	 * Wrapper for {@link TreeSet#clear()}.
	 */
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
		if(mObjects.size() > position)
			return (T) mObjects.toArray()[position];
		if(mObjects.size() > 0)
			return mObjects.first();
		return null;
	}

	/**
	 * Retrieves the service's layout inflater.
	 * 
	 * @return
	 */
	protected LayoutInflater getInflater() {
		return mZabbixDataService.getInflater();
	}

	public int getCurrentPosition() {
		return mPosition;
	}

	public void setCurrentPosition(int position) {
		mPosition = position;
		Log.d("ListAdapter", "selected item: " + position);
	}

}
