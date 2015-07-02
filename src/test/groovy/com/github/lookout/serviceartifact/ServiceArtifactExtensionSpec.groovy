package com.github.lookout.serviceartifact

import org.gradle.api.tasks.StopExecutionException
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
    protected ServiceArtifactExtension extension

    def setup() {
        this.project = ProjectBuilder.builder().build()
        this.extension = new ServiceArtifactExtension(this.project)
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
        when:
        extension.bootstrap()

        then:
        this.project.tasks.findByName('serviceVersionInfo')
    }

    def "bootstrap() should define serviceMetadata"() {
        when:
        extension.bootstrap()

        then:
        this.project.tasks.findByName('serviceMetadata')
    }

    def "name() should add service:name to serviceMetadata"() {
        given:
        String serviceName = 'sample-service'

        when:
        extension.name serviceName

        then:
        extension.name == serviceName
    }

    def "dependencies() should take a list of strings"() {
        when:
        extension.dependencies 'one', 'two'

        then:
        extension.dependencies == ['one', 'two']
    }

    def "validateMetadata() should raise an exception if properties are not set"() {
        when:
        extension.validateMetadata()

        then:
        thrown(StopExecutionException)
    }

    def "validateMetadata() should not raise if name has been provided"() {
        given:
        extension.name 'some-name'

        expect:
        extension.validateMetadata()
    }

    def "generateVersionMap()"() {
        given:
        Map versionMap = extension.generateVersionMap()

        expect:
        versionMap instanceof Map
        versionMap['version']
        versionMap['name']
        versionMap['buildDate']
        versionMap['revision']
        versionMap['builtOn']
    }

    def "component() DSL method must be given a 'type:' keyword-argument"() {
        when:
        extension.component('api', type: null) { }

        then:
        thrown(StopExecutionException)
    }

    def "data() DSL method with dependencies should store them"() {
        when:
        extension.data { dependencies 'redis' }

        then:
        extension.data.dependencies.contains 'redis'
    }

    def "data() DSL method with migrations should store them"() {
        when:
        extension.data { migrations "path/to/some.sql" }

        then:
        extension.data.migrations.contains "path/to/some.sql"
    }

    def "data() DSL method should allow me to use files() for migrations"() {
        given:
        File migration = new File(project.projectDir, 'test.sql')
        migration.withWriter { Writer w -> w.write("hello world") }

        when:
        extension.data { migrations fileTree(dir: projectDir, include: '*.sql') }

        then:
        !extension.data.migrations.isEmpty()
    }
}

@Title("Verify complex behaviors manifested by the ServiceArtifactExtension")
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

    def "bootstrap() should have set up dependencies for serviceMetadata"() {
        given:
        Closure matcher = { (it instanceof Task) && (it.name == 'serviceMetadata') }
        Task zip = this.project.tasks.findByName('serviceZip')
        Task tar = this.project.tasks.findByName('serviceTar')

        expect: "the compressed archives to rely on serviceVersionInfo"
        zip.dependsOn.find(matcher)
        tar.dependsOn.find(matcher)
    }
}


/**
 * Test the functionality of the service { jruby{} } closure
 */
@Title("Verify ServiceArtifactExtension handles JRuby components properly")
class ServiceArtifactExtensionJRubyIntegrationSpec extends AppliedExtensionSpec {
    protected String componentName = 'api'

    def setup() {
        this.project.service {
            name 'spockJRuby'
            component(componentName, type: JRuby) {
            }
        }
    }

    def "an archiveTask should be present"() {
        expect: "a task named 'assembleApi' exists since our component is named 'api'"
        project.tasks.findByName('assembleApi')
    }

    @Ignore
    def "the serviceJar baseName should be the same as the project name"() {
        given:
        project = ProjectBuilder.builder().withName('spock-shadow').build()

        when:
        this.project.apply plugin: 'com.github.lookout.service-artifact'
        this.project.service { jruby {} }

        then:
        project.tasks.findByName('serviceJar').baseName == project.name
    }

    def "the service artifact has a manifest"() {
        given:
        Task task = project.tasks.findByName('assembleApi')

        when:
        project.evaluate()

        then:
        task.manifest.attributes['Main-Class']
    }

    def "the serviceTar task should depend on the service artifact task"() {
        given:
        Task tar = project.tasks.findByName('serviceTar')
        String artifactTaskName = 'assembleApi'

        expect:
        tar.dependsOn.find { (it instanceof Task) && (it.name == artifactTaskName) }
    }

    def "the serviceZip task should depend on the service artifact task"() {
        given:
        Task tar = project.tasks.findByName('serviceZip')
        String artifactTaskName = 'assembleApi'

        expect:
        tar.dependsOn.find { (it instanceof Task) && (it.name == artifactTaskName) }
    }
}
