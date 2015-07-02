package com.github.lookout.serviceartifact.metadata

import com.fasterxml.jackson.annotation.JsonProperty
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Container class for encapsulating some of the DSL configuration behavior
 * behind the service { data { } } closure
 */
class Data {
    protected Logger logger = LoggerFactory.getLogger(this.class)
    protected Project project

    @JsonProperty
    List<String> dependencies = []
    @JsonProperty
    List<String> migrations = []

    Data(Project project) {
        this.project = project
    }

    /**
     * Add the list of arguments as dependencies
     * @param arguments list of String objects representing data components
     */
    void dependencies(Object... arguments) {
        this.dependencies.addAll(arguments)
    }

    /**
     * migrations() DSL method for adding the migrations either as a list of
     * String objects, or as a FileTree into the migrations list
     */
    void migrations(Object... arguments) {
        arguments.each { Object argument ->
            if (argument instanceof FileTree) {
                this.migrations.addAll((argument as FileTree).files)
            }
            else {
                this.migrations.add(argument)
            }
        }
    }

    File getProjectDir() {
        return project.projectDir
    }

    /**
     * Helper method to make the DSL more succinct. Exact same as Project
     * fileTree
     */
    FileTree fileTree(Map args) {
        return project.fileTree(args)
    }
}
