package com.inovex.zabbixmobile.exceptions;

public class ZabbixLoginRequiredException extends Exception {

	private static final long serialVersionUID = 537466652124519153L;

	public ZabbixLoginRequiredException() {
		
	}
	
	public ZabbixLoginRequiredException(Throwable t) {
		super(t);
	}
}
