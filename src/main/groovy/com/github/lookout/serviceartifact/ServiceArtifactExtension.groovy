package com.github.lookout.serviceartifact

import groovy.json.JsonBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.lookout.serviceartifact.scm.AbstractScmHandler

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
class ServiceArtifactExtension {
    protected final Project project
    protected final Map<String, String> env
    protected Logger logger = LoggerFactory.getLogger(ServiceArtifactExtension.class)
    /** List of scm handler classes, in priority order */
    private final List<Class<AbstractScmHandler>> scmHandlerImpls = [
            scm.GerritHandler.class,
            scm.GitHandler.class,
    ]
    /** SCM Handler appropriate for this execution */
    protected AbstractScmHandler _scmHandler

    /** Map of metadata that should be written into the artifact's etc/metadata.conf */
    protected Map<String, Object> metadata = [:]

    ServiceArtifactExtension(final Project project) {
        this(project, [:])
    }

    ServiceArtifactExtension(final Project project,
                            final Map<String, String> env) {
        this.project = project
        this.env = env
    }

    /**
     * Bootstrap and set up whatever internal tasks/helpers we need to set up
     */
    void bootstrap() {
        String versionFilePath = String.format("%s/VERSION", this.project.buildDir)

        Task version = project.tasks.create('serviceVersionInfo') {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Generate the service artifact version information"

            outputs.file(versionFilePath).upToDateWhen { false }

            doFirst {
                JsonBuilder builder = new JsonBuilder()
                builder(generateVersionMap())
                new File(versionFilePath).write(builder.toPrettyString())
            }
        }

        [ServiceArtifactPlugin.TAR_TASK, ServiceArtifactPlugin.ZIP_TASK].each {
            Task archiveTask = this.project.tasks.findByName(it)

            if (archiveTask instanceof Task) {
                archiveTask.dependsOn(version)
                /* Pack the VERSION file containing some built metadata about
                 * this artifact to help trace it back to builds in the future
                 */
                archiveTask.into(this.archiveDirName) { from version.outputs.files }
            }
        }
    }

    /**
     *
     * @return A map containing the necessary version information we want to drop into archives
     */
    Map<String, Object> generateVersionMap() {
        return [
                'version' : this.project.version,
                'name' : this.project.name,
                'buildDate' : new Date(),
                'revision': scmHandler?.revision,
                'builtOn': this.hostname,
        ]
    }

    /**
     * Return a hostname or unknown if we can't resolve our localhost (as seen on Mavericks)
     *
     * @return Local host's name or 'unknown'
     */
    String getHostname() {
        try {
            return InetAddress.localHost.hostName
        }
        catch (UnknownHostException) {
            return 'unknown'
        }
    }

    /**
     * @return A (name)-(version) String for the directory name inside a compressed archive
     */
    String getArchiveDirName() {
        return String.format("%s-%s", this.project.name, this.project.version)
    }

    /**
     * Lazily look up our SCM Handler
     */
    AbstractScmHandler getScmHandler() {
        if (this._scmHandler != null) {
            return this._scmHandler
        }

        this.scmHandlerImpls.find {
            AbstractScmHandler handler = it.build(this.env)

            if (handler.isAvailable()) {
                this._scmHandler = handler
                return true
            }
        }

        return this._scmHandler
    }

    /**
     * Return the appropriately computed version string based on our executing
     * environment
     */
    String version(final String baseVersion) {
        if (this.scmHandler instanceof AbstractScmHandler) {
            return this.scmHandler.annotatedVersion(baseVersion)
        }

        return baseVersion
    }

    /**
     * Return the computed Map<> of metadata that is to be written to the etc/metadata.conf
     * inside of the service artifact
     */
    Map<String, Object> getMetadata() {
        return this.metadata
    }

    void metadata(Object... arguments) {
        arguments.each {
            this.metadata.putAll(it as Map)
        }
    }

    void setMetadata(Object... arguments) {
        this.metadata = [:]
        this.metadata arguments
    }

    /**
     * return the configured project for this service{} extension
     */
    Project getProject() {
        return this.project
    }
}

