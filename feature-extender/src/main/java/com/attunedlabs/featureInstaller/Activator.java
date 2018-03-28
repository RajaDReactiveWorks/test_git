/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.attunedlabs.featureInstaller;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.eventframework.dispatcher.transformer.ILeapEventTransformer;
import com.attunedlabs.featureInstaller.bundle.tracker.impl.FeatureMetaInfoExtender;
import com.attunedlabs.featureInstaller.bundle.tracker.impl.FeatureMetaInfoExtenderException;
import com.attunedlabs.featureInstaller.servicetracker.LeapCustomServiceTacker;
import com.attunedlabs.permastore.config.IPermaStoreCustomCacheObjectBuilder;

public class Activator implements BundleActivator {
	final static Logger logger = LoggerFactory.getLogger(Activator.class);
	 private ServiceTracker serviceTracker;
	 private ServiceTracker serviceTracker1;
	FeatureMetaInfoExtender featureMetaInfoExtender=null;
	
	/**
	 * Activator start will be called when bundle is installed  and started
	 */
    public void start(BundleContext context) throws FeatureMetaInfoExtenderException {
   	 logger.debug("Starting the bundle");
        featureMetaInfoExtender=new FeatureMetaInfoExtender(context);
        logger.debug("feature meta Info got created in activator start");
        featureMetaInfoExtender.open();
        logger.debug("after open");
        LeapCustomServiceTacker permastoreServiceTracker=new LeapCustomServiceTacker(context);
			serviceTracker = new ServiceTracker(context, IPermaStoreCustomCacheObjectBuilder.class
				        .getName(), permastoreServiceTracker);
			serviceTracker1 = new ServiceTracker(context, ILeapEventTransformer.class
			        .getName(), permastoreServiceTracker);
			serviceTracker.open();
			serviceTracker1.open();

    }

    /**
	 * Activator stop will be called when bundle is stopped
	 */
    public void stop(BundleContext context) throws FeatureMetaInfoExtenderException {
   	 logger.debug("Stopping the bundle");
        featureMetaInfoExtender.close();
        serviceTracker.close();
    }

}