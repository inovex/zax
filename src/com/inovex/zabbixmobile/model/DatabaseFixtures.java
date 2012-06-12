package com.inovex.zabbixmobile.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

/**
 * for testing
 *
 */
public class DatabaseFixtures {
	public static final List<BaseModelData> allModels = new ArrayList<BaseModelData>();

	static {
		/**
		 * ApplicationItemRelation
		 */
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 100)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 200));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 100)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 201));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 101)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 201));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 101)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 202));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 102)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 203));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 0)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 204));
		allModels.add(new ApplicationItemRelationData()
			.set(ApplicationItemRelationData.COLUMN_APPLICATIONID, 0)
			.set(ApplicationItemRelationData.COLUMN_ITEMID, 205));
	}

	static {
		/**
		 * Application
		 */
		allModels.add(new ApplicationData()
			.set(ApplicationData.COLUMN_APPLICATIONID, 100)
			.set(ApplicationData.COLUMN_HOSTID, null) // global
			.set(ApplicationData.COLUMN_NAME, "CPU"));
		allModels.add(new ApplicationData()
			.set(ApplicationData.COLUMN_APPLICATIONID, 101)
			.set(ApplicationData.COLUMN_HOSTID, null) // global
			.set(ApplicationData.COLUMN_NAME, "Filesystem"));
		allModels.add(new ApplicationData()
			.set(ApplicationData.COLUMN_APPLICATIONID, 0) // 0 is always "other"
			.set(ApplicationData.COLUMN_HOSTID, null) // global
			.set(ApplicationData.COLUMN_NAME, "- other -"));
		allModels.add(new ApplicationData()
			.set(ApplicationData.COLUMN_APPLICATIONID, 102)
			.set(ApplicationData.COLUMN_HOSTID, 1002)
			.set(ApplicationData.COLUMN_NAME, "Tomcat Probleme"));
	}

	static {
		/**
		 * Event
		 */
		allModels.add(new EventData()
			.set(EventData.COLUMN_CLOCK, 1305619234)
			.set(EventData.COLUMN_EVENTID, 1633)
			.set(EventData.COLUMN_OBJECTID, 12802)
			.set(EventData.COLUMN_VALUE, 1)
			.set(EventData.COLUMN_ACK, 1)
			.set(EventData.COLUMN_HOSTS, "apache web server 1"));
		allModels.add(new EventData()
			.set(EventData.COLUMN_CLOCK, 1305500671)
			.set(EventData.COLUMN_EVENTID, 1634)
			.set(EventData.COLUMN_OBJECTID, 12802)
			.set(EventData.COLUMN_VALUE, 0)
			.set(EventData.COLUMN_ACK, 1)
			.set(EventData.COLUMN_HOSTS, "apache web server 1"));
		allModels.add(new EventData()
			.set(EventData.COLUMN_CLOCK, 1305500614)
			.set(EventData.COLUMN_EVENTID, 1635)
			.set(EventData.COLUMN_OBJECTID, 12801)
			.set(EventData.COLUMN_ACK, 0)
			.set(EventData.COLUMN_VALUE, 1)
			.set(EventData.COLUMN_HOSTS, "tomcat web server 3"));
		allModels.add(new EventData()
			.set(EventData.COLUMN_CLOCK, 1305302491)
			.set(EventData.COLUMN_EVENTID, 1636)
			.set(EventData.COLUMN_OBJECTID, 12801)
			.set(EventData.COLUMN_VALUE, 0)
			.set(EventData.COLUMN_HOSTS, "tomcat web server 3"));
		allModels.add(new EventData()
			.set(EventData.COLUMN_CLOCK, 1305303791)
			.set(EventData.COLUMN_EVENTID, 1637)
			.set(EventData.COLUMN_OBJECTID, 12806)
			.set(EventData.COLUMN_VALUE, 0)
			.set(EventData.COLUMN_HOSTS, "windows server domain controller"));
	}

	static {
		/**
		 * HistoryDetail
		 */
		allModels.add(new HistoryDetailData()
			.set(HistoryDetailData.COLUMN_CLOCK, (new Date().getTime()/1000)-120)
			.set(HistoryDetailData.COLUMN_VALUE, 274116000d)
			.set(HistoryDetailData.COLUMN_ITEMID, 202));
		allModels.add(new HistoryDetailData()
			.set(HistoryDetailData.COLUMN_CLOCK, (new Date().getTime()/1000)-60*60*1)
			.set(HistoryDetailData.COLUMN_VALUE, 274115000d)
			.set(HistoryDetailData.COLUMN_ITEMID, 202));
		allModels.add(new HistoryDetailData()
			.set(HistoryDetailData.COLUMN_CLOCK, (new Date().getTime()/1000)-60*90*1)
			.set(HistoryDetailData.COLUMN_VALUE, 274114000d)
			.set(HistoryDetailData.COLUMN_ITEMID, 202));
		allModels.add(new HistoryDetailData()
			.set(HistoryDetailData.COLUMN_CLOCK, (new Date().getTime()/1000)-60*60*3) // too old
			.set(HistoryDetailData.COLUMN_VALUE, 274113000d)
			.set(HistoryDetailData.COLUMN_ITEMID, 202));
	}

	static {
		/**
		 * HostGroup
		 */
		allModels.add(new HostGroupData()
			.set(HostGroupData.COLUMN_GROUPID, 1002)
			.set(HostGroupData.COLUMN_NAME, "server farm 1"));
		allModels.add(new HostGroupData()
			.set(HostGroupData.COLUMN_GROUPID, 1003)
			.set(HostGroupData.COLUMN_NAME, "linker flügel"));
	}

	static {
		/**
		 * Host
		 */
		allModels.add(new HostData()
			.set(HostData.COLUMN_GROUPID, 1002)
			.set(HostData.COLUMN_HOST, "apache web server 1")
			.set(HostData.COLUMN_HOSTID, 500));
		allModels.add(new HostData()
			.set(HostData.COLUMN_GROUPID, 1002)
			.set(HostData.COLUMN_HOST, "tomcat web server 3")
			.set(HostData.COLUMN_HOSTID, 501));
		allModels.add(new HostData()
			.set(HostData.COLUMN_GROUPID, 1003)
			.set(HostData.COLUMN_HOST, "mysql")
			.set(HostData.COLUMN_HOSTID, 502));
		allModels.add(new HostData()
			.set(HostData.COLUMN_GROUPID, 1003)
			.set(HostData.COLUMN_HOST, "windows server domain controller")
			.set(HostData.COLUMN_HOSTID, 503));
	}

	static {
		/**
		 * Item
		 */
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 200)
			.set(ItemData.COLUMN_DESCRIPTION, "Used disk space on /usr")
			.set(ItemData.COLUMN_HOSTID, 500)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217511)
			.set(ItemData.COLUMN_LASTVALUE, 9924640768l)
			.set(ItemData.COLUMN_UNITS, "B"));
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 201)
			.set(ItemData.COLUMN_DESCRIPTION, "Bufferes Memory")
			.set(ItemData.COLUMN_HOSTID, 501)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217512)
			.set(ItemData.COLUMN_LASTVALUE, 0)
			.set(ItemData.COLUMN_UNITS, "B"));
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 202)
			.set(ItemData.COLUMN_DESCRIPTION, "Cached Memory")
			.set(ItemData.COLUMN_HOSTID, 501)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217513)
			.set(ItemData.COLUMN_LASTVALUE, 0)
			.set(ItemData.COLUMN_UNITS, "B"));
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 203)
			.set(ItemData.COLUMN_DESCRIPTION, "Free Memory")
			.set(ItemData.COLUMN_HOSTID, 503)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217514)
			.set(ItemData.COLUMN_LASTVALUE, 274116608)
			.set(ItemData.COLUMN_UNITS, "B"));
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 204)
			.set(ItemData.COLUMN_DESCRIPTION, "Shared Memory")
			.set(ItemData.COLUMN_HOSTID, 503)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217515)
			.set(ItemData.COLUMN_LASTVALUE, 0)
			.set(ItemData.COLUMN_UNITS, "B"));
		allModels.add(new ItemData()
			.set(ItemData.COLUMN_ITEMID, 205)
			.set(ItemData.COLUMN_DESCRIPTION, "Total Memory")
			.set(ItemData.COLUMN_HOSTID, 504)
			.set(ItemData.COLUMN_LASTCLOCK, 1306217515)
			.set(ItemData.COLUMN_LASTVALUE, 536870912)
			.set(ItemData.COLUMN_UNITS, "B"));
	}

	static {
		/**
		 * Trigger
		 */
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "Low number of free inodes on {HOSTNAME} volume /home")
			.set(TriggerData.COLUMN_LASTCHANGE, 1305132793)
			.set(TriggerData.COLUMN_PRIORITY, 4)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 0)
			.set(TriggerData.COLUMN_TRIGGERID, 12801)
			.set(TriggerData.COLUMN_EXPRESSION, "xyxyx")
			.set(TriggerData.COLUMN_HOSTID, 500)
			.set(TriggerData.COLUMN_ITEMID, 200)
			.set(TriggerData.COLUMN_HOSTS, "apache web server 1"));
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "Low number of free inodes on {HOSTNAME} volume /opt")
			.set(TriggerData.COLUMN_LASTCHANGE, (new Date().getTime()/1000)-24*60*60)
			.set(TriggerData.COLUMN_PRIORITY, 4)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 1)
			.set(TriggerData.COLUMN_TRIGGERID, 12802)
			.set(TriggerData.COLUMN_ITEMID, 202)
			.set(TriggerData.COLUMN_HOSTID, 500)
			.set(TriggerData.COLUMN_HOSTS, "apache web server 1"));
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "Low number of free inodes on {HOSTNAME} volume /tmp")
			.set(TriggerData.COLUMN_LASTCHANGE, (new Date().getTime()/1000)-3*24*60*60)
			.set(TriggerData.COLUMN_PRIORITY, 4)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 1)
			.set(TriggerData.COLUMN_TRIGGERID, 12803)
			.set(TriggerData.COLUMN_HOSTID, 501)
			.set(TriggerData.COLUMN_HOSTS, "tomcat web server 3"));
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "Low free disk space on {HOSTNAME} volume /")
			.set(TriggerData.COLUMN_LASTCHANGE, 1305132805)
			.set(TriggerData.COLUMN_PRIORITY, 4)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 0)
			.set(TriggerData.COLUMN_TRIGGERID, 12804)
			.set(TriggerData.COLUMN_ITEMID, 201)
			.set(TriggerData.COLUMN_HOSTID, 502)
			.set(TriggerData.COLUMN_HOSTS, "mysql"));
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "WEB (HTTP) server is down on {HOSTNAME}")
			.set(TriggerData.COLUMN_LASTCHANGE, (new Date().getTime()/1000)-23*24*60*60)
			.set(TriggerData.COLUMN_PRIORITY, 3)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 1)
			.set(TriggerData.COLUMN_TRIGGERID, 12805)
			.set(TriggerData.COLUMN_HOSTID, 502)
			.set(TriggerData.COLUMN_HOSTS, "mysql"));
		allModels.add(new TriggerData()
			.set(TriggerData.COLUMN_DESCRIPTION, "IMAP server is down on {HOSTNAME}")
			.set(TriggerData.COLUMN_LASTCHANGE, (new Date().getTime()/1000)-5*24*60*60)
			.set(TriggerData.COLUMN_PRIORITY, 3)
			.set(TriggerData.COLUMN_STATUS, 0)
			.set(TriggerData.COLUMN_VALUE, 1)
			.set(TriggerData.COLUMN_TRIGGERID, 12806)
			.set(TriggerData.COLUMN_HOSTID, 503)
			.set(TriggerData.COLUMN_ITEMID, 204)
			.set(TriggerData.COLUMN_HOSTS, "windows server domain controller"));
	}

	static {
		/**
		 * Cache
		 */
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, null)
			.set(CacheData.COLUMN_KIND, HostGroupData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, null)
			.set(CacheData.COLUMN_KIND, HostData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "hostid=500")
			.set(CacheData.COLUMN_KIND, ItemData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "hostid=501")
			.set(CacheData.COLUMN_KIND, ItemData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "hostid=503")
			.set(CacheData.COLUMN_KIND, ItemData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, null)
			.set(CacheData.COLUMN_KIND, TriggerData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, null)
			.set(CacheData.COLUMN_KIND, EventData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, null)
			.set(CacheData.COLUMN_KIND, ApplicationData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "groupid=1002")
			.set(CacheData.COLUMN_KIND, HostData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "groupid=1003")
			.set(CacheData.COLUMN_KIND, HostData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "202")
			.set(CacheData.COLUMN_KIND, HistoryDetailData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "204")
			.set(CacheData.COLUMN_KIND, HistoryDetailData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "triggerid=12806")
			.set(CacheData.COLUMN_KIND, EventData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "triggerid=12806")
			.set(CacheData.COLUMN_KIND, TriggerData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "triggerid=12802")
			.set(CacheData.COLUMN_KIND, EventData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "itemid=204")
			.set(CacheData.COLUMN_KIND, TriggerData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "triggerid=12801")
			.set(CacheData.COLUMN_KIND, EventData.TABLE_NAME));
		allModels.add(new CacheData()
			.set(CacheData.COLUMN_EXPIRE_DATE, new Date().getTime()/1000 + 1000)
			.set(CacheData.COLUMN_FILTER, "triggerid=12801")
			.set(CacheData.COLUMN_KIND, TriggerData.TABLE_NAME));
	}

	public static void insert(SQLiteDatabase db) {
		for (BaseModelData mdl : allModels) {
			long id = mdl.insert(db);
			mdl.set(BaseModelData.COLUMN__ID, id);
		}
	}

	private DatabaseFixtures() {}
}
