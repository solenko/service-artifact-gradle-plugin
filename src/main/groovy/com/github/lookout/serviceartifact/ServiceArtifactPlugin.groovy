package com.github.lookout.serviceartifact

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.Tar

class ServiceArtifactPlugin implements Plugin<Project> {
    static final String GROUP_NAME = 'Service Artifact'

    void apply(Project project) {
        /* Add the git plugin for finding out our projects meta-data */
        project.apply plugin: 'org.ajoberstar.release-base'
        /* Add the asciidoctor plugin because...docs are important */
        project.apply plugin: 'org.asciidoctor.gradle.asciidoctor'

        project.extensions.create('service',
                                    ServiceArtifactExtension,
                                    project,
                                    System.env)

        project.task('serviceTarGz', type: Tar) {
            group GROUP_NAME
            description "Create a .tar.gz artifact containing the service"
        }

        project.task('serviceZip', type: Zip) {
            group GROUP_NAME
            description "Create a .zip artifact containing the service"
        }

        project.task('assembleService') {
            group GROUP_NAME
            description "Assemble all the service artifacts"
            dependsOn project.tasks.serviceZip, project.tasks.serviceTarGz
        }
    }
}
