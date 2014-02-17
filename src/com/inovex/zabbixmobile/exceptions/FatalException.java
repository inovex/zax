package com.inovex.zabbixmobile.exceptions;

import com.inovex.zabbixmobile.R;

/**
 * An exception which cannot be handled by the program. Instead, it triggers a
 * broadcast which is currently used to show a toast.
 * 
 */
public class FatalException extends Exception {

	private static final long serialVersionUID = 80056965825197156L;

	public enum Type {
		HTTP_AUTHORIZATION_REQUIRED(R.string.exc_http_auth_required), NO_API_ACCESS(
				R.string.exc_no_api_access), PRECONDITION_FAILED(
				R.string.exc_precondition_failed), ZABBIX_LOGIN_INCORRECT(
				R.string.exc_login_incorrect), NO_CONNECTION(
				R.string.exc_no_connection), CONNECTION_TIMEOUT(
				R.string.exc_connection_timeout), SERVER_NOT_FOUND(
				R.string.exc_not_found), INTERNAL_ERROR(
				R.string.exc_internal_error), ACCOUNT_BLOCKED(
				R.string.exc_account_blocked), NO_HTTP_RESPONSE(
				R.string.exc_no_response), HTTPS_TRUST_NOT_ENABLED(
				R.string.exc_https_auth_not_enabled);

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

	public FatalException(Type type, String detailedMessage) {
		super(detailedMessage);
		this.type = type;
	}

	public Type getType() {
		return this.type;
	}

	public int getMessageResourceId() {
		return this.type.getMessageResourceId();
	}
}
