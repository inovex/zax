package com.inovex.zabbixmobile.exceptions;

import com.inovex.zabbixmobile.R;

public class FatalException extends Exception {

	private static final long serialVersionUID = 80056965825197156L;

	public enum Type {
		HTTP_AUTHORIZATION_REQUIRED(R.string.exc_http_auth_required),
		NO_API_ACCESS(R.string.exc_no_api_access),
		PRECONDITION_FAILED(R.string.exc_precondition_failed),
		ZABBIX_LOGIN_INCORRECT(R.string.exc_login_incorrect),
		INTERNAL_ERROR(R.string.exc_internal_error);
		
		private int messageResourceId;

		private Type(int messageResourceId) {
			this.messageResourceId = messageResourceId;
		}
		
		protected int getMessageResourceId() {
			return messageResourceId;
		}
	};
	
	private final Type type;
	
	public FatalException(Type type) {
		this.type = type;
	}

	public FatalException(Type type, Throwable t) {
		super(t);
		this.type = type;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public int getMessageResourceId() {
		return this.type.getMessageResourceId();
	}
}
