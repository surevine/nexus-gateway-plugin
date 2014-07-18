package com.surevine.community.nexus;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.shiro.web.servlet.ShiroFilter;

import com.google.common.base.Joiner;

/**
 * Injected {@link ShiroFilter}.
 */
public abstract class OnUploadWebFilter implements Filter {
	
	protected static final String REPOSITORIES = "/nexus/content/repositories/";
	
	protected String[] IGNORED_EXTENSIONS = new String[] {
			".sha1",
			".md5",
			"maven-metadata.xml"
	};
	
	protected static final Collection<Path> AWAITING_POM = Collections.synchronizedSet(new HashSet<Path>());

	public abstract void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException;
	
	protected void sendFile(final Path artifact) {
		System.out.println("Processing artifact: " +artifact);
		
    	final String[] repositories = NexusGatewayProperties.get(
    			NexusGatewayProperties.NEXUS_REPOSITORIES).split(",");
    	
		final String artifactStr = artifact.toString();
		final String repositoryTmp = artifactStr.substring(artifactStr.indexOf("/storage/") +9);
		final String repository = repositoryTmp.substring(0, repositoryTmp.indexOf("/"));
		
		// Only pick up changes for defined repositories.
		if (Arrays.asList(repositories).contains(repository)) {
			try {
				final File[] labelPaths = artifact.getParent().toFile().listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith("-securitylabel.xml");
					}
				});
				
				Arrays.sort(labelPaths);
				
				if (labelPaths.length == 0) {
					System.out.println("Uploaded artifact has no security label. Ignoring.");
					return; // Warning. Return statement in middle of method!
				}
				
				// Take the last of the sorted label paths. Maybe this is correct.
				final Path labelPath = labelPaths[labelPaths.length - 1].toPath();
				
