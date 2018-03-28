package com.attunedlabs.concreatewsdl.plugin;

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

@Mojo(name = "concreateWsdlPlugin", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateWSDLEndPoint extends AbstractMojo {

	@Parameter(property = "project", defaultValue = "${project}", required = true, readonly = false)
	private MavenProject project;

	final static String FEATURE_FILE_PATTERN = "featureservice.xml";
	final static String REPLACE_CXF_ENDPOINT_FILE = "Execution.xml";
	final static String CAMELFILE_PATH = "/OSGI-INF/blueprint";
	final static String CXF_ENDPOINT = "<camelcxf:cxfEndpoint id=\"$endpointname$\" address=\"http://0.0.0.0:9070/ecomm/soap/$featureurl$\" serviceClass=\"$intefacename$\" />";
	final static String ROUTE_DEFINITION = "<route id=\"$routename$\">" + "\n"
			+ "<from uri=\"cxf:bean:$endpointname$?dataFormat=PAYLOAD\"/>" + "\n"
			+ "<setHeader headerName=\"featuregroup\">" + "<simple>$featuregroup$</simple>" + "</setHeader>" + "\n"
			+ "<setHeader headerName=\"feature\">" + "<simple>$featurename$</simple>" + "</setHeader>" + "\n"
			+ "<setHeader headerName=\"endpointType\">" + "<simple>CXF-ENDPOINT</simple>" + "</setHeader>" + "\n"
			+ "<marshal ref=\"xmljsonWithOptions1\" />" + "\n" + "<convertBodyTo type=\"java.lang.String\"/>" + "\n"
			+ "<to uri=\"bean:dataTransform?method=transformRequestData\"/>" + "\n" + "<to uri=\"direct-vm:baseEntry\"/>"
			+ "\n" + "<unmarshal ref=\"xmljsonWithOptions1\"/>" + "\n" + "</route>";
	

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
		GenerateWSDLEndPoint substituteData = new GenerateWSDLEndPoint();
		try {
			substituteData.getFeatureInfo(resFileToRead, resFileToWrite);

		} catch (Exception e) {
			throw new MojoExecutionException("Unable to generate the cxf end point for feature", e);
		}

	}

	private void getFeatureInfo(File fileToRead, File fileToWrite) throws GenerateEndpointException {
		String[] files = fileToRead.list();
		if (files.length != 0) {
			for (String aFile : files) {
				if (aFile.endsWith(FEATURE_FILE_PATTERN)) {
					String filepath = fileToRead + "/" + aFile;
					String featuregroup;

					featuregroup = getFeatureAttribute(filepath, "Features", "featureGroup");
					String featurename = getFeatureAttribute(filepath, "Feature", "featureName");
					String fqInterfaceName = getFeatureAttribute(filepath, "Feature", "interfaceName");
					String newcxfEndpoint = CXF_ENDPOINT.replace("$endpointname$", featuregroup + "_" + featurename)
							.replace("$featureurl$", featuregroup + "/" + featurename)
							.replace("$intefacename$", fqInterfaceName);
					String newRouteDefinition = ROUTE_DEFINITION
							.replace("$routename$", featuregroup + "-" + featurename + "-route")
							.replace("$endpointname$", featuregroup + "_" + featurename)
							.replace("$featuregroup$", featuregroup).replace("$featurename$", featurename);
					String[] writefiles = fileToWrite.list();
					if (writefiles.length == 0) {
					} else {
						for (String wFile : writefiles) {
							if (wFile.endsWith(REPLACE_CXF_ENDPOINT_FILE)) {
								String writeFilePath = fileToWrite + "/" + wFile;
								writeToFile(writeFilePath, newcxfEndpoint, newRouteDefinition);
							}
						} // end of for loop which write target xml
					}
				}
			} // end of for loop which read feature.xml
		} // end of if (files.length != 0)

	}//end of method

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
	private void writeToFile(String filepath, String cxfEndpoint, String routeDefinition)
			throws GenerateEndpointException {
		String matchSTring = "</routeContext>";
		String xmlDataInString = null;
		String replceData = null;
		StringBuilder builder = new StringBuilder();
		File xmlFile = new File(filepath);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(xmlFile));
			while ((xmlDataInString = in.readLine()) != null) {
				builder.append(xmlDataInString);
			}
			replceData = builder.toString().replace("</camelContext>",
					routeDefinition + "</camelContext>" + cxfEndpoint);

			BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
			out.write(replceData);
			out.flush();
			out.close();
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
