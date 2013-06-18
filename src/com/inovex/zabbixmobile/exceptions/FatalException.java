package com.inovex.zabbixmobile.exceptions;

public class FatalException extends Exception {

	private static final long serialVersionUID = 80056965825197156L;

	public FatalException(Throwable t) {
		super(t);
	}
}
