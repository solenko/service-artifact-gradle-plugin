package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

import com.github.lookout.serviceartifact.scm.GerritHandler

abstract class AppliedExtensionSpec extends Specification {
    protected Project project

    def setup() {
        this.project = ProjectBuilder.builder().build()
        this.project.apply plugin: 'com.github.lookout.service-artifact'
    }
}

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

    def "getScmHandler() instantiate a handler instance"() {
        given: "we're inside of a Gerrit-triggered Jenkins job"
        def ext = new ServiceArtifactExtension(this.project, gerritEnv())

        expect:
        ext.scmHandler instanceof GerritHandler
    }

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

    def "bootstrap() should define a serviceVersionInfo task"() {
        given:
        def extension = new ServiceArtifactExtension(this.project)

        when:
        extension.bootstrap()

        then:
        this.project.tasks.findByName('serviceVersionInfo')
    }

    @Ignore
    def "bootstrap() should define serviceMetadata"() {
        given:
        def extension = new ServiceArtifactExtension(this.project)

        when:
        extension.bootstrap()

        then:
        this.project.tasks.findByName('serviceMetadata')
    }


    def "generateVersionMap()"() {
        given:
        def extension = new ServiceArtifactExtension(this.project)
        Map versionMap = extension.generateVersionMap()

        expect:
        versionMap instanceof Map
        versionMap['version']
        versionMap['name']
        versionMap['buildDate']
        versionMap['revision']
        versionMap['builtOn']
    }
}

class ExtensionIntegrationSpec extends AppliedExtensionSpec {

    def "bootstrap() should have set up dependencies for serviceVersionInfo"() {
        given:
        Closure matcher = { (it instanceof Task) && (it.name == 'serviceVersionInfo') }
        Task zip = this.project.tasks.findByName('serviceZip')
        Task tar = this.project.tasks.findByName('serviceTar')

        expect: "the compressed archives to rely on serviceVersionInfo"
        zip.dependsOn.find(matcher)
        tar.dependsOn.find(matcher)
    }
}


/**
 * Test the functionality of the service { metadata [:] } property
 */
class ServiceArtifactExtensionMetadataSpec extends AppliedExtensionSpec {
    def "its metadata should be a map by default"() {
        expect:
        this.project.service.metadata instanceof Map
    }

    def "I should be able to set metadata"() {
        given:
        this.project.service.metadata 'success' : true

        expect:
        this.project.service.metadata == ['success' : true]
    }

    def "I should be able to completely overwrite it with setMetadata"() {
        given:
        this.project.service.metadata 'overwrite' : 1

        when:
        this.project.service.setMetadata 'success' : true

        then:
        this.project.service.metadata == ['success' : true]
    }
}

/**
 * Test the functionality of the service { jruby{} } closure
 */
class ServiceArtifactExtensionJRubyIntegrationSpec extends AppliedExtensionSpec {
    def setup() {
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

    def "the serviceTar task should depend on serviceJar"() {
        given:
        Task tar = project.tasks.findByName('serviceTar')

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
