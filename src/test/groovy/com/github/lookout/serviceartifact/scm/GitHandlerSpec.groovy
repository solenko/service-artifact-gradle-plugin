package com.github.lookout.serviceartifact.scm

import org.eclipse.jgit.errors.RepositoryNotFoundException
import spock.lang.*

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Repository

class GitHandlerSpec extends Specification {
    def handler

    def setup() {
        this.handler = Spy(GitHandler, constructorArgs: [null, [:]])
    }

    def "isAvailable() should be false by default"() {
        given:
        1 * handler.getProperty('git') >> null

        expect:
        !handler.isAvailable()
    }


    def "isAvailable() should be true if .git is present"() {
        given:
        def gitMock = Mock(Grgit)
        1 * handler.getProperty('git') >> gitMock

        expect:
        handler.isAvailable()
    }


    def "annotatedVersion() when .git is NOT present should no-op"() {
        given:
        1 * handler.findGitRoot(_) >> null

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0'
    }

    def "annotatedVersion() when .git is present should include SHA+1"() {
        given:
        Repository repoMock = GroovyMock()
        1 * repoMock.head() >> repoMock
        1 * repoMock.getAbbreviatedId() >> '0xdeadbeef'
        _ * handler.getProperty('git') >> repoMock

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0+0xdeadbeef'
    }

    def "annotateVersion() when a Jenkins BUILD_NUMBER is available should include it"() {
        given:
        _ * handler.getProperty('environment') >> ['BUILD_NUMBER' : '1']
        1 * handler.findGitRoot(_) >> null

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0.1'
    }
}
