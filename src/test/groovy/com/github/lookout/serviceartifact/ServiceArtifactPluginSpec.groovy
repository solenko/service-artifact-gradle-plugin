package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.bundling.Tar
import org.gradle.testfixtures.ProjectBuilder

class ServiceArtifactPluginSpec extends Specification {
    Project project

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.github.lookout.service-artifact'
    }

    def "project should have our plugin"() {
        expect:
        project instanceof Project
        project.plugins.findPlugin(ServiceArtifactPlugin.class)
    }

    def "project should have the asciidoctor plugin"() {
        expect:
        project.plugins.findPlugin('org.asciidoctor.gradle.asciidoctor')
    }

    def "project should NOT have the jruby-gradle base plugin by default"() {
        expect:
        !project.plugins.findPlugin('com.github.jruby-gradle.base')
    }

    def "project should include the service{} DSL"() {
        expect:
        project.service instanceof ServiceArtifactExtension
    }


    def "project should include the serviceTarGz task"() {
        given:
        Task t = project.tasks.findByName('serviceTarGz')

        expect:
        t instanceof Tar
        t.group == 'Service Artifact'
    }

    def "project should include the serviceZip task"() {
        given:
        Task t = project.tasks.findByName('serviceZip')

        expect:
        t instanceof Zip
        t.group == 'Service Artifact'
    }

    def "project should include a prepareServiceScripts task"() {
        given:
        Task t = project.tasks.findByName('prepareServiceScripts')

        expect:
        t instanceof Task
        t.group == 'Service Artifact'
    }

    def "project should include a assembleService task"() {
        given:
        Task t = project.tasks.findByName('assembleService')

        expect:
        t instanceof Task
        t.group == 'Service Artifact'
    }
}


class ServiceArtifactPluginWithJRubySpec extends ServiceArtifactPluginSpec {
    boolean hasPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar'))
    }

    void enableJRuby() {
        project.version = '1.0'
        project.service { jruby {} }
    }

    def "when using the jruby{} closure the plugin should be added"() {
        given:
        enableJRuby()

        expect:
        hasPlugins(project)
    }


    def "a serviceJar task should be present"() {
        given:
        enableJRuby()
        Task jar = project.tasks.findByName('serviceJar')

        expect:
        jar instanceof Task
    }

    def "the default jar task should be disabled"() {
        given:
        enableJRuby()

        expect:
        project.tasks.findByName('jar').enabled == false
    }
}

class ServiceArtifactPluginWithScalaSpec extends ServiceArtifactPluginSpec {
    boolean hasPlugins(Project project) {
        return (project.plugins.findPlugin('scala') &&
                project.plugins.findPlugin('com.github.johnrengelman.shadow'))
    }

    void enableScala() {
        project.version = '1.0'
        project.service { scala {} }
    }

    def "when using the scala{} closure the plugin should be added"() {
        given:
        enableScala()

        expect:
        hasPlugins(project)
    }

    def "a shadowJar task should not be present"() {
        given:
        enableScala()
        Task shadow = project.tasks.findByName('shadowJar')

        expect:
        shadow == null
    }

    def "a serviceJar task should be present"() {
        given:
        enableScala()
        Task shadow = project.tasks.findByName('serviceJar')

        expect:
        shadow instanceof Task
    }

    def "the default jar task should be disabled"() {
        given:
        enableScala()

        expect:
        project.tasks.findByName('jar').enabled == false
    }
}