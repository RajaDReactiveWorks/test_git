package com.attunedlabs.featureinstaller;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceException;
import com.attunedlabs.featureinstaller.util.FeatureMetaInfoResourceUtil;
import com.attunedlabs.leap.LeapHeaderConstant;
import com.attunedlabs.leap.TenantSitePropertiesLoader;
import com.attunedlabs.leap.TenantSitePropertiesLoadingException;
import com.attunedlabs.leap.load.resource.CamelApplicationRun;

/**
 * This method is used to search featureMetaInfo in all jars available in
 * classpath and load resources available at feature level
 * 
 * @author bizruntime
 */
public class FeatureMetaInfoConfigInstaller {

	final static Logger logger = LoggerFactory.getLogger(FeatureMetaInfoConfigInstaller.class);
	private static final String PATTERN_SEARCH_KEY = "featureMetaInfo.xml";

	/**
	 * This method is used to search featureMetaInfo in all jars available in
	 * classpath and load resources available at feature level
	 * 
	 * @throws FeatureMetaInfoConfigInstallerException
	 */
	public void loadFeatureMetaInfoResources() throws FeatureMetaInfoConfigInstallerException {
		TenantSitePropertiesLoader propLoader = new TenantSitePropertiesLoader();
		try {

			propLoader.setTenantAndSite();
			logger.debug("tenant : " + LeapHeaderConstant.tenant + ", site : " + LeapHeaderConstant.site);
			FeatureMetaInfoResourceUtil fmiResList = new FeatureMetaInfoResourceUtil();
			Pattern pattern = Pattern.compile(PATTERN_SEARCH_KEY);
			try {
				fmiResList.getClassPathResources(pattern);
				CamelApplicationRun runCamelApplication = new CamelApplicationRun();
				try {
					runCamelApplication.startCamelApplication();
				} catch (Exception e) {
					throw new FeatureMetaInfoConfigInstallerException("Unable start camel application ", e);
				}
			} catch (FeatureMetaInfoResourceException e) {
				throw new FeatureMetaInfoConfigInstallerException(
						"Unable to load " + PATTERN_SEARCH_KEY + " from class path ", e);
			}
		} catch (TenantSitePropertiesLoadingException e1) {
			throw new FeatureMetaInfoConfigInstallerException("Unable to site and tenant from class path : ");
		}
	}// end of method

	public static void main(String[] args) throws FeatureMetaInfoConfigInstallerException {
		   cleanAtomikosLogs(); 
			FeatureMetaInfoConfigInstaller installer = new FeatureMetaInfoConfigInstaller();
			installer.loadFeatureMetaInfoResources();
	        cleanAtomikosLogs(); 

		}
		
		 public static void cleanAtomikosLogs() { 
		        try { 
		            File currentDir = new File("."); 
		            final File[] tmLogs = currentDir.listFiles(new FilenameFilter() { 
		                public boolean accept(File dir, String name) { 
		                    if (name.endsWith(".epoch") || name.startsWith("tmlog")) { 
		                        return true; 
		                    } 
		                    return false; 
		                } 
		            }); 
		            for (File tmLog : tmLogs) { 
		                tmLog.delete(); 
		            } 
		        } catch (Exception e) { 
		        	logger.debug("failed to delete atomikos logs ..." + e);
		        } 
		    } 

}