package com.surevine.community.nexus;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

public class Pom {
	private String artifactId, groupId, version, packaging;
	
	public Pom(final String pom) {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
			      if (prefix == null) {
			          throw new IllegalArgumentException();
			       }
			       if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
			          return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			       }
			       if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
			          return XMLConstants.XML_NS_URI;
			       }
			       if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			          return "http://maven.apache.org/POM/4.0.0";
			       }
			       
			       if ("pom".equals(prefix)) {
			    	   return "http://maven.apache.org/POM/4.0.0";
			       } else {
			    	   return XMLConstants.NULL_NS_URI;
			       }
			}

			public String getPrefix(String namespaceURI) {
				throw new UnsupportedOperationException("Method not implemented.");
			}

			public Iterator<String> getPrefixes(String namespaceURI) {
				throw new UnsupportedOperationException("Method not implemented.");
			}
		});
		
		try {
			artifactId = xpath.evaluate("/pom:project/pom:artifactId",
					new InputSource(new StringReader(pom)));
		} catch (final XPathExpressionException e1) {
			e1.printStackTrace();
		}

		try {
			groupId = xpath.evaluate("/pom:project/pom:groupId",
					new InputSource(new StringReader(pom)));
		} catch (final XPathExpressionException e1) {
			e1.printStackTrace();
		}

		try {
			version = xpath.evaluate("/pom:project/pom:version",
					new InputSource(new StringReader(pom)));
		} catch (final XPathExpressionException e1) {
			e1.printStackTrace();
		}

		try {
			packaging = xpath.evaluate("/pom:project/pom:packaging",
					new InputSource(new StringReader(pom)));
		} catch (final XPathExpressionException e1) {
			e1.printStackTrace();
		}
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getVersion() {
		return version;
	}

	public String getPackaging() {
		return packaging;
	}
}
