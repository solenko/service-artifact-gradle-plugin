package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.Plugin
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

    def "project should have the application plugin"() {
        expect:
        project.plugins.findPlugin('application')
    }

    def "project should have the git plugin"() {
        expect:
        project.plugins.findPlugin('org.ajoberstar.release-base')
    }

    def "project should include the service{} DSL"() {
        expect:
        project.service instanceof ServiceArtifactExtension
    }
}
