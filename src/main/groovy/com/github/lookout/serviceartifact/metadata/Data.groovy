package com.github.lookout.serviceartifact.metadata

import groovy.transform.TypeChecked

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Container class for encapsulating some of the DSL configuration behavior
 * behind the service { data { } } closure
 */
@TypeChecked
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
        this.dependencies.addAll(arguments as List<String>)
    }

    /**
     * migrations() DSL method for adding the migrations either as a list of
     * String objects, or as a FileTree into the migrations list
     */
    void migrations(Object... arguments) {
        arguments.each { Object argument ->
            if (argument instanceof FileTree) {
                FileTree tree = argument as FileTree
                this.migrations.addAll(tree.files.collect { File f -> f.absolutePath })
            }
            else {
                this.migrations.add(argument as String)
            }
        }
    }

    @JsonIgnore
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

    String toString() {
        return String.format("<Data@%d> %s (%s) - %s (%s)", hashCode(), this.dependencies, this.dependencies.class, this.migrations, this.dependencies.class)

    }
}
