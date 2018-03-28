package com.attunedlabs.eventframework.abstractbean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.metamodel.CompositeDataContext;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.schema.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.config.persistence.ConfigurationTreeNode;
import com.attunedlabs.config.persistence.ITenantConfigTreeService;
import com.attunedlabs.config.persistence.UndefinedPrimaryVendorForFeature;
import com.attunedlabs.config.persistence.impl.TenantConfigTreeServiceImpl;
import com.attunedlabs.datacontext.config.DataContextConfigurationException;
import com.attunedlabs.datacontext.config.DataContextConfigurationUnit;
import com.attunedlabs.datacontext.config.IDataContextConfigurationService;
import com.attunedlabs.datacontext.config.impl.DataContextConfigurationService;
import com.attunedlabs.datacontext.jaxb.FeatureDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContext;
import com.attunedlabs.datacontext.jaxb.RefDataContexts;
import com.attunedlabs.featuredeployment.FeatureDeployment;
import com.attunedlabs.featuredeployment.FeatureDeploymentServiceException;
import com.attunedlabs.featuredeployment.IFeatureDeployment;
import com.attunedlabs.featuredeployment.impl.FeatureDeploymentService;
import com.attunedlabs.leap.LeapHeader;
import com.attunedlabs.leap.LeapHeaderConstant;

