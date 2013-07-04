package com.inovex.zabbixmobile.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

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

	public HttpResponse execute(HttpPost post) throws ClientProtocolException, IOException {
		return mHttpClient.execute(post);
	}
}
