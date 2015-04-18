package com.github.lookout

import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceArtifactPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'application'
        /* Add the git plugin for finding out our projects meta-data */
        project.apply plugin: 'org.ajoberstar.release-base'

        project.extensions.create('service', ServiceArtifactExtension, project, System.env)
    }
}
