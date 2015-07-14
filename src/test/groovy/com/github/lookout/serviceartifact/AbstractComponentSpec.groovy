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
        component.apply(null, null, 'spork')

        then:
        thrown(StopExecutionException)
    }

    def "apply() should raise if name is null/empty"() {
        given:
        this.project = ProjectBuilder.builder().build()

        when:
        component.apply(project, null, '')

        then:
        thrown(StopExecutionException)
    }

    def "apply() with valid arguments should return successfully"() {
        given:
        this.project = ProjectBuilder.builder().build()
        String name = 'spock-component'

        when:
        component.apply(project, null, name)

        then:
        component.project == project
        component.name == name
    }

    def "createCompressedTasks() should create a custom Zip task"() {
        given:
        String expectedTask = 'assembleSpockZip'

        when:
        component.apply(project, null, 'spock')

        then:
        component.createCompressedTasks()

        and:
        project.tasks.findByName(expectedTask)
    }

    def "createCompressedTasks() should create a custom Tar task"() {
        given:
        String expectedTask = 'assembleSpockTar'

        when:
        component.apply(project, null, 'spock')

        then:
        component.createCompressedTasks()

        and:
        project.tasks.findByName(expectedTask)
    }
}
