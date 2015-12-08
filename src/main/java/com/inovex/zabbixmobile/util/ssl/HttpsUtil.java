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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

/**
 * Created by felix on 07/12/15.
 */
public class HttpsUtil {

	private static final String TAG = "HttpsUtil";
	private static final String IPADDRESS_PATTERN =
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

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
			final HostnameVerifier defaultVerifier = connection.getHostnameVerifier();
			// unsafe workaround for https://github.com/square/okhttp/issues/1467
//			connection.setHostnameVerifier(new HostnameVerifier()  {
//				@Override
//				public boolean verify(String hostname, SSLSession session) {
//					if(!defaultVerifier.verify(hostname, session)){
//						Log.d(TAG,"Accepting IP");
//						Pattern p = Pattern.compile(IPADDRESS_PATTERN);
//						Matcher m = p.matcher(hostname);
//						return m.matches();
//					}
//					return true;
//				}
//			});
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
							.setMessage(getChainInfo(chain))
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

	private static String getChainInfo(X509Certificate[] chain) {
		StringBuilder chainInfo = new StringBuilder();
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Error while initializing MessageDigest", e);
		}

		for (int i = 0; i < chain.length; i++) {
			chainInfo.append("Certificate chain[").append(i).append("]:\n");
			chainInfo.append("Subject: ").append(chain[i].getSubjectDN().toString()).append("\n");
			chainInfo.append("Issuer: ").append(chain[i].getIssuerDN().toString()).append("\n");
			if (sha1 != null) {
				sha1.reset();
				try {
					char[] sha1sum = encodeHex(sha1.digest(chain[i].getEncoded()));
					chainInfo.append("Fingerprint (SHA-1): ").append(new String(sha1sum)).append("\n");
				} catch (CertificateEncodingException e) {
					Log.e(TAG, "Error while encoding certificate", e);
				}
			}
		}

		return chainInfo.toString();
	}

	private static char[] encodeHex(byte[] bytes) {
		char[] DIGITS = {
				'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
		};
		int l = bytes.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & bytes[i]) >>> 4 ];
			out[j++] = DIGITS[ 0x0F & bytes[i] ];
		}
		return out;
	}
}
