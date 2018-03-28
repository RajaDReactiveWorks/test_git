package com.attunedlabs.integrationfwk.jdbcIntactivity.config.helper;

/**
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
  public static String elementToString(Node n) {

    String name = n.getNodeName();

    short type = n.getNodeType();

    if (Node.CDATA_SECTION_NODE == type) {
      return "<![CDATA[" + n.getNodeValue() + "]]&gt;";
    }

    if (name.startsWith("#")) {
      return "";
    }

    StringBuffer sb = new StringBuffer();
    sb.append('<').append(name);

    NamedNodeMap attrs = n.getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        sb.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append(
            "\"");
      }
    }

    String textContent = null;
    NodeList children = n.getChildNodes();

    if (children.getLength() == 0) {
      if ((textContent = n.getTextContent()) != null && !"".equals(textContent)) {
        sb.append(textContent).append("</").append(name).append('>');
        ;
      } else {
        sb.append("/>");
      }
    } else {
      sb.append('>');
      boolean hasValidChildren = false;
      for (int i = 0; i < children.getLength(); i++) {
        String childToString = elementToString(children.item(i));
        if (!"".equals(childToString)) {
          sb.append(childToString);
          hasValidChildren = true;
        }
      }

      if (!hasValidChildren && ((textContent = n.getTextContent()) != null)) {
        sb.append(textContent);
      }

      sb.append("</").append(name).append('>');
    }

    return sb.toString();
  }
}
