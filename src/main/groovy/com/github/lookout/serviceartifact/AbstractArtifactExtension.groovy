package com.github.lookout.serviceartifact

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Base abstract class for defining extensions inside of the service {}
 * DSL
 *
 */
abstract class AbstractArtifactExtension {
    abstract void apply(Object delegate, Closure configClosure)

    /**
     * Properly update the compressed archive tasks with the appropriate
     * configurations after a serviceJar task has been set up
     *
     * @param project
     */
    protected void setupCompressedArchives(Object serviceExtension) {
        Project project = serviceExtension.project
        Task tar = project.tasks.findByName('serviceTar')
        Task zip = project.tasks.findByName('serviceZip')
        Task jar = project.tasks.findByName('serviceJar')

        /* Ensure our service (distribution) artifact tasks depend on this
         * jar task
         */
        [tar, zip].each {
            it.dependsOn(jar)
            it.into(serviceExtension.archiveDirName) { from(jar.outputs.files) }
            it.into("${serviceExtension.archiveDirName}/bin") { from("${project.projectDir}/bin") }
        }
    }

    protected void disableJarTask() {
        this.project.tasks.findByName('jar')?.enabled = false
    }
}