				final File[] pomFiles = artifact.getParent().toFile().listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".pom");
					}
				});
				
				if (pomFiles.length == 0) {
					System.out.println("Ack. No pom found. Sssh...");
					final File[] foundFiles = artifact.getParent().toFile().listFiles();
					System.out.println("Found "+foundFiles.length+" files in "+artifact.getParent());
					for (int i=0; i < foundFiles.length; i++) {
						System.out.println("    "+foundFiles[i]);
					}
					AWAITING_POM.add(artifact);
					return; // Warning. Return statement in middle of method!
				}
				
				final Path pomPath = pomFiles[0].toPath();
				
				// Read metadata
				final String labelXml = Joiner.on("").join(Files.readAllLines(labelPath, Charset.forName("UTF-8")));
				final SecurityLabel label = new SecurityLabel(labelXml);
				
				// Read pom information
				final String pomXml = Joiner.on("").join(Files.readAllLines(pomPath, Charset.forName("UTF-8")));
				final Pom pom = new Pom(pomXml);
				
				final Map<String, String> properties = new HashMap<String, String>();
				properties.put("groupId", pom.getGroupId());
				properties.put("artifactId", pom.getArtifactId());
				properties.put("version", pom.getVersion());
				properties.put("packaging", pom.getPackaging());
				properties.put("classification", label.getClassification());
				properties.put("decorator", label.getDecorator());
				properties.put("groups", Joiner.on(",").join(label.getGroups()));
				properties.put("countries", Joiner.on(",").join(label.getCountries()));
				properties.put("source_type", "NEXUS");
				properties.put("repository", repository);
				properties.put("name", artifact.getFileName().toString());
				
				final StringBuilder metadata = new StringBuilder();
				metadata.append("{");
				metadata.append(String.format("\"repository\": \"%s\",", properties.get("repository")));
				metadata.append(String.format("\"groupId\": \"%s\",", properties.get("groupId")));
				metadata.append(String.format("\"artifactId\": \"%s\",", properties.get("artifactId")));
				metadata.append(String.format("\"version\": \"%s\",", properties.get("version")));
				metadata.append(String.format("\"packaging\": \"%s\",", properties.get("packaging")));
				metadata.append(String.format("\"classification\": \"%s\",", properties.get("classification")));
				metadata.append(String.format("\"decorator\": \"%s\",", properties.get("decorator")));
				metadata.append(String.format("\"groups\": \"%s\",", properties.get("groups")));
				metadata.append(String.format("\"countries\": \"%s\",", properties.get("countries")));
				metadata.append(String.format("\"name\": \"%s\",", properties.get("name")));
				metadata.append("\"source_type\": \"NEXUS\"");
				metadata.append("}");
				
				System.out.println("Metadata: " +metadata.toString());


                // Create tar.gz in temporary directory
                final Path tmp = Paths.get("/tmp/nexus-upload", UUID.randomUUID().toString());
                final Path tarFile = Paths.get(tmp.toString(), tmp.getFileName().toString() +".tar.gz");
                final Path metadataPath = Paths.get(tmp.toString(), ".metadata.json");
                final Path artifactPath = Paths.get(tmp.toString(), artifact.getFileName().toString());

                try {
                    Files.createDirectories(tmp);

                    // register for deletion on normal JVM termination

                    artifactPath.toFile().deleteOnExit();
                    metadataPath.toFile().deleteOnExit();
                    tarFile.toFile().deleteOnExit();
                    tmp.toFile().deleteOnExit();

                    Files.copy(artifact, artifactPath);
                    Files.write(metadataPath, metadata.toString().getBytes(Charset.forName("UTF-8")));

                    // Tarball.
                    try {
                        Runtime.getRuntime().exec(
                                new String[] {"tar", "cvzf", tarFile.toString(),
                                        metadataPath.getFileName().toString(),
                                        artifactPath.getFileName().toString()},
                                new String[] {},
                                tmp.toFile()).waitFor();
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace(); // FIXME: Handle better.
                    }

                    // Post to gateway
                    pushToGeneric(tmp, tmp.getFileName().toString(), properties);
                } finally {
                        // Cleanup - explicitly

                        artifactPath.toFile().delete();
                        metadataPath.toFile().delete();
                        tarFile.toFile().delete();
                        tmp.toFile().delete();
                }


            } catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			System.out.println("Ignoring artifact destined for " +repository +" repository. Not found in +" +Joiner.on(",").join(repositories));
		}
	
	}

	public void init(final FilterConfig config) throws ServletException {
	}

	public void destroy() {
	}
	
	protected boolean endsWithAny(final String target, final String[] extensions) {
		for (final String extension : extensions) {
			if (target.endsWith(extension)) return true;
		}
		
		return false;
	}

    /**
     * Submits a tarball to some gateway host as defined in the NexusGatewayProperties
     *
     * The tarball encapsulates data such that a remote Gateway can relay the Nexus release
     *
     * @param path The path where the artifact is held temporarily
     * @param projectName The name of the tarball bundle to push
     * @param properties
     * @return true iff the submission to the Gateway host
     */
	protected boolean pushToGeneric(final java.nio.file.Path path,
			final String projectName, final Map<String, String> properties) {
		final java.nio.file.Path tarball = Paths.get(path.toString(), projectName +".tar.gz");

        boolean result = false;

		try {
			final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("file", new FileBody(tarball.toFile()));
			
			for (final String key : properties.keySet()) {
				entity.addPart(key, new StringBody(properties.get(key)));
			}
			
			final String uri = "http://"
					+NexusGatewayProperties.get(NexusGatewayProperties.GATEWAY_HOST)
					+":"
					+NexusGatewayProperties.get(NexusGatewayProperties.GATEWAY_PORT)
					+"/"
					+NexusGatewayProperties.get(NexusGatewayProperties.GATEWAY_CONTEXT)
					+"/api/export";
			
			Request.Post(uri)
					.body(entity)
					.execute().returnContent().asString();


		} catch (final IOException e) {
			e.printStackTrace();
            result = false;
		}

        return result;
	}
}
