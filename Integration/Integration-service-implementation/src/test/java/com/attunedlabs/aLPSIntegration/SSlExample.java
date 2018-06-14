package com.attunedlabs.aLPSIntegration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSlExample {

	
	public static void main(String[] args) {
        try{
        	System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    		System.setProperty("javax.net.ssl.trustStore", "D:\\NIFIJKS\\JKS\\truststore.jks");
    		System.setProperty("javax.net.ssl.trustStorePassword", "password123");
    		System.setProperty("javax.net.debug", "all");
    		/*System.setProperty("javax.net.ssl.keyStore","D:\\JKSJDK7\\keystore.jks");
    		System.setProperty("javax.net.ssl.keyStorePassword","password123");
    		System.setProperty("javax.net.ssl.keyStoreType", "JKS");*/
     
        // Connect
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("0.0.0.0",8070);
     
        // Send HTTP GET request
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), "UTF8"));
        bufferedWriter.write("hello nifi");
        bufferedWriter.flush();
 
        // Read response
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        String string = null;
     
        while ((string = bufferedReader.readLine()) != null) {
            System.out.println(string);
            System.out.flush();
        }
     
        bufferedReader.close();
        bufferedWriter.close();
     
        // Close connection.
        sslSocket.close();
     
    }catch(Exception exception){
        System.out.println(exception.getMessage());
    }
}
	

}
