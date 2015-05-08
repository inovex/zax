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
				R.string.exc_no_response), HTTPS_CERTIFICATE_NOT_TRUSTED(
				R.string.exc_https_certificate_not_trusted);

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
