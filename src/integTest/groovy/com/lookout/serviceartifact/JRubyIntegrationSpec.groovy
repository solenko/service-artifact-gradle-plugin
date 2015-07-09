package com.github.lookout.serviceartifact


import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import spock.lang.*

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.jrubygradle.jar.JRubyJar
import nebula.test.PluginProjectSpec
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult


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
    protected String version = '1.0'
    protected String projectName = 'fullinteg'

    def setup() {
        settingsFile << "rootProject.name = '${projectName}'"
        buildFile << """
apply plugin: 'com.github.lookout.service-artifact'

version = '${version}'
"""
        /* XXX: why do I have to make the buildDir myself? */
        directory('build')
    }

    Object zipContains(String zipPath, String expectedFile) {
        ZipFile zf = new ZipFile(new File(zipPath, projectDir))
        return zf.entries().find { ZipEntry entry -> entry.name.matches(expectedFile) }
    }

    String zipFileContents(String zipPath, String fileInZip) {
        ZipFile zf = new ZipFile(new File(zipPath, projectDir))
        return zf.getInputStream(zf.getEntry(fileInZip)).text
    }

    Map parseYaml(String yamlString) {
        /* needed for deserializing yaml */
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        return mapper.readValue(yamlString, Map)
    }


    def "the assemble task should produce a tar"() {
        given:
        buildFile << """
service {
  name '${projectName}'
  component('api', type: JRuby) { }
}
"""

        expect:
        runTasksSuccessfully('assemble')
        fileExists("build/distributions/${projectName}-${version}.tar")
    }


    def "assembleApi should produce a valid jar file"() {
        given:
        String jarPath = "build/libs/${projectName}-jruby-${version}.jar"
        createFile('app.rb') << 'puts "hello world"'
        buildFile << """
service {
  name '${projectName}'
  component('api', type: JRuby) {
    include 'app.rb'
  }
}
"""
        expect:
        runTasksSuccessfully('assembleApi')
        fileExists(jarPath)

        and: "it should contain our custom ruby source code"
        zipContains(jarPath, 'app.rb')
    }

    def "assembleApi should produce a valid executable jar"() {
        given:
        String jarPath = "build/libs/${projectName}-jruby-${version}.jar"
        String canaryFile = 'ruby-executed-successfully'
        /* what if we use a groovy string to generate a ruby file? brilliant! */
        createFile('app.rb') << "File.open('${projectDir}/${canaryFile}', 'w+') { |f| f.write('hello') }"
        buildFile << """
service {
  name '${projectName}'
  component('api', type: JRuby) {
    mainScript 'app.rb'
  }
}

task run(type: Exec) {
    commandLine 'java', '-jar', '${jarPath}'
    standardOutput System.err
    dependsOn assembleApi
}
"""
        expect:
        runTasksSuccessfully('run')
        fileExists(canaryFile)
    }

    def "assemble should produce a zip with an etc/metadata.conf file"() {
        given:
        Map metadata = null
        ExecutionResult result = null
        String metadataPath = "${projectName}-${version}/etc/metadata.conf"
        String zipPath = "build/distributions/${projectName}-${version}.zip"
        buildFile << """
service {
  name '${projectName}'
  component('api', type: JRuby) {
  }

  data {
    dependencies 'api-redis'
  }
}
"""
        when:
        result = runTasks('assemble')
        metadata = parseYaml(zipFileContents(zipPath, metadataPath))

        then: "the tasks should have succeeded"
        !result.failure

        and: "the zip should be present"
        fileExists(zipPath)

        and: "it should contain etc/metadata.conf"
        zipContains(zipPath, metadataPath)

        and: "the metadata.conf should be valid YAML"
        metadata instanceof Map
        metadata.component.version == version
        metadata.data.dependencies.contains 'api-redis'

    }

    def "assemble should produce a zip file with a VERSION file"() {
        given:
        String versionPath = "${projectName}-${version}/VERSION"
        JsonSlurper json = new JsonSlurper()
        String zipPath = "build/distributions/${projectName}-${version}.zip"
        buildFile << """
service {
  name '${projectName}'
  component('api', type: JRuby) {
  }
}
"""
        expect:
        runTasksSuccessfully('assemble')
        fileExists(zipPath)

        and: "it should contain VERSION"
        zipContains(zipPath, versionPath)

        and: "the VERSION file should be JSON"
        json.parseText(zipFileContents(zipPath, versionPath)) instanceof Map
    }
}
