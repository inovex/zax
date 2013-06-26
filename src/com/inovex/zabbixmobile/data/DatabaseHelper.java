package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Application;
import com.inovex.zabbixmobile.model.Cache;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.HostHostGroupRelation;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerHostGroupRelation;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "zabbixmobile.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = DatabaseHelper.class.getSimpleName();
	private DatabaseConnection mThreadConnection;
	private Savepoint mSavePoint;
	private int mTransactionSize;

	/**
	 * Pass-through constructor to be used by subclasses (specifically
	 * MockDatabaseHelper).
	 * 
	 * @param context
	 * @param databaseName
	 * @param factory
	 * @param databaseVersion
	 * @param configField
	 */
	public DatabaseHelper(Context context, String databaseName,
			CursorFactory factory, int databaseVersion, int configField) {
		super(context, databaseName, factory, databaseVersion, configField);
	}

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION,
				R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Event.class);
			TableUtils.createTable(connectionSource, Trigger.class);
			TableUtils.createTable(connectionSource, Item.class);
			TableUtils.createTable(connectionSource, Host.class);
			TableUtils.createTable(connectionSource, HostGroup.class);
			TableUtils.createTable(connectionSource, Application.class);
			TableUtils.createTable(connectionSource,
					TriggerHostGroupRelation.class);
			TableUtils.createTable(connectionSource,
					HostHostGroupRelation.class);
			TableUtils.createTable(connectionSource, Cache.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Event.class, true);
			TableUtils.dropTable(connectionSource, Trigger.class, true);
			TableUtils.dropTable(connectionSource, Item.class, true);
			TableUtils.dropTable(connectionSource, Host.class, true);
			TableUtils.dropTable(connectionSource, HostGroup.class, true);
			TableUtils.dropTable(connectionSource, Application.class, true);
			TableUtils.dropTable(connectionSource,
					TriggerHostGroupRelation.class, true);
			TableUtils.dropTable(connectionSource, HostHostGroupRelation.class,
					true);
			TableUtils.dropTable(connectionSource, Cache.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves all events with the given severity from the database.
	 * 
	 * @param severity
	 * @return list of events with a matching severity
	 * @throws SQLException
	 */
	public List<Event> getEventsBySeverityAndHostGroupId(
			TriggerSeverity severity, long hostGroupId) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		QueryBuilder<Event, Long> eventQuery = eventDao.queryBuilder();
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		QueryBuilder<Trigger, Long> triggerQuery = triggerDao.queryBuilder();

		// filter events by trigger severity
		if (!severity.equals(TriggerSeverity.ALL)) {
			triggerQuery.where().eq(Trigger.COLUMN_PRIORITY, severity);
		}

		// filter triggers by host group ID
		if (hostGroupId != HostGroup.GROUP_ID_ALL) {
			Dao<TriggerHostGroupRelation, Void> hostGroupDao = getDao(TriggerHostGroupRelation.class);
			QueryBuilder<TriggerHostGroupRelation, Void> hostGroupQuery = hostGroupDao
					.queryBuilder();
			hostGroupQuery.where().eq(TriggerHostGroupRelation.COLUMN_GROUPID,
					hostGroupId);
			triggerQuery.join(hostGroupQuery);
		}

		// eventQuery.orderBy(Event.COLUMN_CLOCK, false);

		eventQuery.join(triggerQuery);
		return eventQuery.query();
	}

	/**
	 * Retrieves all events with the given severity from the database.
	 * 
	 * @param severity
	 * @return list of events with a matching severity
	 * @throws SQLException
	 */
	public List<Trigger> getTriggersBySeverityAndHostGroupId(
			TriggerSeverity severity, long hostGroupId) throws SQLException {
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		QueryBuilder<Trigger, Long> triggerQuery = triggerDao.queryBuilder();

		// only triggers that have actually happened at some point in time are
		// interesting to us
		Where<Trigger, Long> where = triggerQuery.where();
		where.ne(Trigger.COLUMN_LASTCHANGE, 0);
		
		where.and();
		where.eq(Trigger.COLUMN_VALUE, Trigger.VALUE_PROBLEM);

		// filter events by trigger severity
		if (!severity.equals(TriggerSeverity.ALL)) {
			where.and();
			where.eq(Trigger.COLUMN_PRIORITY, severity);
		}

		// filter triggers by host group ID
		if (hostGroupId != HostGroup.GROUP_ID_ALL) {
			Dao<TriggerHostGroupRelation, Void> hostGroupDao = getDao(TriggerHostGroupRelation.class);
			QueryBuilder<TriggerHostGroupRelation, Void> hostGroupQuery = hostGroupDao
					.queryBuilder();
			hostGroupQuery.where().eq(TriggerHostGroupRelation.COLUMN_GROUPID,
					hostGroupId);
			triggerQuery.join(hostGroupQuery);
		}

		return triggerQuery.query();
	}

	public List<HostGroup> getHostGroups() throws SQLException {
		Dao<HostGroup, Long> hostGroupDao = getDao(HostGroup.class);
		return hostGroupDao.queryForAll();
	}

	public List<Host> getHosts() throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		return hostDao.queryForAll();
	}

	public List<Host> getHostsByHostGroup(long hostGroupId) throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		Dao<HostHostGroupRelation, Long> groupRelationDao = getDao(HostHostGroupRelation.class);
		QueryBuilder<Host, Long> hostQuery = hostDao.queryBuilder();

		if (hostGroupId != HostGroup.GROUP_ID_ALL) {
			QueryBuilder<HostHostGroupRelation, Long> groupQuery = groupRelationDao
					.queryBuilder();
			groupQuery.where().eq(HostHostGroupRelation.COLUMN_GROUPID,
					hostGroupId);
			hostQuery.join(groupQuery);
		}
		return hostQuery.query();
	}

	/**
	 * Returns an event given its ID.
	 * 
	 * @param id
	 *            ID of the desired event
	 * @return the corresponding event
	 * @throws SQLException
	 */
	public Event getEventById(long id) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		return eventDao.queryForId(id);
	}

	public void insertEvents(Collection<Event> events) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		Dao<Host, Long> hostDao = getDao(Host.class);
		mThreadConnection = eventDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Event e : events) {
				synchronized (this) {
					eventDao.createOrUpdate(e);
				}
				Trigger t = e.getTrigger();
				if (t != null) {
					triggerDao.createOrUpdate(t);
				}
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			eventDao.endThreadConnection(mThreadConnection);
		}

	}

	public void insertTriggers(Collection<Trigger> triggers)
			throws SQLException {
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		DatabaseConnection conn = triggerDao.startThreadConnection();
		Savepoint savePoint = null;
		try {
			conn.setSavePoint(null);
			int j = 0;
			for (Trigger e : triggers) {
				j++;
				triggerDao.createOrUpdate(e);
				if (j % 50 == 0) {
					conn.commit(savePoint);
					savePoint = conn.setSavePoint(null);
				}
			}
		} finally {
			// commit at the end
			conn.commit(savePoint);
			triggerDao.endThreadConnection(conn);
		}

	}

	public void insertHosts(List<Host> hosts) throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		mThreadConnection = hostDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Host host : hosts) {
				hostDao.createOrUpdate(host);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			hostDao.endThreadConnection(mThreadConnection);
		}
	}

	public void insertHostGroups(ArrayList<HostGroup> hostGroups)
			throws SQLException {
		Dao<HostGroup, Long> hostGroupDao = getDao(HostGroup.class);
		mThreadConnection = hostGroupDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (HostGroup group : hostGroups) {
				hostGroupDao.createOrUpdate(group);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			hostGroupDao.endThreadConnection(mThreadConnection);
		}
	}

	public void insertApplications(Collection<Application> applications)
			throws SQLException {
		Dao<Application, Long> appDao = getDao(Application.class);
		Dao<Host, Long> hostDao = getDao(Host.class);
		mThreadConnection = appDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Application a : applications) {
				synchronized (this) {
					appDao.createOrUpdate(a);
				}
				Host t = a.getHost();
				if (t != null) {
					hostDao.createOrUpdate(t);
				}
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			appDao.endThreadConnection(mThreadConnection);
		}

	}

	public void insertTriggerHostgroupRelations(
			List<TriggerHostGroupRelation> triggerHostGroupCollection)
			throws SQLException {
		Dao<TriggerHostGroupRelation, Void> dao = getDao(TriggerHostGroupRelation.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (TriggerHostGroupRelation relation : triggerHostGroupCollection) {
				try {
					dao.createIfNotExists(relation);
				} catch (SQLException e) {
					// this might throw an exception if the relation exists
					// already (however, with a different primary key) -> ignore
				}
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	public void insertHostHostgroupRelations(
			List<HostHostGroupRelation> hostHostGroupCollection)
			throws SQLException {
		Dao<HostHostGroupRelation, Void> dao = getDao(HostHostGroupRelation.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (HostHostGroupRelation relation : hostHostGroupCollection) {
				try {
					dao.createIfNotExists(relation);
				} catch (SQLException e) {
					// this might throw an exception if the relation exists
					// already (however, with a different primary key) -> ignore
				}
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	public void clearEvents() throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		eventDao.deleteBuilder().delete();
	}

	public void clearTriggers() throws SQLException {
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		triggerDao.deleteBuilder().delete();
	}

	public void clearHosts() throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		hostDao.deleteBuilder().delete();
	}

	public void clearHostGroups() throws SQLException {
		Dao<HostGroup, Long> hostGroupDao = getDao(HostGroup.class);
		hostGroupDao.deleteBuilder().delete();
	}

	/**
	 * Marks a data type cached for a certain amount of time.
	 * 
	 * @param type
	 *            the data type
	 * @throws SQLException
	 */
	public void setCached(CacheDataType type) throws SQLException {
		Dao<Cache, CacheDataType> cacheDao = getDao(Cache.class);
		cacheDao.createOrUpdate(new Cache(type));
	}

	/**
	 * Checks whether a data type is cached in the local SQLite database.
	 * 
	 * @param type
	 *            the data type
	 * @return true, if a cache entry for this data type exists and is still
	 *         up-to-date; false, otherwise
	 * @throws SQLException
	 */
	public boolean isCached(CacheDataType type) throws SQLException {
		Dao<Cache, CacheDataType> cacheDao = getDao(Cache.class);
		Cache c = cacheDao.queryForId(type);
		if (c == null || c.getExpireTime() < System.currentTimeMillis())
			return false;
		return true;
	}

}
