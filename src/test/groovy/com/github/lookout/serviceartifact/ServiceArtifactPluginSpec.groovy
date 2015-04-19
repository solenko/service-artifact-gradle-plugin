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
}


class ServiceArtifactPluginWithJRubySpec extends ServiceArtifactPluginSpec {
    def "when using the jruby{} closure the plugin should be added"() {
        given:
        project.service {
            jruby {}
        }

        expect:
        project.plugins.findPlugin('com.github.jruby-gradle.base')
    }

    def "using useJRuby() should work like jruby{}"() {
        given:
        project.service {
            useJRuby()
        }

        expect:
        project.plugins.findPlugin('com.github.jruby-gradle.base')
    }
}
