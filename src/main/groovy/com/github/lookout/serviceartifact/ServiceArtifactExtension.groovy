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
    /** DSL helper to allow users to refer to the JRubyComponent by the JRuby
     * constant
     */
    static final Class<AbstractComponent> JRuby = JRubyComponent


    protected Logger logger = LoggerFactory.getLogger(ServiceArtifactExtension)
    protected final Project project
    protected final Map<String, String> env

    /** Disable the built-in jar tasks by default */
    protected boolean defaultJarTaskEnabled = false

    /** List of scm handler classes, in priority order */
    private final List<Class<AbstractScmHandler>> scmHandlerImpls = [
            scm.GerritHandler.class,
            scm.GitHandler.class,
    ]
    /** SCM Handler appropriate for this execution */
    protected AbstractScmHandler _scmHandler

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

        this.data = new Data(project)
    }

    /**
     * Bootstrap and set up whatever internal tasks/helpers we need to set up
     */
    void bootstrap() {
        String versionFilePath = String.format("%s/VERSION", project.buildDir)

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

    /** set the service name */
    void name(String name) {
        this.serviceName = name
    }

    /** access the configured service name */
    String getName() {
        return this.serviceName
    }

    /** set the service's service dependencies */
    void dependencies(Object... arguments) {
        this.serviceDependencies = arguments as List<String>
    }

    /** access the configured service dependencies */
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
        instance.apply(this.project, this, name)
        instance.createCompressedTasks(this.archiveDirName)

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


    /**
     * set whether the plugin should disable the default jar task provided by
     * underlying plugins (e.g. 'java' or 'groovy')
     */
    void defaultJarEnabled(boolean status) {
        this.defaultJarTaskEnabled = status
    }

    boolean getDefaultJarEnabled() {
        return this.defaultJarTaskEnabled
    }

    /**
     * Internal hook to be called after the project has completed its
     * evaluation phase
     */
    void afterEvaluateHook() {
        this.validateMetadata()
        this.disableDefaultJarTask()
    }


    /**
     * if configured as such, disable the default jar task and remove its
     * artifacts from the `archives` configuration
     */
    private void disableDefaultJarTask() {
        /* bail out early if we should leave it alone */
        if (defaultJarTaskEnabled) {
            return
        }

        Task jar = project.tasks.findByName('jar')

        if (jar instanceof Task) {
            jar.enabled = false
            /* also purge the jar task from our archives configuration */
            project.configurations.archives.artifacts.removeAll {
                it.archiveTask.is jar
            }
        }
    }

    /**
     *
     * @return A map containing the necessary version information we want to drop into archives
     */
    private Map<String, Object> generateVersionMap() {
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
    private String getHostname() {
        try {
            return InetAddress.localHost.hostName
        }
        catch (UnknownHostException) {
            return 'unknown'
        }
    }

    /**
     * Lazily look up our SCM Handler
     */
    private AbstractScmHandler getScmHandler() {
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
     * @return A (name)-(version) String for the directory name inside a compressed archive
     */
    private String getArchiveDirName() {
        return String.format("%s-%s", this.project.name, this.project.version)
    }
}
