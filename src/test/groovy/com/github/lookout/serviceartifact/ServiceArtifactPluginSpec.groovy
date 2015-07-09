package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
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

    @Issue('https://github.com/lookout/service-artifact-gradle-plugin/issues/18')
    @Ignore('Waiting for this issue to be resolved: https://github.com/nebula-plugins/gradle-dependency-lock-plugin/issues/56')
    def "project should have the dependency-lock plugin"() {
        expect:
        project.plugins.findPlugin('nebula.dependency-lock')
    }

    def "project should NOT have the jruby-gradle base plugin by default"() {
        expect:
        !project.plugins.findPlugin('com.github.jruby-gradle.base')
    }

    def "project should include the service{} DSL"() {
        expect:
        project.service instanceof ServiceArtifactExtension
    }

    def "project should include the serviceTar task"() {
        given:
        Task t = project.tasks.findByName('serviceTar')

        expect:
        t instanceof Tar
        t.group == ServiceArtifactPlugin.GROUP_NAME
    }

    def "project should include the serviceZip task"() {
        given:
        Task t = project.tasks.findByName('serviceZip')

        expect:
        t instanceof Zip
        t.group == ServiceArtifactPlugin.GROUP_NAME
    }

    def "project should include a prepareServiceScripts task"() {
        given:
        Task t = project.tasks.findByName('prepareServiceScripts')

        expect:
        t instanceof Task
        t.group == ServiceArtifactPlugin.GROUP_NAME
    }

    def "project should include a assembleService task"() {
        given:
        Task t = project.tasks.findByName('assembleService')

        expect:
        t instanceof Task
        t.group == ServiceArtifactPlugin.GROUP_NAME
    }

    @Issue('https://github.com/lookout/service-artifact-gradle-plugin/issues/22')
    def "project should have a publishService task"() {
        given:
        Task t = project.tasks.findByName('publishService')

        expect:
        t instanceof Task
        t.group == ServiceArtifactPlugin.GROUP_NAME
        t.dependsOn.contains(project.tasks.findByName('uploadServiceArchives'))
    }

    @Ignore
    @Issue('https://github.com/lookout/service-artifact-gradle-plugin/issues/24')
    def "assembleService should have outputs"() {
        given:
        Task t = project.tasks.findByName('assembleService')

        expect:
        !t.outputs.files.isEmpty()
    }

    def "project should include a serviceArchives configuration"() {
        expect:
        project.configurations.findByName('serviceArchives')
    }

    def "artifacts{} should include the tar and zip archives"() {
        given:
        def c = project.configurations.findByName('serviceArchives')

        expect:
        c.artifacts.find { it.archiveTask.is project.tasks.findByName('serviceZip')}
        c.artifacts.find { it.archiveTask.is project.tasks.findByName('serviceTar')}
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

    def "artifacts{} should include the jar archive"() {
        given:
        enableScala()
        def c = project.configurations.findByName('serviceArchives')

        expect:
        c.artifacts.find { it.archiveTask.is project.tasks.findByName('serviceJar')}
    }
}
