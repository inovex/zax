package com.inovex.zabbixmobile.util.ssl;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.inovex.zabbixmobile.exceptions.CertificateChainException;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
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
		if(useLocalKeystore && server_url.getProtocol().equals("https")){
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
			connection.setSSLSocketFactory(context.getSocketFactory());
		}
		return connection;
	}

	public static void clearLocalKeyStore(){
		LocalKeyStore keyStore = LocalKeyStore.getInstance();
		if(keyStore != null){
			keyStore.deleteAllCertificates();
		}
	}

	public static void checkCertificate(final Context context, final URL url){
		new AsyncTask<Object, Object, Object>(){

			@Override
			protected Object doInBackground(Object[] params) {
				try {
					HttpsURLConnection connection = HttpsUtil.getHttpsUrlConnection(url,true);
					connection.setDoOutput(true);
					connection.setRequestMethod("GET");
					int responseCode = connection.getResponseCode();
				} catch (SSLHandshakeException e){
					final X509Certificate[] chain = ((CertificateChainException) e.getCause()).getmCertChain();
					return chain;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassCastException e){
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Object o) {
				super.onPostExecute(o);
				final X509Certificate [] chain = (X509Certificate[]) o;
				if(chain != null){
					new AlertDialog.Builder(context)
							.setTitle("Certificate could not be validated")
									// TODO add more information about the certificate
							.setMessage(" MORE INFORMATION GOES HERE")
							.setCancelable(true)
							.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									TrustManagerFactory.get(url.getHost(), url.getPort()).addTrustedCertificate(chain[0]);
								}
							})
							.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// Do nothing
								}
							})
							.show();
				} else {
					// Certificate is valid
				}
			}
		}.execute(context);

	}
}
