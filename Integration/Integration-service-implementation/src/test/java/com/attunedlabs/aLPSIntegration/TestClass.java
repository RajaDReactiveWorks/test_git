package com.attunedlabs.aLPSIntegration;

import java.util.List;

import org.json.JSONException;

import com.jayway.jsonpath.JsonPath;

public class TestClass {
	public static void main(String[] args) throws JSONException {
		

String jsonString = "{ \"EventId\": \"ListRelease\", \"EventParam\": { \"ELASTICEVENT\": [{ \"HEADER\": { \"EVENTPAYLOADNAME\": \"ListGroupInformation\", \"EVENTSEQ\": \"201729818321\", \"EVENTID\": \"ListRelease\", \"UUID\":\"12345\", \"WHID\": \"FR57\", \"WCSID\": \"WCS_ELASTIC\", \"EVENTCREATEDONUTC\": \"20170206121212\" } }, { \"PAYLOAD\": [{ \"ListGroupInformation\": [{ \"GROUPVAL\": \"g4223232\" }, { \"LISTINFORMATION\": [{ \"LISTID\": \"LST00001\" }, { \"LISTID\": \"LST00002\" }] }] }] }] }, \"EventHeader\": { \"siteId\": \"all\", \"tenantId\": \"all\" } }";
String key="GROUPVAL";

		List<String> groupVals = JsonPath.read(jsonString, "$.."+key);
		System.out.println("list is "+groupVals.toString());
		for(String s : groupVals){
			System.out.println("GROUPVAL  is :"+s);
		}
		
	}

}
