package com.github.lookout.serviceartifact.scm

import spock.lang.*

import org.ajoberstar.grgit.Grgit

class GitHandlerSpec extends Specification {
    def "isAvailable() should be false by default"() {
        given:
        def handler = Spy(GitHandler, constructorArgs: [[:]])
        1 * handler.getProperty('git') >> null

        expect:
        !handler.isAvailable()
    }


    def "isAvailable() should be true if .git is present"() {
        given:
        def handler = Spy(GitHandler, constructorArgs: [[:]])
        def gitMock = Mock(Grgit)
        1 * handler.getProperty('git') >> gitMock

        expect:
        handler.isAvailable()
    }


    def "annotatedVersion() when .git is NOT present should no-op"() {
        given:
        def handler = new GitHandler([:])

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0'
    }

    @Ignore
    def "annotatedVersion() when .git is present should include SHA+1"() {
        given:
        def handler = new GitHandler([:])

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0+0xdeadbeef'
    }
}
