package com.inovex.zabbixmobile.exceptions;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by felix on 01/12/15.
 */
public class CertificateChainException extends CertificateException {
	private X509Certificate[] mCertChain;

	public CertificateChainException(String message, Throwable cause, X509Certificate[] mCertChain) {
		super(message, cause);
		this.mCertChain = mCertChain;
	}

	public X509Certificate[] getmCertChain() {
		return mCertChain;
	}

	public void setmCertChain(X509Certificate[] mCertChain) {
		this.mCertChain = mCertChain;
	}
}
