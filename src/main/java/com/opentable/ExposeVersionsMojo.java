package com.opentable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which exposes the version of this project in a generated properties file
 * and as a Maven property.
 *
 * Also, if this project is a service, we do the same for the framework parent
 * pom (if it is a parent)
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ExposeVersionsMojo extends AbstractMojo {

    private static final String PROPERTIES_FILE_EXTENSION = ".properties";

    /**
     * Location where we should output the generated files. The default is
     * target/classes as that is included in the jar
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * Name of the properties file to produce for this version. The .properties
     * suffix will be appended. The default is the artifact ID
     */
    @Parameter(defaultValue = "${project.artifactId}", property = "projectVersionFileName", required = false)
    private String projectVersionFileName;

    /**
     * Name of the property that we will use as a signal that this project is a
     * service and therefore we should record the version information about the
     * framework parent
     */
    @Parameter(property = "serviceSignalProperty", required = true)
    private String serviceSignalProperty;

    /**
     * Name of the group ID of the framework parent POM
     */
    @Parameter(property = "frameworkGroupId", required = true)
    private String frameworkGroupId;

    /**
     * Name of the artifact ID of the framework parent POM
     */
    @Parameter(property = "frameworkArtifactId", required = true)
    private String frameworkArtifactId;

    /**
     * Name of the property that we will put the framework's version in
     */
    @Parameter(property = "frameworkPropertyName", required = true)
    private String frameworkPropertyName;

    /**
     * Name of the properties file to produce to store the framework version The
     * .properties suffix will be appended. The default is the artifact ID
     */
    @Parameter(property = "frameworkVersionFileName", required = true)
    private String frameworkVersionFileName;

    /**
     * Name of the properties file to produce to store the framework version The
     * .properties suffix will be appended. The default is the artifact ID
     */
    @Parameter(property = "skip", defaultValue="false")
    private boolean skip;

    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {

        if(skip) {
            getLog().info("Skipping execution.");
            return;
        }

        try {
            File target = outputDirectory;

            if (!target.exists()) {
                boolean created = target.mkdirs();
                if(!created) {
                    getLog().warn("Could not create target directory");
                }
            }

            File propertiesFile = new File(target, projectVersionFileName + PROPERTIES_FILE_EXTENSION);

            writeVersionNumberToFile(propertiesFile, project.getVersion());

            project.getProperties().put(projectVersionFileName + ".version", project.getVersion());

            if (project.getProperties().containsKey(serviceSignalProperty)) {
                getLog().debug("This project is a service.");
                findProjectInAncestorChain(frameworkGroupId, frameworkArtifactId).ifPresent(otj -> {
                    String version = otj.getVersion();
                    getLog().info("Found framework parent POM. Version: " + version);
                    File otjVersionFile = new File(target, frameworkVersionFileName + PROPERTIES_FILE_EXTENSION);
                    writeVersionNumberToFile(otjVersionFile, version);
                    project.getProperties().put(frameworkPropertyName, otj.getVersion());
                });
            }
        } catch (RuntimeMojoExecutionException e) {
            throw e.getMojoExecutionException();
        }
    }

    /**
     * Write the version number to a file The file will just have one line
     * <tt>version=(version)</tt>
     *
     * @param propertiesFile
     *            the file to write to
     * @param version
     *            the version number to write
     */
    private void writeVersionNumberToFile(File propertiesFile, String version) {
        getLog().info("Writing version " + version + " to " + propertiesFile.getPath());
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8);) {
            writer.write("version=" + version + "\n");
        } catch (IOException e) {
            throw new RuntimeMojoExecutionException(
                    new MojoExecutionException("Error creating file " + propertiesFile, e));
        }
    }

    /**
     * Find a project in the chain of parent POMs. The project will only be
     * returned if it matches the given group and artifact IDs.
     *
     * @param groupId
     *            the groupID to look for
     * @param artifactId
     *            the artifact ID to look for
     * @return the desired parent project if found, otherwise an empty optional.
     */
    private Optional<MavenProject> findProjectInAncestorChain(String groupId, String artifactId) {
        MavenProject target = project;
        while (target != null) {
            if (target.getGroupId().equalsIgnoreCase(groupId) && target.getArtifactId().equalsIgnoreCase(artifactId)) {
                return Optional.of(target);
            }
            target = target.getParent();
        }
        return Optional.empty();
    }
}
