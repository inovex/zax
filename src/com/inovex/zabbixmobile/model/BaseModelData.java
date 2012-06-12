package com.inovex.zabbixmobile.model;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * base class for all models
 */
public abstract class BaseModelData {
	public static final String COLUMN__ID = "_id";

	protected final Map<String, Object> data = new HashMap<String, Object>();

	/**
	 * read property
	 * @param key name
	 * @return value
	 */
	public Object get(String key) {
		return data.get(key);
	}

	protected abstract String getTableName();

	public long insert(SQLiteDatabase zabbixLocalDB) {
		ContentValues values = new ContentValues(data.size());
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			if (entry.getValue() instanceof Integer)
				values.put(entry.getKey(), (Integer) entry.getValue());
			else if (entry.getValue() instanceof Long)
				values.put(entry.getKey(), (Long) entry.getValue());
			else if (entry.getValue() instanceof Float)
				values.put(entry.getKey(), (Float) entry.getValue());
			else if (entry.getValue() instanceof Double)
				values.put(entry.getKey(), (Double) entry.getValue());
			else if (entry.getValue() instanceof Boolean)
				values.put(entry.getKey(), (Boolean) entry.getValue());
			else if (entry.getValue() instanceof String)
				values.put(entry.getKey(), (String) entry.getValue());
			else if (entry.getValue() == null) {
				// ignore
			} else throw new IllegalStateException("unknown type "+entry);
		}
		Log.i("BaseModelData", "insert "+getTableName()+" // "+values);
		return zabbixLocalDB.insert(getTableName(), "", values);
	}

	/**
	 * does only an insert, if this entry does not exist. The entry
	 * will be searched by the column given by paramter uniqueColumnValue.
	 *
	 * @param zabbixLocalDB
	 * @param uniqueColumnValue
	 * @return
	 */
	public long insert(SQLiteDatabase zabbixLocalDB, String uniqueColumnValue) {
		Cursor cur = zabbixLocalDB.query(
				getTableName(), new String[] {uniqueColumnValue}, uniqueColumnValue+"=?", new String[]{data.get(uniqueColumnValue).toString()}, null, null, null);
		if (cur.getCount() == 0) {
			cur.close();
			return insert(zabbixLocalDB);
		} else {
			cur.close();
			return -1;
		}
	}

	/**
	 * set property
	 * @param key
	 * @param value
	 * @return reference to itself
	 */
	public BaseModelData set(String key, Object value) {
		data.put(key, value);
		return this;
	}

	@Override
	public String toString() {
		return getTableName()+"{"+data+"}";
	}
}
