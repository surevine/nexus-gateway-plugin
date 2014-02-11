package com.surevine.nexus;

import static org.junit.Assert.assertEquals;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

import com.surevine.community.nexus.Pom;
import com.surevine.community.nexus.SecurityLabel;

public class OnUploadWebFilterTest {

	@Test
	public void testLabelSuccess() throws XPathExpressionException {
		final String labelXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><securitylabel><classification>COMMERCIAL</classification><decorator>IN CONFIDENCE</decorator><groups><group>STAFF</group></groups><countries><country>UK</country><country>FR</country></countries></securitylabel>";

		final SecurityLabel label = new SecurityLabel(labelXml);

		assertEquals("COMMERCIAL", label.getClassification());
		assertEquals("IN CONFIDENCE", label.getDecorator());
		
		assertEquals(1, label.getGroups().length);
		assertEquals("STAFF", label.getGroups()[0]);
		
		assertEquals(2, label.getCountries().length);
		assertEquals("UK", label.getCountries()[0]);
		assertEquals("FR", label.getCountries()[1]);
	}

	@Test
	public void testPomSuccess() throws XPathExpressionException {
		final String pomXml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">	<modelVersion>4.0.0</modelVersion>	<parent>		<groupId>org.sonatype.nexus.plugins</groupId>		<artifactId>nexus-plugins</artifactId>		<version>2.6.3-01</version>	</parent>	<groupId>com.surevine.nexus</groupId>	<artifactId>securitylabels</artifactId>	<version>0.0.2-SNAPSHOT</version>	<name>securitylabels</name>	<packaging>nexus-plugin</packaging>	<url>http://maven.apache.org</url>	<properties>		<pluginName>Surevine Security Label Plugin</pluginName>		<pluginDescription>Adds security label authorisation for repository			content.</pluginDescription>	</properties>	<repositories>		<repository>			<id>dev-releases</id>			<url>http://10.66.2.218:8081/content/repositories/releases</url>			<releases>				<enabled>true</enabled>			</releases>			<snapshots>				<enabled>false</enabled>			</snapshots>		</repository>		<repository>			<id>dev-snapshots</id>			<url>http://10.66.2.218:8081/content/repositories/snapshots</url>			<releases>				<enabled>true</enabled>			</releases>			<snapshots>				<enabled>false</enabled>			</snapshots>		</repository>	</repositories>	<dependencies>		<dependency>			<groupId>org.sonatype.nexus</groupId>			<artifactId>nexus-plugin-api</artifactId>			<scope>provided</scope>		</dependency>		<dependency>			<groupId>org.apache.shiro</groupId>			<artifactId>shiro-web</artifactId>		</dependency>		<dependency>			<groupId>org.sonatype.nexus.plugins</groupId>			<artifactId>nexus-restlet1x-plugin</artifactId>			<type>${nexus-plugin.type}</type>			<scope>provided</scope>		</dependency>	</dependencies>	<build>		<plugins>			<plugin>				<groupId>org.sonatype.nexus</groupId>				<artifactId>nexus-plugin-bundle-maven-plugin</artifactId>				<extensions>true</extensions>			</plugin>			<plugin>				<groupId>org.codehaus.mojo</groupId>				<artifactId>build-helper-maven-plugin</artifactId>				<executions>					<execution>						<id>attach-artifacts</id>						<phase>package</phase>						<goals>							<goal>attach-artifact</goal>						</goals>						<configuration>							<artifacts>								<artifact>									<file>securitylabel.xml</file>									<type>xml</type>									<classifier>securitylabel</classifier>								</artifact>							</artifacts>						</configuration>					</execution>				</executions>			</plugin>		</plugins>	</build>	<distributionManagement>		<!-- Publish versioned releases here -->		<repository>			<id>dev-releases</id>			<name>Dev releases</name>			<url>http://10.66.2.218:8081/nexus/content/repositories/releases</url>		</repository>		<!-- Publish snapshots here -->		<snapshotRepository>			<id>dev-snapshots</id>			<name>Dev snapshots</name>			<url>http://10.66.2.218:8081/nexus/content/repositories/snapshots</url>		</snapshotRepository>	</distributionManagement></project>";

		final Pom pom = new Pom(pomXml);

		assertEquals("com.surevine.nexus", pom.getGroupId());
		assertEquals("securitylabels", pom.getArtifactId());
		assertEquals("nexus-plugin", pom.getPackaging());
		assertEquals("0.0.2-SNAPSHOT", pom.getVersion());
	}
}
