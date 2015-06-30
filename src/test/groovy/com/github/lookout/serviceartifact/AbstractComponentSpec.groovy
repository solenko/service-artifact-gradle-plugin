package com.github.lookout.serviceartifact

import groovy.transform.InheritConstructors
import org.gradle.api.tasks.StopExecutionException
import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder


/**
 * A simple class with which tests against AbstractComponent can be implemented
 */
class SpockComponent extends AbstractComponent {
    Task getArtifactTask() { return null }
}

@Title("AbstractComponent instance specification")
class AbstractComponentSpec extends Specification {
    protected Project project
    @Subject
    protected AbstractComponent component

    def setup() {
        this.project = ProjectBuilder.builder().build()
        this.component = new SpockComponent()
    }

    def "apply() should raise if Project is not valid"() {
        when:
        component.apply(null, 'spork')

        then:
        thrown(StopExecutionException)
    }

    def "apply() should raise if name is null/empty"() {
        given:
        this.project = ProjectBuilder.builder().build()

        when:
        component.apply(project, '')

        then:
        thrown(StopExecutionException)
    }

    def "apply() with valid arguments should return successfully"() {
        given:
        this.project = ProjectBuilder.builder().build()
        String name = 'spock-component'

        when:
        component.apply(project, name)

        then:
        component.project == project
        component.name == name
    }

    def "chainCompressedArchives() should fail if the provided task name doesn't exist"() {
        given:
        String taskName = 'spockTar'

        when:
        component.apply(project, 'spock')

        then:
        component.chainCompressedArchives(taskName) == false
    }

    def "chainCompressedArchives() should tie artifactTask to the given task names"() {
        given:
        Task compressedArchive = project.tasks.create('spockTar')
        Task artifactTask = project.tasks.create('assembleSpockJar')
        def result

        when:
        component.apply(project, 'spock')
        component.artifactTask = artifactTask
        result = component.chainCompressedArchives(compressedArchive.name)

        then: "the call to return true"
        result == true

        and: "the compressedArchive task to depend on the artifactTask"
        compressedArchive.dependsOn.contains(artifactTask)

    }
}
