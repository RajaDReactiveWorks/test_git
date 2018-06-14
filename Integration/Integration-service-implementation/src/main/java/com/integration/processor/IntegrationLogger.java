package com.integration.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import com.integration.exception.UnableToReadPropertiesFileException;

public class IntegrationLogger {
	private final static Logger logger = LoggerFactory.getLogger(IntegrationLogger.class.getName());
	private static ClassLoader classLoader = IntegrationLogger.class.getClassLoader();
	private final Cluster cluster;
	private final String keyspace;

	IntegrationLogger() throws UnableToReadPropertiesFileException {
		Properties prop = new Properties();
		InputStream is = classLoader.getResourceAsStream("cassebdraDBConfig.properties");
		try {
			prop.load(is);
		} catch (IOException e) {
			throw new UnableToReadPropertiesFileException(
					"uable to get cassebdraDBConfig.properties: " + e.getMessage(), e);
		}
		String host = prop.getProperty("host");
		cluster = Cluster.builder().addContactPoint(host).build();
		keyspace = prop.getProperty("keyspace");
	}

	/*public void updatetrackingStatus(String eventId, String status, String tableName, String column) {
		logger.debug("insdie updatetrackingStatus method");
		String query = "update " + tableName + " set status='" + status + "' where " + column + " = " + eventId;
		logger.debug("query is :" + query);
		Session session = cluster.connect(keyspace);
		session.execute(query);
		session.close();

	}*/

	public void updateFiletrackingStatus(String eventId, String status, String message) {
		logger.debug("insdie updatetrackingStatus method");
		if (message != null) {
			message = message.replace("{", "open brace").replace("'", "");
		}
		String query = "update  integrationlog.filetrackinglog  set status='" + status + "', errorMsg='"
				+ message + "' where filetrackerid = " + eventId;
		logger.debug("query is :" + query);
		Session session = cluster.connect(keyspace);
		session.execute(query);
		session.close();

	}

	public void updateintegrationflowtrackingStatus(String uuid, String status, String message) {
		logger.debug("insdie updateintegrationflowtrackingStatus method");
		if (message != null) {
			message = message.replace("{", "open brace").replace("'", "");
		}
		logger.debug("uuid  is : " + uuid);
		String query = "update  integrationlog.integrationflowtraking  set status='" + status + "',reason='" + message
				+ "' where uuid = '" + uuid + "'";
		logger.debug("query is :" + query);
		Session session = cluster.connect(keyspace);
		session.execute(query);
		session.close();

	}

	public String logInIntegrationFlowTracking(String eventId, String correlationId, int sequenceId, String destination,
			String enddtm, String entityType, String protocol, String reason, String document, String requesttype,
			String responsemessage, int retrycount, String siteId, String source, String startdtm, String status,
			String tenantId) {
		String uuid = UUIDs.timeBased().toString();
		String query = "insert into integrationlog.integrationflowtraking(uuid,eventId,correlationId,sequenceId,destination,enddtm,eventname,protocol,reason, "
				+ "requestmessage,requesttype, responsemessage,retrycount,siteid,source,startdtm,status,tenantid) "
				+ "values(" + "'" + uuid + "','" + eventId + "','" + correlationId + "'," + sequenceId + ",'"
				+ destination + "','" + enddtm + "','" + entityType + "','" + protocol + "','" + reason + "','"
				+ document + "','" + requesttype + "','" + responsemessage + "'," + retrycount + ",'" + siteId + "','"
				+ source + "','" + startdtm + "','" + status + "','" + tenantId + "')";
		logger.debug("query is :" + query);
		Session session = cluster.connect(keyspace);
		session.execute(query);
		session.close();
		return uuid;
	}

	/*
	 * public void logEventInCassandra(Exchange exchange) { logger.debug(
	 * "inside logEventInCassandra method"); Message message = exchange.getIn();
	 * JSONObject jsonEventFromKafka = message.getBody(JSONObject.class); try {
	 * LeapHeader leapHeader = (LeapHeader)
	 * exchange.getIn().getHeader(LeapHeaderConstant.LEAP_HEADER_KEY); String
	 * siteId = leapHeader.getSite(); String tenantId = leapHeader.getTenant();
	 * // JSONObject jsonEventFromKafka = new JSONObject(eventBody); JSONObject
	 * eventParamJson = jsonEventFromKafka.getJSONObject("EventParam"); String
	 * eventId = jsonEventFromKafka.getString("eventId"); logger.debug(
	 * "event param is :" + eventParamJson.toString()); String endPoint =
	 * eventParamJson.getString("EndPoint"); String fileType =
	 * eventParamJson.getString("FileType"); String entityType =
	 * eventParamJson.getString("entityType"); String fileName =
	 * eventParamJson.getString("FileName"); String sourceFolder =
	 * eventParamJson.getString("SourceFolder"); String http_url =
	 * eventParamJson.getString("EndPoint_URL"); JSONArray fileContentArray =
	 * eventParamJson.getJSONArray("content"); Timestamp timestamp = new
	 * Timestamp(System.currentTimeMillis()); Session session =
	 * cluster.connect(keyspace); if (endPoint.equalsIgnoreCase("rest")) {
	 * updatetrackingStatus(eventId, "success",
	 * "integrationlog.integrationflowtraking", "id"); String query =
	 * "insert into integrationlog.integrationflowtraking(id,tenantid, siteid,messageseq,eventname,source,destination,requestmessage,requesttype, "
	 * +
	 * "protocol,responsemessage, startdtm,enddtm,status,reason,loggingSeq,retryCount) "
	 * + "values('" + UUIDs.timeBased() + "','" + tenantId + "','" + siteId +
	 * "','" + eventId + "','" + eventId + "','" + http_url +
	 * "'ELASTIC_SUBSCRIBER','" + fileContentArray.toString() + "'," + fileType
	 * + "'KAFKA SUBSCRIBER'," + "'done','" + "" + "','" + "" + "','" +
	 * "success" + "','" + "" + "'," + 1 + "," + 0 + ")";
	 * session.execute(query); session.close();
	 * 
	 * } else { updatetrackingStatus(eventId, "success",
	 * "integrationlog.filetrackinglog", "filetrackerid"); String query =
	 * "insert into integrationlog.filetrackinglog(fileTrackerId ,endPoint,endpoint_url,fileName,sourceFolder, fileType,  receivedDTM, "
	 * +
	 * "status,sequenceId,correlationId,eventSequenceId) values (?,?,?,?,?,?,?,?,?,?,?)"
	 * ; if (fileContentArray.length() > 1) { PreparedStatement prepared =
	 * session.prepare(query); int j = 1; for (int i = 0; i <
	 * fileContentArray.length(); i++) { logger.debug("inside the if block :" +
	 * i);
	 * 
	 * logger.debug("splitted file is :" +
	 * IntegrationUtil.convertDocumentToString(((Document)
	 * fileContentArray.get(i)))); BoundStatement bound =
	 * prepared.bind(UUIDs.timeBased(), endPoint, http_url, fileName,
	 * sourceFolder, fileType, timestamp, "success", 2,
	 * UUID.fromString(eventId), j); session.execute(bound); j++; }
	 * session.close(); } else { logger.debug("inside the else block");
	 * session.execute(query, UUIDs.timeBased(), endPoint, http_url, fileName,
	 * sourceFolder, fileType, timestamp, "success", 0,
	 * UUID.fromString(eventId), 0); } }
	 * 
	 * } catch (JSONException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * }
	 */

}
