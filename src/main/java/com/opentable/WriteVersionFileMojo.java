package com.opentable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which adds a file to the project that has the project's version number in it
 * There's also special handling for the OTJ Parent POM, so we can determine the OTJ version
 */
@Mojo(name = "create", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class WriteVersionFileMojo extends AbstractMojo {

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true)
    private File outputDirectory;

    /**
     * Name of the file.
     */
    @Parameter(defaultValue = "${project.artifactId}", property = "fileName", required = false)
    private String fileName;

    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {

        File target = outputDirectory;

        if (!target.exists()) {
            target.mkdirs();
        }

        File propertiesFile = new File(target, fileName + ".properties");

        getLog().info("Writing version " + project.getVersion() + " to " + fileName + ".properties");

        writeVersionNumberToFile(propertiesFile, project.getVersion());

        project.getProperties().put(fileName + ".version", project.getVersion());

        if(project.getProperties().containsKey("ot.mainclass")) {
            getLog().info("This is an OTJ project...");
            Optional<MavenProject> otjProjectOptional = findProjectInAncestorChain("com.opentable", "otj-parent-spring");
            if(otjProjectOptional.isPresent()) {
                getLog().info("Found OTJ parent POM...");
                MavenProject otj = otjProjectOptional.get();
                getLog().info("OTJ Version: " + otj.getVersion());
                File otjVersionFile = new File(target, "otj-version.properties");
                writeVersionNumberToFile(otjVersionFile, otj.getVersion());
                project.getProperties().put("otj.parent.pom.version", otj.getVersion());
            }
        }
    }

    private void writeVersionNumberToFile(File propertiesFile, String version) throws MojoExecutionException {
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("version=" + version + "\n");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + propertiesFile, e);
        }
    }

    private Optional<MavenProject> findProjectInAncestorChain(String groupId, String artifactId) {
        MavenProject target = project;
        while(target != null) {
            if(target.getGroupId().equalsIgnoreCase(groupId) && target.getArtifactId().equalsIgnoreCase(artifactId)) {
                return Optional.of(target);
            }
            target = target.getParent();
        }
        return Optional.empty();
    }
}
