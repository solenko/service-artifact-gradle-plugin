package com.github.lookout.serviceartifact

import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceArtifactPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'application'
        /* Add the git plugin for finding out our projects meta-data */
        project.apply plugin: 'org.ajoberstar.release-base'
        /* Add the asciidoctor plugin because...docs are important */
        project.apply plugin: 'org.asciidoctor.gradle.asciidoctor'

        project.extensions.create('service',
                                    ServiceArtifactExtension,
                                    project,
                                    System.env)
    }
}
