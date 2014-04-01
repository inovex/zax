package com.inovex.zabbixmobile.model;

<<<<<<< HEAD
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "zabbixservers")
public class ZabbixServer implements Comparable<ZabbixServer> {

	/** Screen ID */
	public static final String COLUMN_ZABBIXSERVERID = "zabbixserverid";
	@DatabaseField(columnName = COLUMN_ZABBIXSERVERID, generatedId = true)
	long id;

	/** Screen name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	String name;

	public ZabbixServer() {
	}

	public long getId() {
		return id;
=======
import java.io.Serializable;

public class ZabbixServer implements Serializable, Comparable<ZabbixServer> {

	private static final long serialVersionUID = 1L;

	private String name;
	private String url;
	private String username;
	private String password;

	private boolean trustAllSslCa;

	private boolean httpAuthEnabled;
	private String httpAuthUsername;
	private String httpAuthPassword;

	public ZabbixServer() {

	}

	public ZabbixServer(String name, String url, String username,
			String password, boolean trustAllSslCa, boolean httpAuthEnabled,
			String httpAuthUsername, String httpAuthPassword) {
		this.name = name;
		this.url = url;
		this.username = username;
		this.password = password;
		this.trustAllSslCa = trustAllSslCa;
		this.httpAuthEnabled = httpAuthEnabled;
		this.httpAuthUsername = httpAuthUsername;
		this.httpAuthPassword = httpAuthPassword;
>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
	}

	public String getName() {
		return name;
	}

<<<<<<< HEAD
	public void setId(long serverId) {
		this.id = serverId;
	}

=======
>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
	public void setName(String name) {
		this.name = name;
	}

<<<<<<< HEAD
=======
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isTrustAllSslCa() {
		return trustAllSslCa;
	}

	public void setTrustAllSslCa(boolean trustAllSslCa) {
		this.trustAllSslCa = trustAllSslCa;
	}

	public boolean isHttpAuthEnabled() {
		return httpAuthEnabled;
	}

	public void setHttpAuthEnabled(boolean httpAuthEnabled) {
		this.httpAuthEnabled = httpAuthEnabled;
	}

	public String getHttpAuthUsername() {
		return httpAuthUsername;
	}

	public void setHttpAuthUsername(String httpAuthUsername) {
		this.httpAuthUsername = httpAuthUsername;
	}

	public String getHttpAuthPassword() {
		return httpAuthPassword;
	}

	public void setHttpAuthPassword(String httpAuthPassword) {
		this.httpAuthPassword = httpAuthPassword;
	}

	@Override
	public String toString() {
		return "ZabbixServer [name=" + name + ", url=" + url + ", username="
				+ username + ", password=" + password + ", trustAllSslCa="
				+ trustAllSslCa + ", httpAuthEnabled=" + httpAuthEnabled
				+ ", httpAuthUsername=" + httpAuthUsername
				+ ", httpAuthPassword=" + httpAuthPassword + "]";
	}

>>>>>>> ed6f38d3ab663b2f3aa357cbc858f0d24717f20b
	@Override
	public int compareTo(ZabbixServer another) {
		return this.getName().compareTo(another.getName());
	}

}
