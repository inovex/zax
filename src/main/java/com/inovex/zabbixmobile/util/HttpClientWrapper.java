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

package com.inovex.zabbixmobile.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

/**
 * Convenience class for dealing with an HTTP client.
 *
 */
public class HttpClientWrapper {
	private final AbstractHttpClient mHttpClient;

	public HttpClientWrapper(DefaultHttpClient defaultHttpClient) {
		mHttpClient = defaultHttpClient;
	}

	public CredentialsProvider getCredentialsProvider() {
		return mHttpClient.getCredentialsProvider();
	}

	public HttpParams getParams() {
		return mHttpClient.getParams();
	}

	public HttpResponse execute(HttpPost post) throws ClientProtocolException,
			IOException {
		return mHttpClient.execute(post);
	}


}
