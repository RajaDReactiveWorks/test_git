package com.attunedlabs.security.service;

import com.attunedlabs.security.exception.UserRegistrationException;
import com.attunedlabs.security.pojo.UserDetails;

public interface IUserRegistryService {
	
	/**
	 * 
	 * @param userDetails
	 * @throws UserRegistrationException
	 */
	public void addNewUser(UserDetails userDetails) throws UserRegistrationException;
	
}
