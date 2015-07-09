package com.github.lookout.serviceartifact.component

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.*

/**
 */
class JRubyComponentSpec extends Specification {
    protected Project project
    protected JRubyComponent component

    def setup() {
        project = ProjectBuilder.builder().build()
        component = new JRubyComponent()
    }

    boolean hasPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar'))
    }


    def "apply() should install the necessary plugins"() {
        when:
        component.apply(project, null, 'spock')

        then:
        hasPlugins(project)
    }

    def "getArtifactTask() should return null if apply() hasn't run"() {
        expect:
        component.artifactTask == null
    }

    def "getArtifactTask() should return a real task after apply()" () {
        when:
        component.apply(project, null, 'spock')

        then:
        component.artifactTask instanceof Task

        and: "the task name should be computed from the component name"
        component.artifactTask.name == "assembleSpock"

        and: "assemble should depend on that task"
        project.tasks.findByName('assemble').dependsOn.contains(component.artifactTask)
    }

    def "mainScript() should set up the entrypoint in the JRuby Jar"() {
        when:
        component.apply(project, null, 'spock')
        component.mainScript 'main.rb'

        then:
        component.artifactTask
        /* The JRuby/Gradle Jar plugin needs some tweaking to make properties more accessible")
        component.artifactTask.jruby.scriptName == 'main.rb'
        */
    }

    def "include() with null should no-op"() {
        when:
        component.apply(project, null, 'spock')
        component.include()

        then:
        component.artifactTask
    }

    def "include() should include files in a consistent relative structure in the JRuby Jar"() {
        when:
        component.apply(project, null, 'spock')
        component.include 'main.rb'

        then:
        component.artifactTask
    }
}
