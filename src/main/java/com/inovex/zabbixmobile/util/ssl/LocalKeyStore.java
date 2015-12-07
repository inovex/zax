package com.inovex.zabbixmobile.util.ssl;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by felix on 30/11/15.
 */
public class LocalKeyStore {

	private static final String TAG = "LocalKeyStore";
	private static String keyStoreDirectory;

	public static void setKeyStoreDirectory(String directory) {
		keyStoreDirectory = directory;
		KeystoreHolder.instance = new LocalKeyStore();
	}

	private static class KeystoreHolder{
		static LocalKeyStore instance;
	}

	public static LocalKeyStore getInstance(){
		return KeystoreHolder.instance;
	}

	private File mKeyStoreFile;
	private KeyStore mKeyStore;


	private LocalKeyStore(){
		File file;
		file = new File( keyStoreDirectory, "keystore.bks");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// it's okay if this is null, keystore can handle that
		}
		try {
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			store.load(fis,"".toCharArray());
			mKeyStore = store;
			mKeyStoreFile = file;
			writeKeystoreToFile();
		} catch (Exception e) {
			mKeyStore = null;
			mKeyStoreFile = null;
		} finally {
			try {
				if(fis != null){
					fis.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void addCertificate(String host, int port, X509Certificate certificate) throws CertificateException {
		if(mKeyStore == null){
			throw new CertificateException("Certificate can not be added, because key store is not initialized");
		}
		try{
			mKeyStore.setCertificateEntry(getKeyAlias(host, port), certificate);
		} catch (KeyStoreException e) {
			throw new CertificateException("Failed to add certificate to local store",e);
		}
		writeKeystoreToFile();
	}

	@NonNull
	private String getKeyAlias(String host, int port) {
		return host + ":" + port;
	}

	private void writeKeystoreToFile() throws CertificateException {
		OutputStream keyStoreStream = null;
		try{
			keyStoreStream = new FileOutputStream(mKeyStoreFile);
			mKeyStore.store(keyStoreStream, "".toCharArray());
		} catch (FileNotFoundException e) {
			throw new CertificateException("Unable to write key :" + e.getMessage());
		} catch (CertificateException e) {
			throw new CertificateException("Unable to write key :" + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new CertificateException("Unable to write key :" + e.getMessage());
		} catch (KeyStoreException e) {
			throw new CertificateException("Unable to write key :" + e.getMessage());
		} catch (IOException e) {
			throw new CertificateException("Unable to write key :" + e.getMessage());
		} finally {
			try {
				keyStoreStream.close();
			} catch (IOException e) {
			}
		}
	}

	public boolean checkCertificateIsValid(Certificate certificate, String host, int port){
		if(mKeyStore != null) {
			Certificate storedCert;
			try {
				storedCert = mKeyStore.getCertificate(getKeyAlias(host, port));
				if (storedCert != null) {
					PublicKey storedPubKey = storedCert.getPublicKey();
					PublicKey hostKey = certificate.getPublicKey();
					return storedPubKey.equals(hostKey);
				}
			} catch (KeyStoreException e) {
				return false;
			}
		}
		return false;
	}

	public void deleteCertificate(String host, int port){
		if(mKeyStore != null){
			Certificate storedCert = null;
			try{
				mKeyStore.deleteEntry(getKeyAlias(host, port));
				writeKeystoreToFile();
			} catch (KeyStoreException e) {
				// ignore
			} catch (CertificateException e) {
				Log.e(TAG, "error updating key store file",e);
			}
		}
	}

	public void deleteAllCertificates(){
		if(mKeyStore != null){
			mKeyStoreFile.delete();
			KeystoreHolder.instance = new LocalKeyStore();
		}
	}
}
