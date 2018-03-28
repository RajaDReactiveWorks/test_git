package com.attunedlabs.dynastore;

/**
 * This class represent the policy for DynaStoreSession(Cache).
 * Currently this Policy only stores as how much KB of data a user can store in cache per Cache session
 * @author Bizruntime
 *
 */
public class DynaStoreCachePolicy {
	long dataUploadLimit;

	DynaStoreCachePolicy(long dataUploadLimit){
		this.dataUploadLimit=dataUploadLimit;
	}
	
	public long getDataUploadLimit() {
		return dataUploadLimit;
	}

	
}
