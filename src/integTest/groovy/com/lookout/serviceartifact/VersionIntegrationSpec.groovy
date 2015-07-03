package com.github.lookout.serviceartifact

import spock.lang.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class VersionIntegrationSpec extends Specification {
    protected Project project

    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.github.lookout.service-artifact'
    }


    def "I should be able to just use service.version() if I want to"() {
        given:
        project.version = project.service.version('9000')
        project.service {
            name 'foo'
        }

        when:
        project.evaluate()

        then:
        project.version == '9000'
    }
}