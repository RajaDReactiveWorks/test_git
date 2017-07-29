//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.29 at 07:41:38 PM IST 
//


package com.getusroi.integrationfwk.config.jaxb;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MailAttachments" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="MailAttachment">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="mailAttachmentXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mailAttachmentNameXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="mailAttachmentFormatXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="recepientIdXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mailSubjectXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="mailBodyXpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hasAttachments" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mailAttachments"
})
public class EmailNotification
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "MailAttachments")
    protected MailAttachments mailAttachments;
    @XmlAttribute(name = "recepientIdXpath")
    protected String recepientIdXpath;
    @XmlAttribute(name = "mailSubjectXpath")
    protected String mailSubjectXpath;
    @XmlAttribute(name = "mailBodyXpath")
    protected String mailBodyXpath;
    @XmlAttribute(name = "hasAttachments")
    protected String hasAttachments;

    /**
     * Gets the value of the mailAttachments property.
     * 
     * @return
     *     possible object is
     *     {@link MailAttachments }
     *     
     */
    public MailAttachments getMailAttachments() {
        return mailAttachments;
    }

    /**
     * Sets the value of the mailAttachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailAttachments }
     *     
     */
    public void setMailAttachments(MailAttachments value) {
        this.mailAttachments = value;
    }

    /**
     * Gets the value of the recepientIdXpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecepientIdXpath() {
        return recepientIdXpath;
    }

    /**
     * Sets the value of the recepientIdXpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecepientIdXpath(String value) {
        this.recepientIdXpath = value;
    }

    /**
     * Gets the value of the mailSubjectXpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailSubjectXpath() {
        return mailSubjectXpath;
    }

    /**
     * Sets the value of the mailSubjectXpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailSubjectXpath(String value) {
        this.mailSubjectXpath = value;
    }

    /**
     * Gets the value of the mailBodyXpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailBodyXpath() {
        return mailBodyXpath;
    }

    /**
     * Sets the value of the mailBodyXpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailBodyXpath(String value) {
        this.mailBodyXpath = value;
    }

    /**
     * Gets the value of the hasAttachments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHasAttachments() {
        return hasAttachments;
    }

    /**
     * Sets the value of the hasAttachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHasAttachments(String value) {
        this.hasAttachments = value;
    }

}