public abstract class AbstractMetaModelBean extends AbstractLeapCamelBean {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractLeapCamelBean.class);
	private DataSource dataSource = null;
	private List<DataSource> dataSourceList;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataSourceList(List<DataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}

	protected JdbcDataContext getLocalDataContext(Exchange exchange) throws Exception {
		logger.debug(".getMetaModelJdbcDataContext method of AbstractMetaModelBean");
		Connection con = getConnection(dataSource, exchange);
		logger.debug("AbstractMetaModelBean.getMetaModelJdbcDataContext got the connection : " + con);
		logger.debug("status of connection : " + con.isClosed() + " : " + con.getMetaData().getDatabaseProductName());
		JdbcDataContext metamodelJdbcContext = new JdbcDataContext(con);
		metamodelJdbcContext.setIsInTransaction(true);
		return metamodelJdbcContext;
	}

	protected DataContext getCompositeMetaModelDataContext(Exchange exchange) throws Exception {

		logger.debug(".getCompositeMetaModelDataContext method of AbstractMetaModelBean");
		Collection<DataContext> coll = new ArrayList<>();
		logger.debug("DataSource List : " + dataSourceList);
		if (dataSourceList != null && !(dataSourceList.isEmpty())) {
			for (DataSource datasource : dataSourceList) {
				Connection connection = getConnection(datasource, exchange);

				logger.debug(
						"AbstractMetaModelBean.getCompositeMetaModelDataContext got the connection : " + connection);
				DataContext metamodelJdbcContext = new JdbcDataContext(connection);

				coll.add(metamodelJdbcContext);
			}
		}
		DataContext dataContextComposite = new CompositeDataContext(coll);
		return dataContextComposite;
	}

	protected JdbcDataContext getMetaModelJdbcDataContext(TableType[] tableTypes, String catalogName, Exchange exchange)

			throws Exception {
		// #TODO Support is not yet Provided
		return null;
	}

	@Override
	protected abstract void processBean(Exchange exch) throws Exception;

	/**
	 * This method is used to get com.attunedlabs.datacontext.jaxb.DataContext
	 * Object
	 * 
	 * @param requestContext
	 *            : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 * @throws UndefinedPrimaryVendorForFeature
	 * @throws FeatureDeploymentServiceException
	 */

	protected com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContext(Exchange exchange)
			throws DataContextConfigurationException, UndefinedPrimaryVendorForFeature,
			FeatureDeploymentServiceException {
		logger.debug(".getThisDataContext method of AbstractMetaModelBean");
		LeapHeader leapheader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext requestContext = leapheader.getRequestContext();
		RequestContext newRequestContext;
		/**
		 * In some cases the AbstractMetaModel bean getDataContext gets 'null' as
		 * implementation, hence below condition
		 **/
		if (leapheader.getImplementationName() == null) {
			IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
			FeatureDeployment featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
					leapheader.getTenant(), leapheader.getSite(), leapheader.getFeatureName(), leapheader);
			newRequestContext = new RequestContext(leapheader.getTenant(), leapheader.getSite(),
					requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
					featureDeployment.getFeatureVersion());
		} else {
			newRequestContext = leapheader.getRequestContext();
		}
		com.attunedlabs.datacontext.jaxb.DataContext datacontext = getDataContextObjectOfFeature(newRequestContext);

		return datacontext;
	}

	/**
	 * This method is used to get com.attunedlabs.datacontext.jaxb.DataContext
	 * Object
	 * 
	 * @param requestContext
	 *            : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 * @throws FeatureDeploymentServiceException
	 */

	protected com.attunedlabs.datacontext.jaxb.DataContext getFeatureDataContext(String dbname, Exchange exchange)
			throws DataContextConfigurationException, FeatureDeploymentServiceException {
		logger.debug(".getThisDataContext method of AbstractMetaModelBean");
		LeapHeader leapheader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext requestContext = leapheader.getRequestContext();
		RequestContext newRequestContext;
		/**
		 * In some cases the AbstractMetaModel bean getDataContext gets 'null' as
		 * implementation, hence below condition
		 **/
		if (leapheader.getImplementationName() == null) {
			IFeatureDeployment featureDeploymentservice = new FeatureDeploymentService();
			FeatureDeployment featureDeployment = featureDeploymentservice.getActiveAndPrimaryFeatureDeployedFromCache(
					leapheader.getTenant(), leapheader.getSite(), leapheader.getFeatureName(), leapheader);
			newRequestContext = new RequestContext(leapheader.getTenant(), leapheader.getSite(),
					requestContext.getFeatureGroup(), requestContext.getFeatureName(),
					featureDeployment.getImplementationName(), featureDeployment.getVendorName(),
					featureDeployment.getFeatureVersion());
		} else {
			newRequestContext = leapheader.getRequestContext();
		}
		com.attunedlabs.datacontext.jaxb.DataContext datacontext = getDataContextObjectOfFeature(newRequestContext,
				dbname);

		return datacontext;
	}

	/**
	 * This method is used to get
	 * com.attunedlabs.datacontext.jaxb.FeatureDataContext Object
	 * 
	 * @param requestContext
	 *            : RequestCOntext Object of a feature
	 * @return com.attunedlabs.datacontext.jaxb.FeatureDataContext
	 * @throws DataContextConfigurationException
	 */
	protected FeatureDataContext getFeatureDataContextObject(RequestContext requestContext)
			throws DataContextConfigurationException {
		logger.debug(".getReferenceFeatureDataContext method of AbstractMetaModelBean");
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);
		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		return featureDataContext;
	}

	/**
	 * This method is used to compare datacontext for this feature and other
	 * feature. If same then create Apache metamodel datacontext else create
	 * composite datacontext
	 * 
	 * @param requestContext
	 *            : Feature Request Context Object
	 * @param featureDataContext
	 *            : FeatureDataContext Object of current feature
	 * @param refFeatureDataContext
	 *            : FeatureDataContext Object of reference feature
	 * @return
	 */
	protected boolean compareDataContext(RequestContext requestContext,

			com.attunedlabs.datacontext.jaxb.DataContext featureDataContext, FeatureDataContext refFeatureDataContext) {

		logger.debug(".compareDataContext method of AbstractMetaModelBean");
		boolean flag = false;
		String dbBeanRefName = featureDataContext.getDbBeanRefName();
		String dbType = featureDataContext.getDbType();
		String dbHost = featureDataContext.getDbHost();
		String dbPort = featureDataContext.getDbPort();
		String dbSchema = featureDataContext.getDbSchema();
		List<RefDataContexts> refDataContextsList = refFeatureDataContext.getRefDataContexts();
		for (RefDataContexts refDataContexts : refDataContextsList) {
			String featureGroup = refDataContexts.getFeatureGroup();
			String featureName = refDataContexts.getFeatureName();
			if (featureGroup.equalsIgnoreCase(requestContext.getFeatureGroup())
					&& featureName.equalsIgnoreCase(requestContext.getFeatureName())) {
				List<RefDataContext> refDataContextList = refDataContexts.getRefDataContext();
				for (RefDataContext refDataContext : refDataContextList) {
					if (refDataContext.getDbBeanRefName().equalsIgnoreCase(dbBeanRefName)
							&& refDataContext.getDbType().equalsIgnoreCase(dbType)
							&& refDataContext.getDbHost().equalsIgnoreCase(dbHost)
							&& refDataContext.getDbPort().equalsIgnoreCase(dbPort)
							&& refDataContext.getDbSchema().equalsIgnoreCase(dbSchema)) {
						flag = true;
					} else {
						flag = false;
					}
				}
			} // end of if matching fetaureGroup and featureName
		} // end of for(RefDataContexts refDataContexts:refDataContextsList)

		return flag;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * Object based on feature group and feature name
	 * 
	 * @param featureGroup
	 *            : Feature group in String
	 * @param feature
	 *            : Feature in String
	 * @param exchange
	 *            : Camel Exchange Object
	 * @return com.attunedlabs.datacontext.jaxb.DataContext Object
	 * @throws Exception
	 */
	protected com.attunedlabs.datacontext.jaxb.DataContext getReferenceFeatureDataContext(String featureGroup,
			String feature, Exchange exchange) throws Exception {
		logger.debug(".getDataContextForFeature method of AbstractMetaModelBean");

		LeapHeader leapheader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		ITenantConfigTreeService tenantTreeService = TenantConfigTreeServiceImpl.getTenantConfigTreeServiceImpl();
		ConfigurationTreeNode fgconfigNodeTree = tenantTreeService.getPrimaryVendorForFeature(leapheader.getTenant(),
				leapheader.getSite(), featureGroup, feature);
		String vendorName = fgconfigNodeTree.getNodeName();
		String version = fgconfigNodeTree.getVersion();
		RequestContext requestContext = new RequestContext(leapheader.getTenant(), leapheader.getSite(), featureGroup,
				feature, leapheader.getImplementationName(), vendorName, version);
		com.attunedlabs.datacontext.jaxb.DataContext dataContext = getDataContextObjectOfFeature(requestContext);
		return dataContext;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * Object based on feature group,feature name,vendor,version,db name
	 * 
	 * @param featureGroup
	 *            : Feature group in String
	 * @param feature
	 *            : Feature in String
	 * @param vendor
	 *            : vendor in String
	 * @param version
	 *            : version in String
	 * @param db
	 *            name : dbname in String
	 * @param exchange
	 *            : Camel Exchange Object
	 * @return com.attunedlabs.datacontext.jaxb.DataContext Object
	 * @throws Exception
	 */

	protected com.attunedlabs.datacontext.jaxb.DataContext getReferenceFeatureDataContext(String featureGroup,
			String feature, String vendor, String version, String dbName, Exchange exchange) throws Exception {
		logger.debug(".getDataContextForFeature method of AbstractMetaModelBean");
		LeapHeader leapheader = (LeapHeader) exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY);
		RequestContext requestContext = new RequestContext(leapheader.getTenant(), leapheader.getSite(), featureGroup,
				feature, leapheader.getImplementationName(), vendor, version);
		com.attunedlabs.datacontext.jaxb.DataContext dataContext = getDataContextObjectOfFeature(requestContext,
				dbName);

		return dataContext;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * of reference feature #TODO,till now its not decided how many data context a
	 * feature can have,in xml we have provided a support for multiple but in code
	 * implementation we are taking it as only one.
	 * 
	 * @param requestContext
	 *            : Request Context Object of current feature
	 * @return com.attunedlabs.datacontext.jaxb.DataContext
	 * @throws DataContextConfigurationException
	 */
	private com.attunedlabs.datacontext.jaxb.DataContext getDataContextObjectOfFeature(RequestContext requestContext)
			throws DataContextConfigurationException {
		logger.debug(".getDataContextForFeature method of AbstractMetaModelBean");
		com.attunedlabs.datacontext.jaxb.DataContext dataContextforFeature = null;
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);

		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		List<com.attunedlabs.datacontext.jaxb.DataContext> dataContextList = featureDataContext.getDataContexts()
				.getDataContext();

		for (com.attunedlabs.datacontext.jaxb.DataContext dataContext : dataContextList) {
			dataContextforFeature = dataContext;
		}
		return dataContextforFeature;
	}

	/**
	 * This method is used to get the com.attunedlabs.datacontext.jaxb.DataContext
	 * of reference feature by passing db reference name #TODO,till now its not
	 * decided how many data context a feature can have,in xml we have provided a
	 * support for multiple but in code implementation we are taking it as only one.
	 * 
	 * @param requestContext
	 *            : Request Context Object of current feature
	 * @return com.attunedlabs.datacontext.jaxb.DataContext
	 * @throws DataContextConfigurationException
	 */

	private com.attunedlabs.datacontext.jaxb.DataContext getDataContextObjectOfFeature(RequestContext requestContext,
			String name) throws DataContextConfigurationException {

		logger.debug(".getDataContextForFeature method of AbstractMetaModelBean");
		IDataContextConfigurationService dataContextConfigService = new DataContextConfigurationService();
		DataContextConfigurationUnit dataContextConfigurationUnit = dataContextConfigService
				.getDataContextConfiguration(requestContext);

		FeatureDataContext featureDataContext = (FeatureDataContext) dataContextConfigurationUnit.getConfigData();
		List<com.attunedlabs.datacontext.jaxb.DataContext> dataContextList = featureDataContext.getDataContexts()
				.getDataContext();

		for (com.attunedlabs.datacontext.jaxb.DataContext dataContext : dataContextList) {
			if (dataContext.getDbBeanRefName().equalsIgnoreCase(name)) {
				return dataContext;
			}
		}
		return null;
	}

	protected UpdateableDataContext getUpdateableDataContext(Exchange exchange) throws SQLException {
		logger.debug(".getUpdateableDataContextForCassandra method of AbstractMetaModelBean");
		Connection con = getConnection(dataSource, exchange);
		logger.debug("AbstractMetaModelBean.getLocalUpdateableDataContextForMysql got the connection : " + con);
		UpdateableDataContext dataContext = DataContextFactory.createJdbcDataContext(con);
		// dataContext.setIsInTransaction(true);
		return dataContext;
	}

	protected Table getTableForDataContext(DataContext datacontext, String tableName) {
		logger.debug(".getTableForDataContext method of AbstractMetaModelBean");
		Table table = datacontext.getTableByQualifiedLabel(tableName);
		return table;

	}

}
