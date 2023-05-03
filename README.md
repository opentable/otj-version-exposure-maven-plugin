# Expose Versions Maven Plugin

The expose versions Maven plugin is designed to help determine the versions of components and expose them, so that they can be accessible to other Maven plugins and to applications at runtime. Which is cool

We basically have 2 types of projects: services and libraries. All of our projects use a common Maven parent POM. That POM is basically our framework, it manages the versions of all of our components.

This plugin does the following:
 - It takes the version of the project being built and stores it in a properties file it creates. This way we can access that file at runtime to get the version.
 - It exposes the version as a Maven property
 - If the project is a service, we find the version of the framework
   - We store the framework version in a properties file added to the built artifact
   - We expose the framework version as an artifact
   
We determine that a project is a service by checking for the existence of a specified Maven property.

## Configuration

| Property  | Description |
| ------------- | ------------- |
| outputDirectory  | This is where we should save version files to. This defaults to target/classes.  |
| projectVersionFileName  | The name of the version file we write for the project. It is also used to create the property name (we append .version to it) that we stored the version number is as well. This defaults to the artifact ID. |
| serviceSignalProperty  | A Maven property that when present signals that this project is a service.  |
| frameworkGroupId  | The group ID of the framework's Maven Parent POM |
| frameworkArtifactId  | The group ID of the framework's Maven Parent POM |
| frameworkPropertyName  | The group ID of the framework's Maven Parent POM |
| frameworkVersionFileName  | The group ID of the framework's Maven Parent POM |
| skip  | Should plugin execution be skipped |


Example:
```xml
        <plugin>
            <groupId>com.opentable</groupId>
            <artifactId>expose-versions-maven-plugin</artifactId>
            <version>1.3</version>
            <executions>
                <execution>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <serviceSignalProperty>services.have.this.property.set</serviceSignalProperty>
                <frameworkGroupId>com.opentable</frameworkGroupId>
                <frameworkArtifactId>framework</frameworkArtifactId>
                <frameworkPropertyName>framework.version</frameworkPropertyName>
                <frameworkVersionFileName>framework-version</frameworkVersionFileName>
                <skip>false</skip>
            </configuration>
        </plugin>
```

Using the configuration above, if a project with artifact ID `sample-project` with version 123 had the Maven property of `services.have.this.property.set` and had the `com.opentable:framework` parent with version 456, the following would happen:

 - A file called `sample-project.properties` would be created and would be accessible in the classpath. It would have one line: `version=123`.
 - A Maven property called `sample-project.version` would exist. It would be equal to 123.
 - A file called `framework-version.properties` would be created and would be accessible in the classpath. It would have one line: `version=456`.
  - A Maven property called `framework.version` would exist. It would be equal to 456.
