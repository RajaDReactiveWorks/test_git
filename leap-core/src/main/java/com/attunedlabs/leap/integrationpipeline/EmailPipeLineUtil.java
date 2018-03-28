package com.attunedlabs.leap.integrationpipeline;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.w3c.dom.Document;

import com.attunedlabs.integrationfwk.activities.bean.ActivityConstant;
import com.attunedlabs.integrationfwk.activities.bean.EmailNotifier;
import com.attunedlabs.integrationfwk.activities.bean.EmailNotifierException;
import com.attunedlabs.integrationfwk.config.jaxb.MailAttachments;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.leap.util.LeapConfigurationUtil;



public class EmailPipeLineUtil {
	private Logger LOGGER = (Logger) LoggerFactory.getLogger(EmailPipeLineUtil.class.getName());
	/**
	 * method to set the exchange body in leapHeader and set the body node from
	 * the exchange and update the exchange with its value
	 * 
	 * @param exchange
	 * @throws EmailNotifierException
	 * @throws IOException
	 */

	public void setExchangeBodyToLeapHeader(Exchange exchange) throws EmailNotifierException, IOException {
		// get the current pipeActivity
		PipeActivity pipeActivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		String xmlInput = exchange.getIn().getBody(String.class);
		Message in = exchange.getIn();
		EmailNotifier emailNotifier = new EmailNotifier();
		// transforming the xmlString into Document
		MailAttachments mailAttachments = pipeActivity.getEmailNotifyActivity().getEmailNotification()
				.getMailAttachments();
		LOGGER.debug("XmlInput available: "+xmlInput);
		Document exchangeDocument = emailNotifier.generateDocumentFromString(xmlInput);
		if (mailAttachments != null) {
			attachIntoMail(pipeActivity, emailNotifier, exchangeDocument, in);
		}

		// fetching the bodyXpath declared in the pipelineConfiguration
		List<String> mailBodyXpath = new LinkedList<String>();
		mailBodyXpath.add(pipeActivity.getEmailNotifyActivity().getEmailNotification().getMailBodyXpath());
		List<Object> bodySet = emailNotifier.xpathProcessingOnInputXml(mailBodyXpath, exchangeDocument);
		// only single body in the list is available from above operation, thats
		// why using bodySet.get(0)
		String body = bodySet.get(0).toString();

		// doing this because, if any attachment given in email pipeline
		// activity along with body is there in the exchange body
		xmlInput = exchange.getIn().getBody(String.class);
		// Setting exchange body into LeapHeader
		LeapConfigurationUtil leapConfigUtil = new LeapConfigurationUtil();
		leapConfigUtil.setIntegrationPipeContext(xmlInput, exchange);
		// setting the body into exchange
		exchange.getIn().setBody(body);
	}

	/**
	 * 
	 * Method to attach file in mails which will be called only when attachment
	 * is available in the pipeline
	 * 
	 * @param pipeActivity
	 * @param emailNotifier
	 * @param exchangeDocument
	 * @param in
	 * @throws EmailNotifierException
	 */
	public void attachIntoMail(PipeActivity pipeActivity, EmailNotifier emailNotifier, Document exchangeDocument,
			Message in) throws EmailNotifierException {
		// fetching the Attachment related Xpath declared in the
		// pipelineconfiguration
		List<String> attachmentFileXpath = new LinkedList<String>();
		List<String> attachementFileName = new LinkedList<String>();
		List<String> attachmentFileFormat = new LinkedList<String>();
		attachmentFileXpath.add(pipeActivity.getEmailNotifyActivity().getEmailNotification().getMailAttachments()
				.getMailAttachment().getMailAttachmentXpath());
		attachementFileName.add(pipeActivity.getEmailNotifyActivity().getEmailNotification().getMailAttachments()
				.getMailAttachment().getMailAttachmentNameXpath());
		attachmentFileFormat.add(pipeActivity.getEmailNotifyActivity().getEmailNotification().getMailAttachments()
				.getMailAttachment().getMailAttachmentFormatXpath());

		// processing the xpath with the xml in the exchange and getting values
		// in List Objects
		List<Object> attachmentFileSet = emailNotifier.xpathProcessingOnInputXml(attachmentFileXpath, exchangeDocument);
		List<Object> attachmentFileNameSet = emailNotifier.xpathProcessingOnInputXml(attachementFileName,
				exchangeDocument);
		List<Object> attachmentFileFormatSet = emailNotifier.xpathProcessingOnInputXml(attachmentFileFormat,
				exchangeDocument);

		// Looping through the number of attachments present and attaching to
		// the mail
		for (int i = 0; i < attachmentFileSet.size(); i++) {
			byte[] decodedData = Base64Utils.decode(attachmentFileSet.get(i).toString().getBytes());
			String fileName = attachmentFileNameSet.get(i).toString();
			String fileFormat = attachmentFileFormatSet.get(i).toString();
			String fileNameToAttach = fileName + fileFormat;
			
			// attaching the file with its file name
			in.addAttachment(fileNameToAttach, new DataHandler(new ByteArrayDataSource(decodedData)));
		}
	}

	/**
	 * method to get the leap header into the exchange
	 * 
	 * @param exchange
	 * @throws EmailNotifierException
	 */
	public void getLeapHeaderToExchangeBody(Exchange exchange) throws EmailNotifierException {
		LeapConfigurationUtil leapConfigurationUtil = new LeapConfigurationUtil();
		leapConfigurationUtil.getIntegrationPipeContext(exchange);
	}// end of method

}