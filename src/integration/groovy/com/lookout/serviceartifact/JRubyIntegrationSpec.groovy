package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.testfixtures.ProjectBuilder

import com.github.jrubygradle.jar.JRubyJar

/**
 * Verify that the end-to-end JRuby support works
 */
class JRubyIntegrationSpec extends Specification {
    protected Project project
    protected String serviceName = 'faas'
    protected String componentName = 'backend'
    protected String version = '1.1'

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.github.lookout.service-artifact'
    }

    boolean hasJRubyPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar'))
    }

    void enableJRuby() {
        project.version = version
        project.service {
            name this.serviceName
            component(this.componentName, type: JRuby) {
            }
        }
    }

    def "when using component{} DSL the JRuby plugins should be added"() {
        given:
        enableJRuby()

        when:
        project.evaluate()

        then:
        hasJRubyPlugins(project)
    }

    def "artifacts{} should include the JRubyJar archive"() {
        given:
        enableJRuby()
        Configuration config = project.configurations.findByName('serviceArchives')

        when:
        project.evaluate()

        then:
        config
        config.artifacts.find { it.archiveTask instanceof JRubyJar }
    }
}