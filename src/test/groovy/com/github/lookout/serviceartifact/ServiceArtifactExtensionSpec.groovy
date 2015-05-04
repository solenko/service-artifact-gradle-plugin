package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

class ServiceArtifactExtensionSpec extends Specification {
    protected Project project

    def setup() {
        this.project = ProjectBuilder.builder().build()
    }


    /** Return a sample Gerrit environment for testing Gerrit specific
     * behaviors */
    Map<String, String> gerritEnv() {
        return [
                'GERRIT_CHANGE_NUMBER' : 1,
                'GERRIT_PATCHSET_NUMBER' : 1,
                'GERRIT_PATCHSET_REVISION' : '0xded']
    }

    def "should be construct-able"() {
        given:
        def extension = new ServiceArtifactExtension(this.project)

        expect:
        extension instanceof ServiceArtifactExtension
    }
}


class ServiceArtifactExtensionInstanceSpec extends ServiceArtifactExtensionSpec {

    def "getScmHandler() instantiate a handler instance"() {
        given: "we're inside of a Gerrit-triggered Jenkins job"
        def ext = new ServiceArtifactExtension(this.project, gerritEnv())

        expect:
        ext.scmHandler instanceof scm.GerritHandler
    }

}

class ServiceArtifactExtensionVersionSpec extends ServiceArtifactExtensionSpec {

    def "version() should return an unmolested string by default"() {
        given:
        def ext = Spy(ServiceArtifactExtension, constructorArgs: [this.project])
        1 * ext.getScmHandler() >> null

        when:
        String version = ext.version('1.0')

        then:
        version == '1.0'
    }

    def "version() in a Gerrit build should update the version properly"() {
        given:
        def ext = new ServiceArtifactExtension(this.project, gerritEnv())

        when:
        String version = ext.version('1.0')

        then:
        version == '1.0.1.1+0xded'
    }
}

class ServiceArtifactExtensionShadowSpec extends Specification {
    protected Project project

    def setup() {
        this.project = ProjectBuilder.builder().build()
        this.project.apply plugin: 'com.github.lookout.service-artifact'
        this.project.service { jruby {} }
    }

    def "the serviceJar task must be present"() {
        expect:
        project.tasks.findByName('serviceJar')
    }

    def "the serviceJar baseName should be the same as the project name"() {
        given:
        project = ProjectBuilder.builder().withName('spock-shadow').build()

        when:
        this.project.apply plugin: 'com.github.lookout.service-artifact'
        this.project.service { jruby {} }

        then:
        project.tasks.findByName('serviceJar').baseName == project.name
    }

    def "the serviceJar has a manifest"() {
        given:
        def task = project.tasks.findByName('serviceJar')

        expect:
        task.manifest.attributes['Main-Class']

    }
}

/**
 * Test the behaviors of the setupCompressedArchives() functionality
 */
class ServiceArtifactExtensionArchivesSpec extends Specification {
    protected Project project

    def setup() {
        this.project = ProjectBuilder.builder().build()
        this.project.apply plugin: 'com.github.lookout.service-artifact'
        this.project.service { jruby {} }
    }

    def "the serviceTarGz task should depend on serviceJar"() {
        given:
        Task tar = project.tasks.findByName('serviceTarGz')

        expect:
        tar.dependsOn.find { (it instanceof Task) && (it.name == 'serviceJar') }
    }

    def "the serviceZip task should depend on serviceJar"() {
        given:
        Task tar = project.tasks.findByName('serviceZip')

        expect:
        tar.dependsOn.find { (it instanceof Task) && (it.name == 'serviceJar') }
    }
}

