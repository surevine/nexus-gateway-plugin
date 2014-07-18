package com.surevine.community.nexus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class OnMavenUploadWebFilter extends OnUploadWebFilter {
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		System.out.println("Filtering");
		
		chain.doFilter(req, res);
		
		final HttpServletRequest request = (HttpServletRequest) req;
		System.out.println("Maven filtering " +request.getMethod() +" " +request.getRequestURI());
		if ("PUT".equals(request.getMethod()) && request.getRequestURI().startsWith(REPOSITORIES)) {
			final String path = request.getRequestURI().substring(REPOSITORIES.length());
			final Path artifact = Paths.get(System.getProperty("nexus-work"), "storage", path);
			System.out.println("Artefact is in correct path: "+path);
			
			if (path.endsWith("-securitylabel.xml")) {
				System.out.println("Found security label");
				
				// Find and send to gateway all non-pom/hash/metadata.xml files of their most recent version.
				
				// List all sendable files (not maven-metadata.xml, sha1, md5, -security-label.xml, etc)
				final String[] artifacts = artifact.getParent().toFile().list(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return !endsWithAny(name, IGNORED_EXTENSIONS) && !name.endsWith("-securitylabel.xml");
					}
				});
				
				final Map<String, String> groupByExtension = new HashMap<>();
				for (final String filename : artifacts) {
					System.out.println("Processing: "+filename);
					final String extension = filename.substring(filename.lastIndexOf('.'));
					if (groupByExtension.containsKey(extension)) {
						if (groupByExtension.get(extension).compareTo(filename) > 1) {
							groupByExtension.put(extension, filename);
						}
					} else {
						groupByExtension.put(extension, filename);
					}
				}
				
				for (final String key : groupByExtension.keySet()) {
					final Path guessedArtifact = Paths.get(artifact.getParent().toString(), groupByExtension.get(key));
					System.out.println("Sending "+guessedArtifact);
					sendFile(guessedArtifact);
				}
			} else if (!endsWithAny(path, IGNORED_EXTENSIONS)) {
				System.out.println("Sending[2]: "+artifact);
				sendFile(artifact);
			}
		}
	}
}
