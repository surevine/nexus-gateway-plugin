package com.surevine.community.nexus;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SecurityLabel {

	private String classification, decorator;
	
	private String[] groups, countries;
	
	public SecurityLabel(final String label) {
		final XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			classification = xpath.evaluate("/securitylabel/classification",
					new InputSource(new StringReader(label)));
		} catch (final XPathExpressionException e1) {
			e1.printStackTrace();
		}
		try {
			decorator = xpath.evaluate("/securitylabel/decorator",
					new InputSource(new StringReader(label)));
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
		
		NodeList countryNodes;
		try {
			countryNodes = (NodeList) xpath.evaluate("/securitylabel/countries/country",
					new InputSource(new StringReader(label)), XPathConstants.NODESET);
			countries = new String[countryNodes.getLength()];
			for (int i = 0; i<countryNodes.getLength(); i++) {
				countries[i] = countryNodes.item(i).getTextContent();
			}
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
		
		NodeList groupNodes;
		try {
			groupNodes = (NodeList) xpath.evaluate("/securitylabel/groups/group",
					new InputSource(new StringReader(label)), XPathConstants.NODESET);
			groups = new String[groupNodes.getLength()];
			for (int i = 0; i<groupNodes.getLength(); i++) {
				groups[i] = groupNodes.item(i).getTextContent();
			}
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	public String getClassification() {
		return classification;
	}
	
	public String getDecorator() {
		return decorator;
	}
	
	public String[] getGroups() {
		return groups;
	}
	
	public String[] getCountries() {
		return countries;
	}
}
