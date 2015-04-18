package com.github.lookout.serviceartifact

import groovy.transform.TypeChecked
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.lookout.serviceartifact.scm.AbstractScmHandler

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
@TypeChecked
class ServiceArtifactExtension {
    protected final Project project
    protected final Map<String, String> env
    protected Logger logger = LoggerFactory.getLogger(ServiceArtifactExtension.class)
    /** List of scm handler classes, in priority order */
    private final List<Class<AbstractScmHandler>> scmHandlerImpls = [scm.GerritHandler.class]
    /** SCM Handler appropriate for this execution */
    private AbstractScmHandler scmHandler


    ServiceArtifactExtension(final Project project) {
        this(project, [:])
    }

    ServiceArtifactExtension(final Project project,
                            final Map<String, String> env) {
        this.project = project
        this.env = env
    } 

    /**
     * Lazily look up our SCM Handler
     */
    AbstractScmHandler getScmHandler() {
        if (this.scmHandler != null) {
            return this.scmHandler
        }

        this.scmHandlerImpls.each { Class<AbstractScmHandler> h ->
            AbstractScmHandler handler = h.build(this.env)

            if (handler.isAvailable()) {
                this.scmHandler = handler
                return
            }
        }

        return this.scmHandler
    }

    /**
     * Return the appropriately computed version string based on our executing
     * environment
     */
    String version(final String baseVersion) {
        AbstractScmHandler handler = getScmHandler()

        if (handler instanceof AbstractScmHandler) {
            return handler.annotatedVersion(baseVersion)
        }

        return baseVersion
    }
}

