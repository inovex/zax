package com.inovex.zabbixmobile.util.ssl;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Created by felix on 07/12/15.
 */
public class HttpsUtil {

	private static final String TAG = "HttpsUtil";

	public static HttpsURLConnection getHttpsUrlConnection(URL url) throws IOException {
		return HttpsUtil.getHttpsUrlConnection(url,false);
	}

	public static HttpsURLConnection getHttpsUrlConnection(URL server_url,boolean useLocalKeystore) throws IOException {
		HttpsURLConnection connection;
		try {
			connection = (HttpsURLConnection) server_url.openConnection();
		} catch (ClassCastException e){
			Log.e(TAG, "url is no https-url");
			return null;
		}
		if(server_url.getProtocol().equals("https")){
			int port = server_url.getPort();
			String host = server_url.getHost();
			TrustManager[] trustManagers = {TrustManagerFactory.get(host, port)};
			SSLContext context = null;
			try {
				context = SSLContext.getInstance("TLS");
				context.init(null, trustManagers, null);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}
			((HttpsURLConnection)connection).setSSLSocketFactory(context.getSocketFactory());
		}
		return connection;
	}

	public static void clearLocalKeyStore(){
		LocalKeyStore.getInstance().deleteAllCertificates();
	}
}
