package com.github.lookout.serviceartifact

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException
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
    protected String name

    void apply(Project project, String name) {
        if (name?.length() <= 0) {
            throw new StopExecutionException("Cannot apply component with a zero-length name")
        }

        if (!(project instanceof Project)) {
            throw new StopExecutionException("Cannot apply component to an invalid project")
        }

        this.project = project
        this.name = name
    }

    /**
     * Chain together artifact tasks
     */
    boolean chainCompressedArchives(Object... taskNames) {
        Project project = this.project
        Task artifactTask =  this.artifactTask

        if (!taskNames.findAll { project.tasks.findByName(it) }) {
            return false
        }

        taskNames.each { String taskName ->
            Task archiveTask = project.tasks.findByName(taskName)

            if (archiveTask && artifactTask) {
                archiveTask.dependsOn(artifactTask)
            }
        }

        return true
    }
}
