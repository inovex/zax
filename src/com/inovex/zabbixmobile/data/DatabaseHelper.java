package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Application;
import com.inovex.zabbixmobile.model.ApplicationItemRelation;
import com.inovex.zabbixmobile.model.Cache;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Graph;
import com.inovex.zabbixmobile.model.GraphItem;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.HostHostGroupRelation;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Screen;
import com.inovex.zabbixmobile.model.ScreenItem;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerHostGroupRelation;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
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

	// TODO: handle SQLExceptions in this class!
	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "zabbixmobile.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;
	private static final String TAG = DatabaseHelper.class.getSimpleName();
	private DatabaseConnection mThreadConnection;

	private final Class<?>[] mTables = { Event.class, Trigger.class, Item.class,
			Host.class, HostGroup.class, Application.class,
			TriggerHostGroupRelation.class, HostHostGroupRelation.class,
			ApplicationItemRelation.class, HistoryDetail.class, Screen.class,
			ScreenItem.class, Graph.class, GraphItem.class, Cache.class };

	/**
	 * Pass-through constructor to be used by subclasses (specifically
	 * {@link MockDatabaseHelper}).
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

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			for (Class<?> table : mTables)
				TableUtils.createTable(connectionSource, table);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			for (Class<?> table : mTables)
				TableUtils.dropTable(connectionSource, table, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Queries all events with the given severity and host group from the
	 * database.
	 * 
	 * @param severity
	 * @param hostGroupId
	 * @return list of events with a matching severity and host group
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
	 * Queries all problems with a given severity and host group from the
	 * database.
	 * 
	 * @param severity
	 * @param hostGroupId
	 * @return list of events with a matching severity and host group
	 * @throws SQLException
	 */
	public List<Trigger> getProblemsBySeverityAndHostGroupId(
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

	/**
	 * Queries all host groups from the database.
	 * 
	 * @return list of all host groups
	 * @throws SQLException
	 */
	public List<HostGroup> getHostGroups() throws SQLException {
		Dao<HostGroup, Long> hostGroupDao = getDao(HostGroup.class);
		return hostGroupDao.queryForAll();
	}

	/**
	 * Queries all hosts from the database.
	 * 
	 * @return list of all hosts
	 * @throws SQLException
	 */
	public List<Host> getHosts() throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		return hostDao.queryForAll();
	}

	/**
	 * Queries all hosts in a specified group from the database.
	 * 
	 * @param hostGroupId
	 *            ID of the host group
	 * @return list of hosts in the specified group
	 * @throws SQLException
	 */
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
	 * Queries a hosts by its ID.
	 * 
	 * This method is synchronized on the {@link Dao} for {@link Host} to ensure
	 * that hosts are actually present in the database (see
	 * {@link ZabbixRemoteAPI#importHostsAndGroups()}).
	 * 
	 * @param hostId
	 *            ID of the host group
	 * @return hosts with the given ID
	 * @throws SQLException
	 */
	public Host getHostById(long hostId) throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		synchronized (hostDao) {
			return hostDao.queryForId(hostId);
		}
	}

	/**
	 * Returns an event given its ID.
	 * 
	 * @param id
	 *            ID of the queried event
	 * @return the corresponding event
	 * @throws SQLException
	 */
	public Event getEventById(long id) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		return eventDao.queryForId(id);
	}

	/**
	 * Returns a trigger given its ID.
	 * 
	 * @param id
	 *            ID of the queried trigger
	 * @return the corresponding trigger
	 * @throws SQLException
	 */
	public Trigger getTriggerById(long id) throws SQLException {
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		return triggerDao.queryForId(id);
	}

	/**
	 * Queries all applications from the database.
	 * 
	 * @return list of all applications
	 * @throws SQLException
	 */
	public List<Application> getApplications() throws SQLException {
		Dao<Application, Long> appDao = getDao(Application.class);
		return appDao.queryForAll();
	}

	/**
	 * Queries all applications for a specified host from the database.
	 * 
	 * @param host
	 * 
	 * @return list of applications
	 * @throws SQLException
	 */
	public List<Application> getApplicationsByHost(Host host)
			throws SQLException {
		Dao<Application, Long> appDao = getDao(Application.class);
		return appDao.queryForEq(Application.COLUMN_HOSTID, host);
	}

	/**
	 * Queries all applications for a specified host ID from the database.
	 * 
	 * @param hostId
	 * 
	 * @return list of applications
	 * @throws SQLException
	 */
	public List<Application> getApplicationsByHostId(long hostId)
			throws SQLException {
		Dao<Application, Long> appDao = getDao(Application.class);
		return appDao.queryForEq(Application.COLUMN_HOSTID, hostId);
	}

	/**
	 * Queries the application with a given ID from the database.
	 * 
	 * @param id
	 * 
	 * @return application, or null if there is no application with this ID
	 * @throws SQLException
	 */
	public Application getApplicationById(long id) throws SQLException {
		Dao<Application, Long> appDao = getDao(Application.class);
		return appDao.queryForId(id);
	}

	/**
	 * Queries all items for a specified application from the database.
	 * 
	 * @param applicationId
	 * 
	 * @return list of items
	 * @throws SQLException
	 */
	public List<Item> getItemsByApplicationId(long applicationId)
			throws SQLException {
		Dao<Item, Long> itemDao = getDao(Item.class);
		Dao<ApplicationItemRelation, Long> relationDao = getDao(ApplicationItemRelation.class);
		Dao<Application, Long> applicationDao = getDao(Application.class);

		QueryBuilder<Item, Long> itemQuery = itemDao.queryBuilder();
		QueryBuilder<ApplicationItemRelation, Long> relationQuery = relationDao
				.queryBuilder();
		QueryBuilder<Application, Long> applicationQuery = applicationDao
				.queryBuilder();

		applicationQuery.where().eq(Application.COLUMN_APPLICATIONID,
				applicationId);
		relationQuery.join(applicationQuery);

		itemQuery.join(relationQuery);
		return itemQuery.query();
	}

	/**
	 * Queries all history details for a specified item from the database.
	 * 
	 * @param itemId
	 * 
	 * @return list of history details
	 * @throws SQLException
	 */
	public List<HistoryDetail> getHistoryDetailsByItemId(long itemId)
			throws SQLException {
		Dao<HistoryDetail, Long> historyDao = getDao(HistoryDetail.class);
		QueryBuilder<HistoryDetail, Long> query = historyDao.queryBuilder();
		query.where().eq(HistoryDetail.COLUMN_ITEMID, itemId);
		query.orderBy(HistoryDetail.COLUMN_CLOCK, true);
		return query.query();
	}
	
	/**
	 * Queries all history details for a specified item from the database.
	 * 
	 * @param itemId
	 * 
	 * @return list of history details
	 * @throws SQLException
	 */
	public long getNewestHistoryDetailsClockByItemId(long itemId)
			throws SQLException {
		Dao<HistoryDetail, Long> historyDao = getDao(HistoryDetail.class);
		QueryBuilder<HistoryDetail, Long> query = historyDao.queryBuilder();
		query.where().eq(HistoryDetail.COLUMN_ITEMID, itemId);
		query.orderBy(HistoryDetail.COLUMN_CLOCK, false);
		HistoryDetail newest = query.queryForFirst();
		if(newest != null)
			return newest.getClock();
		else
			return 0;
	}

	/**
	 * Queries all screens from the database.
	 * 
	 * 
	 * @return list of screens
	 * @throws SQLException
	 */
	public List<Screen> getScreens() throws SQLException {
		Dao<Screen, Long> screenDao = getDao(Screen.class);
		return screenDao.queryForAll();
	}

	/**
	 * Queries all graph IDs for a particular screen from the database.
	 * 
	 * @param screen
	 * @return list of graph IDs
	 * @throws SQLException
	 */
	public Set<Long> getGraphIdsByScreen(Screen screen) throws SQLException {
		Dao<ScreenItem, Long> screenItemDao = getDao(ScreenItem.class);
		List<ScreenItem> screenItems = screenItemDao.queryForEq(
				ScreenItem.COLUMN_SCREENID, screen.getId());

		HashSet<Long> graphIds = new HashSet<Long>();
		for (ScreenItem s : screenItems) {
			graphIds.add(s.getResourceId());
		}
		return graphIds;
	}

	/**
	 * Queries all graphs for a particular screen from the database.
	 * 
	 * @param screen
	 * @return graphs
	 * @throws SQLException
	 */
	public Collection<Graph> getGraphsByScreen(Screen screen)
			throws SQLException {
		Set<Long> graphIds = getGraphIdsByScreen(screen);

		Dao<Graph, Long> graphsDao = getDao(Graph.class);
		QueryBuilder<Graph, Long> graphsQuery = graphsDao.queryBuilder();
		graphsQuery.where().in(Graph.COLUMN_GRAPHID, graphIds);
		return graphsQuery.query();
	}

	/**
	 * Queries all graph items for a particular graph from the database.
	 * 
	 * @param graph
	 * @return graph items
	 * @throws SQLException
	 */
	public Collection<GraphItem> getGraphItemsByGraph(Graph graph)
			throws SQLException {
		Dao<GraphItem, Long> graphItemDao = getDao(GraphItem.class);

		return graphItemDao.queryForEq(GraphItem.COLUMN_GRAPHID, graph.getId());
	}

	/**
	 * Inserts events into the database.
	 * 
	 * @param events
	 *            collection of events to be inserted
	 * @throws SQLException
	 */
	public void insertEvents(Collection<Event> events) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
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

	/**
	 * Inserts triggers into the database.
	 * 
	 * @param triggers
	 *            collection of triggers to be inserted
	 * @throws SQLException
	 */
	public void insertTriggers(Collection<Trigger> triggers)
			throws SQLException {
		Dao<Trigger, Long> triggerDao = getDao(Trigger.class);
		DatabaseConnection conn = triggerDao.startThreadConnection();
		Savepoint savePoint = null;
		try {
			conn.setSavePoint(null);
			for (Trigger e : triggers) {
				triggerDao.createOrUpdate(e);
			}
		} finally {
			// commit at the end
			conn.commit(savePoint);
			triggerDao.endThreadConnection(conn);
		}

	}

	/**
	 * Inserts hosts into the database.
	 * 
	 * @param hosts
	 *            collection of hosts to be inserted
	 * @throws SQLException
	 */
	public void insertHosts(List<Host> hosts) throws SQLException {
		Dao<Host, Long> hostDao = getDao(Host.class);
		mThreadConnection = hostDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Host host : hosts) {
				hostDao.createIfNotExists(host);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			hostDao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts host groups into the database.
	 * 
	 * @param hostGroups
	 *            collection of host groups to be inserted
	 * @throws SQLException
	 */
	public void insertHostGroups(ArrayList<HostGroup> hostGroups)
			throws SQLException {
		Dao<HostGroup, Long> hostGroupDao = getDao(HostGroup.class);
		mThreadConnection = hostGroupDao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (HostGroup group : hostGroups) {
				hostGroupDao.createIfNotExists(group);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			hostGroupDao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts applications into the database.
	 * 
	 * @param applications
	 *            collection of applications to be inserted
	 * @throws SQLException
	 */
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

	/**
	 * Inserts trigger to host group relations into the database.
	 * 
	 * @param triggerHostGroupCollection
	 *            collection of relations to be inserted
	 * @throws SQLException
	 */
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

	/**
	 * Inserts host to host group relations into the database.
	 * 
	 * @param hostHostGroupCollection
	 *            collection of relations to be inserted
	 * @throws SQLException
	 */
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

	/**
	 * Inserts application to item relations into the database.
	 * 
	 * @param applicationItemRelations
	 *            collection of relations to be inserted
	 * @throws SQLException
	 */
	public void insertApplicationItemRelations(
			List<ApplicationItemRelation> applicationItemRelations)
			throws SQLException {
		Dao<ApplicationItemRelation, Long> dao = getDao(ApplicationItemRelation.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (ApplicationItemRelation relation : applicationItemRelations) {
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

	/**
	 * Inserts items into the database.
	 * 
	 * @param itemCollection
	 *            collection of items to be inserted
	 * @throws SQLException
	 */
	public void insertItems(List<Item> itemCollection) throws SQLException {
		Dao<Item, Long> dao = getDao(Item.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Item item : itemCollection) {
				dao.createIfNotExists(item);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts history details into the database.
	 * 
	 * @param historyDetailsCollection
	 *            collection of history details to be inserted
	 * @throws SQLException
	 */
	public void insertHistoryDetails(
			List<HistoryDetail> historyDetailsCollection) throws SQLException {
		Dao<HistoryDetail, Long> dao = getDao(HistoryDetail.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (HistoryDetail historyDetail : historyDetailsCollection) {
				dao.createOrUpdate(historyDetail);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts screens into the database.
	 * 
	 * @param screenCollection
	 *            collection of screens to be inserted
	 * @throws SQLException
	 */
	public void insertScreens(List<Screen> screenCollection)
			throws SQLException {
		Dao<Screen, Long> dao = getDao(Screen.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Screen screen : screenCollection) {
				dao.createOrUpdate(screen);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts screen items into the database.
	 * 
	 * @param screenItemsCollection
	 *            collection of screens to be inserted
	 * @throws SQLException
	 */
	public void insertScreenItems(List<ScreenItem> screenItemsCollection)
			throws SQLException {
		Dao<ScreenItem, Long> dao = getDao(ScreenItem.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (ScreenItem screen : screenItemsCollection) {
				dao.createOrUpdate(screen);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts graphs into the database.
	 * 
	 * @param graphCollection
	 *            collection of graphs to be inserted
	 * @throws SQLException
	 */
	public void insertGraphs(List<Graph> graphCollection) throws SQLException {
		Dao<Graph, Long> dao = getDao(Graph.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (Graph graph : graphCollection) {
				dao.createOrUpdate(graph);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Inserts graph items into the database.
	 * 
	 * @param graphItemsCollection
	 *            collection of graph items to be inserted
	 * @throws SQLException
	 */
	public void insertGraphItems(List<GraphItem> graphItemsCollection)
			throws SQLException {
		Dao<GraphItem, Long> dao = getDao(GraphItem.class);
		mThreadConnection = dao.startThreadConnection();
		Savepoint savePoint = null;
		try {

			for (GraphItem screen : graphItemsCollection) {
				dao.createOrUpdate(screen);
			}

		} finally {
			// commit at the end
			savePoint = mThreadConnection.setSavePoint(null);
			mThreadConnection.commit(savePoint);
			dao.endThreadConnection(mThreadConnection);
		}
	}

	/**
	 * Sets an event to acknowledged.
	 * 
	 * @param event
	 *            the event
	 * @throws SQLException
	 */
	public void acknowledgeEvent(Event event) throws SQLException {
		Dao<Event, Long> eventDao = getDao(Event.class);
		event.setAcknowledged(true);
		eventDao.update(event);
	}

	/**
	 * Clears all data of a certain type from the database.
	 * 
	 * @param c
	 *            class of the type
	 * @throws SQLException
	 */
	private <T> void clearTable(Class<T> c) throws SQLException {
		Dao<T, Long> dao = getDao(c);
		dao.deleteBuilder().delete();
	}

	/**
	 * Clears the entire database.
	 * 
	 * @throws SQLException
	 */
	public void clearAllData() throws SQLException {
		for (Class<?> table : mTables) {
			clearTable(table);
		}
	}

	/**
	 * Removes all events from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearEvents() throws SQLException {
		clearTable(Event.class);
	}

	/**
	 * Removes all triggers from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearTriggers() throws SQLException {
		clearTable(Trigger.class);
	}

	/**
	 * Removes all hosts from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearHosts() throws SQLException {
		clearTable(Host.class);
	}

	/**
	 * Removes all host groups from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearHostGroups() throws SQLException {
		clearTable(HostGroup.class);
	}

	/**
	 * Removes all items from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearItems() throws SQLException {
		clearTable(Item.class);
	}

	/**
	 * Removes all screens and screen items from the database.
	 * 
	 * @throws SQLException
	 */
	public void clearScreens() throws SQLException {
		clearTable(Screen.class);
		clearTable(ScreenItem.class);
	}

	/**
	 * Removes all applications for a given host.
	 * 
	 * @param hostId
	 *            the host ID
	 */
	public void deleteItemsByHostId(Long hostId) {
		Log.d(TAG, "deleting items for host ID " + hostId);
		try {
			// delete application item relations (they will be rebuilt by the
			// next items import)
			Dao<ApplicationItemRelation, Long> relationDao = getDao(ApplicationItemRelation.class);
			DeleteBuilder<ApplicationItemRelation, Long> relationDeleteBuilder = relationDao
					.deleteBuilder();
			relationDeleteBuilder.where().eq(
					ApplicationItemRelation.COLUMN_HOSTID, hostId);
			relationDeleteBuilder.delete();
			// delete items
			Dao<Item, Long> itemDao = getDao(Item.class);
			DeleteBuilder<Item, Long> itemDeleteBuilder = itemDao
					.deleteBuilder();
			itemDeleteBuilder.where().eq(Item.COLUMN_HOSTID, hostId);
			itemDeleteBuilder.delete();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Removes all applications for a given host.
	 * 
	 * @param hostId
	 *            the host ID
	 */
	public void deleteApplicationsByHostId(Long hostId) {
		Log.d(TAG, "deleting applications for host ID " + hostId);
		try {
			// delete applications
			Dao<Application, Long> appDao = getDao(Application.class);
			DeleteBuilder<Application, Long> deleteBuilder = appDao
					.deleteBuilder();
			deleteBuilder.where().eq(Application.COLUMN_HOSTID, hostId);
			deleteBuilder.delete();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Deletes history details which are older than the specified time.
	 * 
	 * @param threshold
	 *            all items with a clock smaller than this threshold will be
	 *            deleted
	 * @throws SQLException
	 */
	public void deleteOldHistoryDetailsByItemId(long itemId, long threshold)
			throws SQLException {
		Dao<HistoryDetail, Long> historyDao = getDao(HistoryDetail.class);
		DeleteBuilder<HistoryDetail, Long> deleteBuilder = historyDao
				.deleteBuilder();
		deleteBuilder.where().eq(HistoryDetail.COLUMN_ITEMID, itemId).and()
				.lt(HistoryDetail.COLUMN_CLOCK, threshold);
		int n = deleteBuilder.delete();
		Log.d(TAG, "deleted " + n + " history details.");
	}

	/**
	 * Deletes history details which are older than the specified time.
	 * 
	 * @param threshold
	 *            all items with a clock smaller than this threshold will be
	 *            deleted
	 * @throws SQLException
	 */
	public void deleteGraphsByIds(Set<Long> graphIds) {

		try {

			Dao<Graph, Long> graphDao = getDao(Graph.class);
			DeleteBuilder<Graph, Long> graphDeleteBuilder = graphDao
					.deleteBuilder();
			graphDeleteBuilder.where().in(Graph.COLUMN_GRAPHID, graphIds);
			graphDeleteBuilder.delete();

			Dao<GraphItem, Long> graphItemDao = getDao(GraphItem.class);
			DeleteBuilder<GraphItem, Long> graphItemDeleteBuilder = graphItemDao
					.deleteBuilder();
			graphItemDeleteBuilder.where().in(GraphItem.COLUMN_GRAPHID,
					graphIds);
			graphItemDeleteBuilder.delete();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: test

	}

	/**
	 * Marks a data type cached for a certain amount of time.
	 * 
	 * @param type
	 *            the data type
	 * @param itemId
	 *            ID of the item which has been cached or null if an entire
	 *            table has been cached
	 * @throws SQLException
	 */
	public void setCached(CacheDataType type, Long itemId) {
		if (itemId == null)
			itemId = (long) Cache.DEFAULT_ID;
		try {
			Dao<Cache, CacheDataType> cacheDao = getDao(Cache.class);
			cacheDao.createOrUpdate(new Cache(type, itemId));
		} catch (SQLException e) {
			// As caching is only a performance optimization, there is nothing
			// more to do here. If something is wrong with the database, caching
			// will simply not be used.
			Log.d(TAG, "Could not set " + type + "(id: " + itemId
					+ ") cached. Exception: " + e.getMessage());
		}
	}

	/**
	 * Checks whether a data type is cached in the local SQLite database.
	 * 
	 * @param type
	 *            the data type
	 * @param itemId
	 *            ID of the item to be checked or null, if an entire table shall
	 *            be checked
	 * @return true, if a cache entry for this data type exists and is still
	 *         up-to-date; false, otherwise
	 * @throws SQLException
	 */
	public boolean isCached(CacheDataType type, Long itemId) {
		Dao<Cache, CacheDataType> cacheDao;
		if (itemId == null)
			itemId = (long) Cache.DEFAULT_ID;
		try {
			cacheDao = getDao(Cache.class);

			Cache c;
			QueryBuilder<Cache, CacheDataType> query = cacheDao.queryBuilder();
			query.where().eq(Cache.COLUMN_TYPE, type).and()
					.eq(Cache.COLUMN_ITEM_ID, itemId);
			c = query.queryForFirst();
			if (c != null && c.getExpireTime() < System.currentTimeMillis()) {
				cacheDao.delete(c);
				c = null;
			}
			if (c == null) {
				Log.d(TAG, type + " (id: " + itemId + ") was not cached.");
				return false;
			}
			Log.d(TAG, type + " (id: " + itemId + ") was cached.");
			return true;
		} catch (SQLException e) {
			Log.d(TAG, "Could not check whether " + type + "(id: " + itemId
					+ ") is cached. Exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Queries a item by its ID.
	 * 
	 * This method is synchronized on the {@link Dao} for {@link Item} to ensure
	 * that item is actually present in the database (see
	 * {@link ZabbixRemoteAPI#importItemsByHostId(Long)}).
	 * 
	 * @param itemId
	 *            ID of the item
	 * @return item with the given ID
	 * @throws SQLException
	 */
	public Item getItemById(long itemId) throws SQLException {
		Dao<Item, Long> itemDao = getDao(Item.class);
		synchronized (itemDao) {
			return itemDao.queryForId(itemId);
		}
	}

	public Graph getGraphById(long graphId) throws SQLException {
		Dao<Graph, Long> graphDao = getDao(Graph.class);
		synchronized (graphDao) {
			return graphDao.queryForId(graphId);
		}
	}
}
