package com.inovex.zabbixmobile.exceptions;

/**
 * This exception is thrown when a user is not logged in and a relogin needs to
 * be performed.
 * 
 */
public class ZabbixLoginRequiredException extends Exception {

	private static final long serialVersionUID = 537466652124519153L;

	public ZabbixLoginRequiredException() {

	}

	public ZabbixLoginRequiredException(Throwable t) {
		super(t);
	}
}
