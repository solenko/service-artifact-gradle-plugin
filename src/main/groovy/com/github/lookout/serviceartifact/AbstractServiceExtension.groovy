package com.github.lookout.serviceartifact

import com.github.lookout.serviceartifact.scm.AbstractScmHandler
import groovy.json.JsonBuilder
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Base abstract class for defining extensions inside of the service {}
 * DSL
 *
 */
abstract class AbstractServiceExtension {
    abstract void apply(Object delegate, Closure configClosure)

    /**
     * Properly update the compressed archive tasks with the appropriate
     * configurations after the serviceJar task has been set up
     *
     * @param project
     * @return
     */
    protected void setupCompressedArchives(Project project, AbstractScmHandler scmHandler) {
        Task tar = project.tasks.findByName('serviceTarGz')
        Task zip = project.tasks.findByName('serviceZip')
        Task jar = project.tasks.findByName('serviceJar')

        Task version = project.tasks.create('serviceVersionInfo') {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Generate the service artifact version information"

            def versionFilePath = "${this.project.buildDir}/VERSION"
            outputs.file(versionFilePath).upToDateWhen { false }

            doFirst {
                JsonBuilder builder = new JsonBuilder()
                builder(buildDate: new Date(),
                        version: project.version,
                        name: project.name,
                        revision: scmHandler?.revision,
                        builtOn: InetAddress.localHost.hostName)
                new File(versionFilePath).write(builder.toPrettyString())
            }
        }

        /* Ensure our service (distribution) artifact tasks depend on this
         * jar task
         */
        [tar, zip].each {
            String directory = String.format("%s-%s", project.name, project.version)

            it.dependsOn(jar)
            it.into(directory) { from(jar.outputs.files) }
            it.into("${directory}/bin") { from("${project.projectDir}/bin") }

            /* Pack a handy VERSION file containing some built metadata about
             * this artifact to help trace it back to builds in the future
             */
            it.into(directory) { from version.outputs.files }
        }
    }

    protected void disableJarTask() {
        Task jarTask = this.project.tasks.findByName('jar')

        if (jarTask instanceof Task) {
            jarTask.enabled = false
        }
    }
}
