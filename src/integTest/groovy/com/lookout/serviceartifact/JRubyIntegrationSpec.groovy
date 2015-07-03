package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.testfixtures.ProjectBuilder

import nebula.test.PluginProjectSpec
import nebula.test.IntegrationSpec

import com.github.jrubygradle.jar.JRubyJar

/**
 * Verify that the end-to-end JRuby support works
 */
class JRubyIntegrationSpec extends PluginProjectSpec {
    protected String serviceName = 'faas'
    protected String componentName = 'backend'
    protected String version     = '1.1'

    String getPluginName() { 'com.github.lookout.service-artifact' }

    boolean hasJRubyPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar'))
    }

    void enableJRuby() {
        project.apply plugin: this.pluginName
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

class JRubyFullIntegrationSpec extends IntegrationSpec {

    def "the assemble task should produce a tar"() {
        given:
        String version = '1.0'
        String projectName = 'fullinteg'
        settingsFile << "rootProject.name = '${projectName}'"
        buildFile << """
apply plugin: 'com.github.lookout.service-artifact'

version = '${version}'

service {
  name '${projectName}'
  component('api', type: JRuby) { }
}
"""

        /* XXX: why do I have to make the buildDir myself? */
        directory('build')

        expect:
        runTasksSuccessfully('assemble')
        fileExists("build/distributions/${projectName}-${version}.tar")
    }
}