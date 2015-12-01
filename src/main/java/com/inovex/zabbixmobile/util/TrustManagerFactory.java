package com.inovex.zabbixmobile.util;

import android.util.Log;

import com.inovex.zabbixmobile.exceptions.CertificateChainException;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by felix on 30/11/15.
 */
public class TrustManagerFactory {

	private static final String TAG = "TrustManagerWrapper";
	private static LocalKeyStore keyStore = null;
	private static X509TrustManager defaultTrustManager = null;

	private TrustManagerFactory(){}

	public static CombinedTrustManager get(String host, int port){
		return CombinedTrustManager.getInstance(host, port);
	}

	static {
		try {
			keyStore = LocalKeyStore.getInstance();

			javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance("X509");
			tmf.init((KeyStore) null);

			TrustManager[] tms = tmf.getTrustManagers();
			if (tms != null) {
				for (TrustManager tm : tms) {
					if (tm instanceof X509TrustManager) {
						defaultTrustManager = (X509TrustManager) tm;
						break;
					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Unable to get X509 Trust Manager ", e);
		} catch (KeyStoreException e) {
			Log.e(TAG, "Key Store exception while initializing TrustManagerFactory ", e);
		}
	}

	public static class CombinedTrustManager implements X509TrustManager {
		private static final Map<String,CombinedTrustManager> mTrustManagers =
				new HashMap<String, CombinedTrustManager>();

		private final String mHost;
		private final int mPort;

		public CombinedTrustManager(String mHost, int mPort) {
			this.mHost = mHost;
			this.mPort = mPort;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			defaultTrustManager.checkClientTrusted(chain,authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			String message = null;
			X509Certificate certificate = chain[0];

			Throwable cause = null;

			try{
				defaultTrustManager.checkServerTrusted(chain, authType);
				new StrictHostnameVerifier().verify(mHost,certificate);
			} catch (SSLException e) {
				// hostname does not match certificate
				message = e.getMessage();
				cause = e;
			} catch (CertificateException e){
				// certificate chain can't be validated
				message = e.getMessage();
				cause = e;
			}

			if(!keyStore.checkCertificateIsValid(certificate,mHost,mPort)){
				throw new CertificateChainException(message,cause,chain);
			}
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return defaultTrustManager.getAcceptedIssuers();
		}

		public synchronized static CombinedTrustManager getInstance(String host, int port) {
			String key = host + ":" + port;
			CombinedTrustManager trustManager;
			if(mTrustManagers.containsKey(key)){
				trustManager = mTrustManagers.get(key);
			} else {
				trustManager = new CombinedTrustManager(host,port);
				mTrustManagers.put(key,trustManager);
			}
			return trustManager;
		}

		public void addTrustedCertificate(X509Certificate certificate){
			if(keyStore != null){
				try {
					keyStore.addCertificate(mHost,mPort,certificate);
				} catch (CertificateException e) {
					e.printStackTrace();
				}
			}
		}
	}
}