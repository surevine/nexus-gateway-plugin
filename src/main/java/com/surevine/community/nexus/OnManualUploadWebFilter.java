package com.surevine.community.nexus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.servlet.ShiroFilter;

/**
 * Injected {@link ShiroFilter}.
 */
@Singleton
public class OnManualUploadWebFilter extends OnUploadWebFilter {

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(req, res);
		
		final HttpServletRequest request = (HttpServletRequest) req;
		if ("POST".equals(request.getMethod()) && request.getRequestURI().equals("/nexus/service/local/artifact/maven/content")) {
			System.out.println("Manual filtering " +request.getMethod() +" " +request.getRequestURI());
			
			final BufferedReader br = new InputStreamReader(request.getInputStream());
			
			Enumeration e = request.getParameterNames();
			while(e.hasMoreElements()) {
				final String key = (String) e.nextElement();
				System.out.println(key);
			}
			/*final String path = request.getRequestURI().substring(REPOSITORIES.length());
			final Path artifact = Paths.get(System.getProperty("nexus-work"), "storage", path);
			
			if (path.endsWith("-securitylabel.xml")) {
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
					sendFile(guessedArtifact);
				}
			} else if (!endsWithAny(path, IGNORED_EXTENSIONS)) {
				sendFile(artifact);
			}*/
		}
	}
}
