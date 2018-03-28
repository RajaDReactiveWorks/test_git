/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.attunedlabs.featureInstaller.bundle.tracker;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

import com.attunedlabs.featureInstaller.bundle.tracker.impl.FeatureMetaInfoExtenderException;

/**
 * This is a very simple bundle tracker utility class that tracks active
 * bundles. 
 **/
public abstract class BundleTracker {
  final Set m_bundleSet = new HashSet();
  final BundleContext m_context;
  final SynchronousBundleListener m_listener;
  boolean m_open;

  /**
   * Constructs a bundle tracker object that will use the specified bundle
   * context.
   * 
   * @param context The bundle context to use to track bundles.
   **/
  public BundleTracker(BundleContext context){
    m_context = context;
    m_listener = new SynchronousBundleListener() {
      public void bundleChanged(BundleEvent evt) {
        synchronized (BundleTracker.this) {
          if (!m_open) {
            return;
          }

          if (evt.getType() == BundleEvent.STARTED) {
            if (!m_bundleSet.contains(evt.getBundle())) {
              m_bundleSet.add(evt.getBundle());            
					try {
						addedBundle(evt.getBundle());
					} catch (FeatureMetaInfoExtenderException e) {
						// TODO Need to add custom exception
						e.printStackTrace();
					}
				
            }
          } else if (evt.getType() == BundleEvent.STOPPED) {
            if (m_bundleSet.contains(evt.getBundle())) {
              m_bundleSet.remove(evt.getBundle());
              try {
					removedBundle(evt.getBundle());
				} catch (FeatureMetaInfoExtenderException e) {
					// TODO Need to add custom exception
					e.printStackTrace();
				}
            }
          }
        }
      }
    };
  }

  /**
   * Returns the current set of active bundles.
   * 
   * @return The current set of active bundles.
   **/
  public synchronized Bundle[] getBundles() {
    return (Bundle[]) m_bundleSet.toArray(new Bundle[m_bundleSet.size()]);
  }

	/**
	 * Call this method to start the tracking of active bundles.
	 * 
	 * @throws FeatureMetaInfoExtenderException
	 **/
  public synchronized void open() throws FeatureMetaInfoExtenderException {
    if (!m_open) {
      m_open = true;
      m_context.addBundleListener(m_listener);
      Bundle[] bundles = m_context.getBundles();
      System.out.println("bundles length : "+bundles.length);
      for (int i = 0; i < bundles.length; i++) {
        if (bundles[i].getState() == Bundle.ACTIVE) {
          m_bundleSet.add(bundles[i]);
          addedBundle(bundles[i]);
        }
      }
    }
  }

  /**
   * Call this method to stop the tracking of active bundles.
   **/
  public synchronized void close()throws FeatureMetaInfoExtenderException {
    if (m_open) {
      m_open = false;
      m_context.removeBundleListener(m_listener);
      Bundle[] bundles = (Bundle[]) m_bundleSet.toArray(new Bundle[m_bundleSet.size()]);
      for (int i = 0; i < bundles.length; i++) {
        if (m_bundleSet.remove(bundles[i])) {
          removedBundle(bundles[i]);
        }
      }
    }
  }

	/**
	 * Subclasses must implement this method; it can be used to perform actions
	 * upon the activation of a bundle.
	 * 
	 * @param bundle
	 *            The bundle being added to the active set.
	 * @throws FeatureMetaInfoExtenderException
	 **/
  protected abstract void addedBundle(Bundle bundle) throws FeatureMetaInfoExtenderException;

  /**
   * Subclasses must implement this method; it can be used to perform actions
   * upon the deactivation of a bundle.
   * @param bundle The bundle being removed from the active set.
   **/
  protected abstract void removedBundle(Bundle bundle)throws FeatureMetaInfoExtenderException;
}
