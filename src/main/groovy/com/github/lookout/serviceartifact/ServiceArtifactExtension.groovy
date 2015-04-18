package com.github.lookout.serviceartifact

import groovy.transform.TypeChecked
import org.gradle.api.Project
import org.ajoberstar.grgit.Grgit

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
@TypeChecked
class ServiceArtifactExtension {
    protected final Project project
    protected final Map<String, String> env

    private final String GERRIT_CHANGE = 'GERRIT_CHANGE_NUMBER'
    private final String GERRIT_PATCH  = 'GERRIT_PATCHSET_NUMBER'

    ServiceArtifactExtension(final Project project) {
        this(project, [:])
    }

    ServiceArtifactExtension(final Project project,
                            final Map<String, String> env) {
        this.project = project
        this.env = env
    }

    /**
     * Return the appropriately computed version string based on our executing
     * environment
     */
    String version(final String baseVersion) {
        if (isUnderGerrit()) {
            return String.format("%s.%s.%s",
                                 baseVersion,
                                 env[GERRIT_CHANGE],
                                 env[GERRIT_PATCH])
        }
        return baseVersion
    }

    /**
     * Return true if our current build is executing inside of a
     * Gerrit-context, i.e. the GERRIT_CHANGE_NUMBER environment variable is
     * preset
     */
    protected boolean isUnderGerrit() {
        if (this.env == null) {
            return false
        }
        return this.env.containsKey(GERRIT_CHANGE)
    }
}

