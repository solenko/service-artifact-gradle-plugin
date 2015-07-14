package com.github.lookout.serviceartifact

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.Tar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A Component is a declaration of a part of a service. This object is intended
 * to contain the necessary configuration and logic needed to configure tasks
 * and configurations necessary to build an artifact for the defined component
 */
abstract class AbstractComponent {
    Task artifactTask

    protected Logger logger = LoggerFactory.getLogger(this.class)
    protected Project project
    protected Object extension
    protected String name
    protected Metadata metadata

    void apply(Project project, Object extension, String name) {
        if (name?.length() <= 0) {
            throw new StopExecutionException("Cannot apply component with a zero-length name")
        }

        if (!(project instanceof Project)) {
            throw new StopExecutionException("Cannot apply component to an invalid project")
        }

        this.project = project
        this.extension = extension
        this.name = name
        this.metadata = new Metadata(extension?.serviceName, name, project.version)

        this.metadata.service.dependencies = extension?.serviceDependencies
        this.metadata.data = extension?.data
    }

    boolean createCompressedTasks(String archiveDirName) {
        Project project = this.project
        String metadataFilePath = String.format("%s/%s-metadata.conf", project.buildDir, this.name)

        String metadataTaskName = String.format("%s%s", ServiceArtifactPlugin.METADATA_TASK, this.name.capitalize())

        Task metadataTask = project.tasks.create(metadataTaskName) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Generate the service artifact etc/metadata.conf"

            outputs.file(metadataFilePath).upToDateWhen { false }

            doFirst {
                new File(metadataFilePath).write(metadata.toYaml())
            }
        }

        Task zipTask = createZipTask(this.project, this.name)
        Task tarTask = createTarTask(this.project, this.name)

        /* setting this up last so archiveDirName gets the right version and other things */
        project.afterEvaluate {
            /* Link our generic serviceTar to our custom tar task */
            project.tasks.findByName(ServiceArtifactPlugin.TAR_TASK).dependsOn(tarTask)
            project.tasks.findByName(ServiceArtifactPlugin.ZIP_TASK).dependsOn(zipTask)

            [tarTask, zipTask].each { Task archiveTask ->
                Task versionTask = this.project.tasks.findByName(ServiceArtifactPlugin.VERSION_TASK)

                if (archiveTask instanceof Task) {
                    archiveTask.dependsOn(this.artifactTask)
                    archiveTask.dependsOn(versionTask)
                    archiveTask.dependsOn(metadataTask)
                    /* Pack the VERSION file containing some built metadata about
                     * this artifact to help trace it back to builds in the future
                     */
                    archiveTask.into(archiveDirName) { from versionTask.outputs.files }
                    archiveTask.into("${archiveDirName}/etc") {
                        from metadataTask.outputs.files
                        rename "${this.name}-metadata.conf", "metadata.conf"
                    }
                }
            }
        }

        return true
    }

    private Zip createZipTask(Project project, String name) {
        return project.task("assemble${name.capitalize()}Zip", type: Zip) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Create a .zip artifact containing the ${name} service"
        }
    }

    private Tar createTarTask(Project project, String name) {
        return project.task("assemble${name.capitalize()}Tar", type: Tar) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Create a .tar artifact containing the ${name} service"
        }
    }
}
