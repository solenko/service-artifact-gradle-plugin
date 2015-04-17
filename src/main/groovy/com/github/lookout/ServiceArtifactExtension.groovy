package com.github.lookout

import org.gradle.api.Project

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
class ServiceArtifactExtension {
    protected final Project project

    ServiceArtifactExtension(final Project project) {
        this.project = project
    }
}

