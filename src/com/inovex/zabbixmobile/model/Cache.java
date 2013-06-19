package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cache")
public class Cache {

	public enum CacheDataType {
		EVENT(2 * 60), TRIGGER(2 * 60), HOST(2 * 24 * 60 * 60), ITEM(4 * 60);
		// TODO: add all data types

		private final long lifeTime;

		/**
		 * Creates a CacheDataType.
		 * 
		 * @param lifeTime
		 *            life time (in seconds!)
		 */
		private CacheDataType(long lifeTime) {
			this.lifeTime = lifeTime;
		}

		/**
		 * 
		 * @return life time (in seconds!)
		 */
		public long getLifeTime() {
			return lifeTime;
		}
	}

	@DatabaseField(id = true)
	private CacheDataType type;
	@DatabaseField
	private long expireTime;

	public Cache() {

	}

	public Cache(CacheDataType type) {
		this.type = type;
		this.expireTime = System.currentTimeMillis() + type.getLifeTime()
				* 1000;
	}

	public CacheDataType getType() {
		return type;
	}

	public void setType(CacheDataType type) {
		this.type = type;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

}
