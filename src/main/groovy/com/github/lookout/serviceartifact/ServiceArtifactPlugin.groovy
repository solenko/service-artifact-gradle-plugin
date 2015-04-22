package com.github.lookout.serviceartifact

import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceArtifactPlugin implements Plugin<Project> {
    private final String GROUP_NAME = 'Service Artifact'

    void apply(Project project) {
        /* Add the git plugin for finding out our projects meta-data */
        project.apply plugin: 'org.ajoberstar.release-base'
        /* Add the asciidoctor plugin because...docs are important */
        project.apply plugin: 'org.asciidoctor.gradle.asciidoctor'

        project.extensions.create('service',
                                    ServiceArtifactExtension,
                                    project,
                                    System.env)

        project.task('serviceTarGz') {
            group GROUP_NAME
            description "Create a .tar.gz artifact containing the service"
        }

        project.task('serviceZip') {
            group GROUP_NAME
            description "Create a .zip artifact containing the service"
        }
    }
}
