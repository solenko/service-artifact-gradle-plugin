package com.github.lookout.serviceartifact

import com.github.lookout.serviceartifact.component.JRubyComponent
import groovy.json.JsonBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.lookout.serviceartifact.scm.AbstractScmHandler
import com.github.lookout.serviceartifact.metadata.Data

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
class ServiceArtifactExtension {
    protected final Project project

    static final Class<AbstractComponent> JRuby = JRubyComponent

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
    protected Metadata metadata

    /** Name of the service of which our artifact is a part */
    protected String serviceName = null
    /** List of services that this service depends on */
    protected List<String> serviceDependencies = []
    /** Data container object for implementing the data {} DSL */
    protected Data data

    ServiceArtifactExtension(final Project project) {
        this(project, [:])
    }

    ServiceArtifactExtension(final Project project,
                            final Map<String, String> env) {
        this.project = project
        this.env = env

        this.metadata = new Metadata(project.name, project.name, project.version)
        this.data = new Data(project)
    }

    /**
     * Bootstrap and set up whatever internal tasks/helpers we need to set up
     */
    void bootstrap() {
        String versionFilePath = String.format("%s/VERSION", this.project.buildDir)
        String metadataFilePath = String.format("%s/metadata.conf", this.project.buildDir)

        Task versionTask = project.tasks.create(ServiceArtifactPlugin.VERSION_TASK) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Generate the service artifact version information"

            outputs.file(versionFilePath).upToDateWhen { false }

            doFirst {
                JsonBuilder builder = new JsonBuilder()
                builder(generateVersionMap())
                new File(versionFilePath).write(builder.toPrettyString())
            }
        }

        Task metadataTask = project.tasks.create(ServiceArtifactPlugin.METADATA_TASK) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Generate the service artifact etc/metadata.conf"

            outputs.file(metadataFilePath).upToDateWhen { false }

            doFirst {
                new File(metadataFilePath).write('hello')
            }
        }


        /* setting this up last so archiveDirName gets the right version and other things */
        project.afterEvaluate {
            [ServiceArtifactPlugin.TAR_TASK, ServiceArtifactPlugin.ZIP_TASK].each {
                Task archiveTask = this.project.tasks.findByName(it)

                if (archiveTask instanceof Task) {
                    archiveTask.dependsOn(versionTask)
                    archiveTask.dependsOn(metadataTask)
                    /* Pack the VERSION file containing some built metadata about
                     * this artifact to help trace it back to builds in the future
                     */
                    archiveTask.into(this.archiveDirName) { from versionTask.outputs.files }
                    archiveTask.into("${this.archiveDirName}/etc") { from metadataTask.outputs.files }
                }
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
            AbstractScmHandler handler = it.build(this.project, this.env)

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
     * Validate the existing metadata to ensure non-optional fields are set
     * before we attempt to do anything with the metadata
     *
     * @return true if the metadata is valid
     * @throws org.gradle.api.tasks.StopExecutionException
     */
    boolean validateMetadata() {
        logger.info("Metadata: ${serviceName} (${serviceDependencies})")
        logger.info("  data: ${this.data.dependencies} - ${this.data.migrations}")
        if (this.serviceName) {
            return true
        }
        throw new StopExecutionException("Missing required metadata fields")
    }

    void name(String name) {
        this.serviceName = name
    }

    String getName() {
        return this.serviceName
    }

    void dependencies(Object... arguments) {
        this.serviceDependencies = arguments as List<String>
    }

    List<String> getDependencies() {
        return this.serviceDependencies
    }

    /**
     * return the configured project for this service{} extension
     */
    Project getProject() {
        return this.project
    }

    /**
     * Configure a component of the given type
     *
     * @param keywordArguments Expected to receive a KW arg of "type" to make our DSL pretty
     * @param name the name of the component getting configured
     * @param configurationSpec
     * @return the configured AbstractComponent instance
     */
    AbstractComponent component(Map keywordArguments, String name, Closure configurationSpec) {
        if (!(keywordArguments.type instanceof Class<AbstractComponent>)) {
            throw new StopExecutionException("component() must be called with a type: parameter")
        }

        AbstractComponent instance = keywordArguments.type.newInstance()
        instance.apply(this.project, name)
        instance.chainCompressedArchives('serviceTar', 'serviceZip')

        configurationSpec.delegate = instance
        configurationSpec.call(instance)

        if (instance.artifactTask) {
            project.artifacts.add(ServiceArtifactPlugin.ARCHIVES_CONFIG, instance.artifactTask)
        }

        return instance
    }

    void data(Closure dataConfigurationSpec) {
        dataConfigurationSpec.delegate = this.data
        /* Since both the owner (this) and the Data object have .dependencies()
         * we want to resolve to the delegate first
         */
        dataConfigurationSpec.resolveStrategy = Closure.DELEGATE_FIRST
        dataConfigurationSpec.call(project)
    }


    void afterEvaluateHook() {
        this.validateMetadata()
    }
}

