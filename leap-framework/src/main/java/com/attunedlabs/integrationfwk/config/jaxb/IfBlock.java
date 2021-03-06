//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.19 at 05:01:20 PM IST 
//


package com.attunedlabs.integrationfwk.config.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="ConditionalExpression" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ConditionalValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/pipeline}Pipeline" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/integration-fwk-Supporting}InnerIfBlock" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/integration-fwk-Supporting}InnerElseIfBlock" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://attunedlabs.com/internal/integration-fwk-Supporting}InnerElseBlock" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="xpath"/>
 *             &lt;enumeration value="mvel"/>
 *             &lt;enumeration value="groovy"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "conditionalExpression",
    "conditionalValue",
    "pipeline",
    "innerIfBlock",
    "innerElseIfBlock",
    "innerElseBlock"
})
public class IfBlock
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "ConditionalExpression", required = true)
    protected String conditionalExpression;
    @XmlElement(name = "ConditionalValue")
    protected String conditionalValue;
    @XmlElement(name = "Pipeline", namespace = "http://attunedlabs.com/internal/pipeline")
    protected Pipeline pipeline;
    @XmlElement(name = "InnerIfBlock", namespace = "http://attunedlabs.com/internal/integration-fwk-Supporting")
    protected InnerIfBlock innerIfBlock;
    @XmlElement(name = "InnerElseIfBlock", namespace = "http://attunedlabs.com/internal/integration-fwk-Supporting")
    protected List<InnerElseIfBlock> innerElseIfBlock;
    @XmlElement(name = "InnerElseBlock", namespace = "http://attunedlabs.com/internal/integration-fwk-Supporting")
    protected InnerElseBlock innerElseBlock;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the conditionalExpression property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConditionalExpression() {
        return conditionalExpression;
    }

    /**
     * Sets the value of the conditionalExpression property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConditionalExpression(String value) {
        this.conditionalExpression = value;
    }

    /**
     * Gets the value of the conditionalValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConditionalValue() {
        return conditionalValue;
    }

    /**
     * Sets the value of the conditionalValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConditionalValue(String value) {
        this.conditionalValue = value;
    }

    /**
     * Gets the value of the pipeline property.
     * 
     * @return
     *     possible object is
     *     {@link Pipeline }
     *     
     */
    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Sets the value of the pipeline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pipeline }
     *     
     */
    public void setPipeline(Pipeline value) {
        this.pipeline = value;
    }

    /**
     * Gets the value of the innerIfBlock property.
     * 
     * @return
     *     possible object is
     *     {@link InnerIfBlock }
     *     
     */
    public InnerIfBlock getInnerIfBlock() {
        return innerIfBlock;
    }

    /**
     * Sets the value of the innerIfBlock property.
     * 
     * @param value
     *     allowed object is
     *     {@link InnerIfBlock }
     *     
     */
    public void setInnerIfBlock(InnerIfBlock value) {
        this.innerIfBlock = value;
    }

    /**
     * Gets the value of the innerElseIfBlock property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the innerElseIfBlock property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInnerElseIfBlock().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InnerElseIfBlock }
     * 
     * 
     */
    public List<InnerElseIfBlock> getInnerElseIfBlock() {
        if (innerElseIfBlock == null) {
            innerElseIfBlock = new ArrayList<InnerElseIfBlock>();
        }
        return this.innerElseIfBlock;
    }

    /**
     * Gets the value of the innerElseBlock property.
     * 
     * @return
     *     possible object is
     *     {@link InnerElseBlock }
     *     
     */
    public InnerElseBlock getInnerElseBlock() {
        return innerElseBlock;
    }

    /**
     * Sets the value of the innerElseBlock property.
     * 
     * @param value
     *     allowed object is
     *     {@link InnerElseBlock }
     *     
     */
    public void setInnerElseBlock(InnerElseBlock value) {
        this.innerElseBlock = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
