package com.github.lookout.serviceartifact.scm

import spock.lang.*

class GerritHandlerSpec extends Specification {

    Map<String, String> gerritEnv() {
        return [
                'GERRIT_CHANGE_NUMBER' : 1,
                'GERRIT_PATCHSET_NUMBER' : 1,
                'GERRIT_PATCHSET_REVISION' : '0xdeadbeef',
                ]
    }

    def "isAvailable() should be false by default"() {
        given:
        def handler = new GerritHandler([:])

        expect:
        !handler.isAvailable()
    }

    def "isAvailable() should be true if the env has Gerrit env vars"() {
        given:
        def handler = new GerritHandler(gerritEnv())

        expect:
        handler.isAvailable()
    }

    def "getRevision() should return an empty string by default"() {
        given:
        def handler = new GerritHandler([:])

        expect:
        handler.revision == ''
    }

    def "getRevision() should return the GERRIT_PATCHSET_REVISION when present"() {
        given:
        def handler = new GerritHandler(gerritEnv())

        expect:
        handler.revision == gerritEnv()['GERRIT_PATCHSET_REVISION']
    }

    def "annotatedVersion() should include change and patchset numbers, and SHA1"() {
        given:
        def handler = new GerritHandler(gerritEnv())

        when:
        String version = handler.annotatedVersion('1.0')

        then:
        version == '1.0.1.1+0xdeadbeef'
    }
}
