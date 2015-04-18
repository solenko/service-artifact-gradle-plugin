package com.github.lookout

import spock.lang.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ServiceArtifactExtensionSpec extends Specification {
    protected Project project

    def setup() {
        this.project = ProjectBuilder.builder().build()
    }
    
    def "should be construct-able"() {
        given:
        def extension = new ServiceArtifactExtension(this.project)

        expect:
        extension instanceof ServiceArtifactExtension
    }
}


class ServiceArtifactExtensionInstanceSpec extends ServiceArtifactExtensionSpec {
    protected ServiceArtifactExtension extension

    def setup() {
        this.extension = new ServiceArtifactExtension(this.project)
    }

    def "isUnderGerrit() should be false by default"() {
        expect:
        !this.extension.isUnderGerrit()
    }

    def "isUnderGerrit() should be false if the env has Gerrit environment vars"() {
        given:
        this.extension = new ServiceArtifactExtension(this.project,
            ['GERRIT_CHANGE_NUMBER' : 23])

        expect:
        this.extension.isUnderGerrit()
    }
}

class ServiceArtifactExtensionVersionSpec extends ServiceArtifactExtensionSpec {
    ServiceArtifactExtension extension

    def "version() should return an unmolested string by default"() {
        given:
        this.extension = new ServiceArtifactExtension(this.project)

        when:
        String version = this.extension.version('1.0')

        then:
        version == '1.0'
    }

    def "version() in a Gerrit build should update the version properly"() {
        given:
        this.extension = new ServiceArtifactExtension(this.project, [
            'GERRIT_CHANGE_NUMBER' : 23,
            'GERRIT_PATCHSET_NUMBER' : 1,
        ])

        when:
        String version = this.extension.version('1.0')

        then:
        version == '1.0.23.1'
    }
}
