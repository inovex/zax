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

package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cache")
public class Cache {

	public enum CacheDataType {
		HOSTGROUP(7 * 24 * 60 * 60), HOST(2 * 24 * 60 * 60), EVENT(2 * 60), TRIGGER(
				2 * 60), APPLICATION(2 * 24 * 60 * 60), ITEM(4 * 60), HISTORY_DETAILS(
				4 * 60), GRAPH(2 * 24 * 60 * 60), SCREEN(2 * 24 * 60 * 60);

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

	public static final int DEFAULT_ID = -1;

	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_ITEM_ID = "item_id";
	private static final String INDEX_CACHE = "cache_idx";

	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField(uniqueIndexName = INDEX_CACHE, columnName = COLUMN_TYPE)
	private CacheDataType type;
	@DatabaseField(uniqueIndexName = INDEX_CACHE, columnName = COLUMN_ITEM_ID, canBeNull = true)
	private Long itemId;
	@DatabaseField
	private long expireTime;

	public Cache() {

	}

	public Cache(CacheDataType type, Long itemId) {
		this.type = type;
		this.expireTime = System.currentTimeMillis() + type.getLifeTime()
				* 1000;
		this.itemId = itemId;
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
