package com.github.lookout.serviceartifact

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.Tar

class ServiceArtifactPlugin implements Plugin<Project> {
    static final String GROUP_NAME = 'Service Artifact'
    static final String ARCHIVES_CONFIG = "serviceArchives"
    static final String ZIP_TASK = 'serviceZip'
    static final String TAR_TASK = 'serviceTar'
    static final String VERSION_TASK  = 'serviceVersionInfo'
    static final String METADATA_TASK = 'serviceMetadata'

    void apply(Project project) {
        /* Add the asciidoctor plugin because...docs are important */
        project.apply plugin: 'org.asciidoctor.gradle.asciidoctor'
        /* Add the dependency-lcok plugin by default because that's important! */
        //project.apply plugin: 'nebula.dependency-lock'


        Object service = project.extensions.create('service',
                                                            ServiceArtifactExtension,
                                                            project,
                                                            System.env)

        ServiceArtifactExtension.metaClass.scala = { Closure extraConfig ->
            new lang.Scala(project).apply(delegate, extraConfig)
        }

        Configuration archive = project.configurations.create(ARCHIVES_CONFIG)

        Task prepareTask = project.task('prepareServiceScripts') {
            group GROUP_NAME
            description "stub task for preparing the bin scripts for the artifact"
        }

        Task tarTask = project.task(TAR_TASK, type: Tar) {
            group GROUP_NAME
            description "Create a .tar.gz artifact containing the service"
            dependsOn prepareTask
        }

        Task zipTask = project.task(ZIP_TASK, type: Zip) {
            group GROUP_NAME
            description "Create a .zip artifact containing the service"
            dependsOn prepareTask
        }

        Task assembleTask = project.task('assembleService') {
            group GROUP_NAME
            description "Assemble all the service artifacts"
            dependsOn zipTask, tarTask
        }

        Task publishTask = project.task('publishService') {
            group GROUP_NAME
            description "Publish all our artifacts (uploadServiceArchives"
            dependsOn project.tasks.uploadServiceArchives
        }

        project.artifacts.add(ARCHIVES_CONFIG, zipTask)
        project.artifacts.add(ARCHIVES_CONFIG, tarTask)

        /* With everything defined, let's bootstrap the service extension internals */
        service.bootstrap()
        project.afterEvaluate {
            service.afterEvaluateHook()
        }
    }
}
