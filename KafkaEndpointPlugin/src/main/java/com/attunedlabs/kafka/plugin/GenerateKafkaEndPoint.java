package com.attunedlabs.kafka.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "kafkaEndpointPlugin", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateKafkaEndPoint extends AbstractMojo {

	@Parameter(property = "project", defaultValue = "${project}", required = true, readonly = false)
	private MavenProject project;

	final static String FEATURE_FILE_PATTERN = "sac-key2act-globalFeatureservice.xml";
	final static String REPLACE_KAFKA_ENDPOINT_FILE = "sacSetupServiceRoutImpl.xml";
	final static String KAFKA_BROKER_HOST_PORT_URL_KEY="$kafkaBrokerHostPort$";
	final static String KAFKA_TOPIC_NAME_URL_KEY="$topicName$";
	final static String KAFKA_GROUP_ID_URL_KEY="$groupId$";
	final static String KAFKA_CLIENT_ID_URL_KEY="$clientId$";
	final static String SSL_TRUSTSTORE_LOCATION_URL_KEY="$sslTruststoreLocation$";
	final static String SSL_TRUSTSTORE_PASSWORD_URL_KEY="$sslTruststorePassword$";
	final static String KAFKA_ENDPOINT_CONFIG_TAG="KafkaEndpointConfig";
	final static String KAFKA_BROKER_HOST_PORT_CONFIG_KEY="brokerHostPort";
	final static String KAFKA_TOPIC_NAME_CONFIG_KEY="topicName";
	final static String KAFKA_GROUP_ID_CONFIG_KEY="groupId";
	final static String KAFKA_CLIENT_ID_CONFIG_KEY="clientId";
	final static String SSL_TRUSTSTORE_LOCATION_CONFIG_KEY="sslTruststoreLocation";
	final static String SSL_TRUSTSTORE_PASSWORD_CONFIG_KEY="sslTruststorePassword";
	final static String CAMELFILE_PATH = "/META-INF/spring";
	final static String KAFKA_ENDPOINT_URI = "kafka:$kafkaBrokerHostPort$?topic=$topicName$&amp;groupId=$groupId$&amp;clientId=$clientId$&amp;sslTruststoreLocation=$sslTruststoreLocation$&amp;sslTruststorePassword=$sslTruststorePassword$&amp;securityProtocol=SSL&amp;autoCommitEnable=false&amp;metadataMaxAgeMs=30000000&amp;serializerClass=kafka.serializer.StringEncoder";	
	final static String ROUTE_DEFINITION = "<route id=\"kafka-consumer-route\">"+
			"			<from uri=\"$kafkaEndpoint$\"/>"+
			"			<to uri=\"bean:dataTransform?method=marshalXmltoJson\"/>				"+
			"				<log message=\"log message XML : ${body}\"/>"+
			"			 	<setHeader headerName=\"endpointType\">"+
			"				<simple>HTTP-XML</simple>"+
			"				</setHeader>				"+
			"				 <to uri=\"direct:baseEntry\"/>"+
			"		</route>";
	
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		List<Resource> resourceList = project.getResources();
		String directory = null;
		for (Resource resource : resourceList) {
			directory = resource.getDirectory();
		}
		List<String> listOfEleClassPath = null;
		try {
			listOfEleClassPath = project.getCompileClasspathElements();

		} catch (DependencyResolutionRequiredException e1) {
			throw new MojoExecutionException("Unable to resolve the require depenedncy error", e1);
		}
		File resFileToRead = new File(directory);
		File resFileToWrite = new File(directory + CAMELFILE_PATH);		
		try {
			getFeatureInfo(resFileToRead,resFileToWrite);

		} catch (Exception e) {
			throw new MojoExecutionException("Unable to generate the cxf end point for feature", e);
		}

	}

	private void getFeatureInfo(File fileToRead,File fileToWrite) throws GenerateEndpointException {
		String[] files = fileToRead.list();
		if (files.length != 0) {
			for (String aFile : files) {
				if (aFile.endsWith(FEATURE_FILE_PATTERN)) {
					String filepath = fileToRead + "/" + aFile;
					System.out.println("file to read : "+filepath);
					boolean isKafkaEndpointConfigDefined=checkKafkaConfigExist(filepath,KAFKA_ENDPOINT_CONFIG_TAG);
					if(isKafkaEndpointConfigDefined){
					String kafkaBrokerHostAndPort = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,KAFKA_BROKER_HOST_PORT_CONFIG_KEY);
					String kafkaTopicName = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,KAFKA_TOPIC_NAME_CONFIG_KEY);
					String kafkaGroupId = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,KAFKA_GROUP_ID_CONFIG_KEY);
					String kafkaClientId = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,KAFKA_CLIENT_ID_CONFIG_KEY);
					String sslTruststoreLocation = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,SSL_TRUSTSTORE_LOCATION_CONFIG_KEY);
					String sslTruststorePassword = getFeatureAttribute(filepath,KAFKA_ENDPOINT_CONFIG_TAG,SSL_TRUSTSTORE_PASSWORD_CONFIG_KEY);
					String newKafkaEndpointURI = KAFKA_ENDPOINT_URI.replace(KAFKA_BROKER_HOST_PORT_URL_KEY,kafkaBrokerHostAndPort)
							.replace(KAFKA_TOPIC_NAME_URL_KEY,kafkaTopicName)
							.replace(KAFKA_GROUP_ID_URL_KEY, kafkaGroupId).replace(KAFKA_CLIENT_ID_URL_KEY, kafkaClientId).replace(SSL_TRUSTSTORE_LOCATION_URL_KEY, sslTruststoreLocation).replace(SSL_TRUSTSTORE_PASSWORD_URL_KEY, sslTruststorePassword);
					String newRouteDefinition = ROUTE_DEFINITION
							.replace("$kafkaEndpoint$", newKafkaEndpointURI);
					String[] writefiles = fileToWrite.list();
					if (writefiles.length == 0) {
					} else {
						for (String wFile : writefiles) {
							if (wFile.endsWith(REPLACE_KAFKA_ENDPOINT_FILE)) {
								String writeFilePath = fileToWrite + "\\" + wFile;
								writeToFile(writeFilePath, newRouteDefinition);
							}
						} // end of for loop which write target xml
					}
				}
			}
		}
		}//end of if(isKafkaEndpointConfigDefined)

	}//end of method
	
	private boolean checkKafkaConfigExist(String filepath, String featureTag) throws GenerateEndpointException{
		File xmlFile = new File(filepath);
		Document doc = null;
		boolean isKafkaConfigExist=false;
		try {
			doc = readXmlDocument(xmlFile);
		} catch (GenerateEndpointException e) {
			throw new GenerateEndpointException("Unable to generate Document Object in custom plugin", e);
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName(featureTag);
			if(nodeList !=null && !(nodeList.getLength()<=0))
				isKafkaConfigExist=true;
		}
		return isKafkaConfigExist;
	}

	/**
	 * This method is used to get the attribute value required to create cxf end point from feature.xml
	 * @param filepath : file path from where xml need to load
	 * @param featureTag : feature element in String
	 * @param featureAttribute : feature attribute in String
	 * @return String : required attribute
	 * @throws GenerateEndpointException
	 */
	private String getFeatureAttribute(String filepath, String featureTag, String featureAttribute)
			throws GenerateEndpointException {
		String reqAttributeValue = null;
		File xmlFile = new File(filepath);
		Document doc = null;
		try {
			doc = readXmlDocument(xmlFile);
		} catch (GenerateEndpointException e) {
			throw new GenerateEndpointException("Unable to generate Document Object in custom plugin", e);
		}
		if (doc != null) {
			NodeList nodeList = doc.getElementsByTagName(featureTag);
			for (int temp = 0; temp < nodeList.getLength(); temp++) {
				Node node = nodeList.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					System.out.println(featureAttribute + " : " + element.getAttribute(featureAttribute));
					reqAttributeValue = element.getAttribute(featureAttribute);
				}
			} // end of for loop
		} // end of if(doc!=null)
		return reqAttributeValue;
	}

	/**
	 * This method is used to write cxf end point and route definition in xml file
	 * @param filepath : path of the fine which need to write
	 * @param cxfEndpoint : cxf endpoint in String format
	 * @param routeDefinition : route definition for cxf end point
	 * @throws GenerateEndpointException
	 */
	private void writeToFile(String filepath, String routeDefinition)
			throws GenerateEndpointException {
		String matchSTring = "</routeContext>";
		String xmlDataInString = null;
		String replceData = null;
		StringBuilder builder = new StringBuilder();
		File xmlFile = new File(filepath);
		//Document doucument=readXmlDocument(xmlFile);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(xmlFile));
			while ((xmlDataInString = in.readLine()) != null) {
				builder.append(xmlDataInString);
			}
			boolean bool=builder.toString().contains("<route id=\"kafka-consumer-route\">");
			if(bool){
				System.out.println("route definition already exist");
			}else{
				System.out.println("route definition doesnot exist");
			replceData = builder.toString().replace("</routeContext>",
					routeDefinition + "</routeContext>" );

			BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
			out.write(replceData);
			out.flush();
			out.close();
			}
		} catch (IOException e1) {
			throw new GenerateEndpointException("Unable to Read/Write to xml file : " + filepath);
		}

		
	}

	/**
	 * This method is used to search for a pattern in text 
	 * @param text : text where need to search for pattern
	 * @param regex : pattern to be searched for
	 * @return int : starting index if pattern found in text
	 */
	public int printMatches(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		int startIndex = 0;
		// Check all occurrences
		while (matcher.find()) {
			startIndex = matcher.start();
					}
		return startIndex;
	}

	/**
	 * This method is used to read xml file
	 * @param xmlFile : File Object
	 * @return : Document Object
	 * @throws GenerateEndpointException
	 */
	private Document readXmlDocument(File xmlFile) throws GenerateEndpointException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			try {
				try {
					document = builder.parse(xmlFile);
				} catch (IOException e) {
					throw new GenerateEndpointException("Unable to file the file : " + xmlFile, e);
				}
			} catch (SAXException e) {
				throw new GenerateEndpointException("Unable to parse xml file : " + xmlFile, e);

			}
		} catch (ParserConfigurationException e) {
			throw new GenerateEndpointException("Unable to create Document Builder Object in plugin", e);

		}

		return document;
	}

}
