package com.attunedlabs.aLPSIntegration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HTTPSClient1 {
	

	public static void main(String[] args) throws Exception {

		System.setProperty("javax.net.ssl.trustStoreType", "JKS");
	       System.setProperty("javax.net.ssl.trustStore", "D:\\NIFIJKS\\JKS\\truststore.jks");
	       System.setProperty("javax.net.ssl.trustStorePassword", "password123");
	      // System.setProperty("javax.net.ssl.keyStoreType", "JKS");
	       //System.setProperty("javax.net.ssl.keyStore", "D:\\NewJKS\\keystore.jks");
	      // System.setProperty("javax.net.ssl.keyStorePassword", "rwx123");
		System.setProperty("javax.net.debug", "all");
	       //System.setProperty("javax.net.debug", "ssl,handshake");
		//SSLContext sslContext = createSSLContext();


		int responseCode = 0;
		try {
			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

				public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
					return hostname.equals("localhost");
				}
			});

			URL destinationURL = new URL("https://localhost:8070");
			System.out.println("calling nifi .......");
			HttpsURLConnection con = (HttpsURLConnection) destinationURL.openConnection();
			SSLContext sslContext = createSSLContext();
			SSLSocketFactory socketFactory=sslContext.getSocketFactory();
			con.setSSLSocketFactory(socketFactory);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "text/xml");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("hello");
			wr.flush();
			wr.close();
			responseCode = con.getResponseCode();
			System.out.println("response code is :" + responseCode);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((line = reader.readLine()) != null) {

				System.out.println("reponse  is : " + line);
			}

			reader.close();
			con.disconnect();
		} catch (Exception e) {
			System.out.println("unable to call elastic Server "+e.getMessage());
			throw e;
		}


	}

	
	// Create the and initialize the SSLContext
	private static SSLContext createSSLContext() {
		try {
			// System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			// System.setProperty("java.net.useSystemProxies", "true");
			// System.setProperty("https.protocols", "TLSv1");
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("D:\\NewJKS\\keystore.jks"), "rwx123".toCharArray());
			
			KeyStore trust = KeyStore.getInstance("JKS");
			trust.load(new FileInputStream("D:\\NIFIJKS\\JKS\\truststore.jks"), "password123".toCharArray());
		

			// Create key manager
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, "rwx123".toCharArray());
			KeyManager[] km = keyManagerFactory.getKeyManagers();

			// Create trust manager
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trust);
			TrustManager[] tm = trustManagerFactory.getTrustManagers();

			// Initialize SSLContext
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, new SecureRandom());

			return sslContext;
		} catch (Exception ex) {
			ex.printStackTrace();

		}

		return null;
	}

	
}
