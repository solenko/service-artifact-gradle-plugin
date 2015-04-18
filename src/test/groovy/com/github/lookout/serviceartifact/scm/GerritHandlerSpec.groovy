package com.github.lookout.serviceartifact.scm

import spock.lang.*

class GerritHandlerSpec extends Specification {
    protected GerritHandler handler

    def "isAvailable() should be false by default"() {
        given:
        this.handler = new GerritHandler([:])

        expect:
        !this.handler.isAvailable()
    }

    def "isAvailable() should be true if the env has Gerrit env vars"() {
        given:
        this.handler = new GerritHandler([
                            'GERRIT_CHANGE_NUMBER' : 1,
                            ])
        expect:
        this.handler.isAvailable()
    }

    def "annotatedVersion() should include change and patchset numbers, and SHA1"() {
        given:
        this.handler = new GerritHandler([
                            'GERRIT_CHANGE_NUMBER' : 1,
                            'GERRIT_PATCHSET_NUMBER' : 1,
                            'GERRIT_PATCHSET_REVISION' : '0xdeadbeef',
                            ])

        when:
        String version = this.handler.annotatedVersion('1.0')

        then:
        version == '1.0.1.1+0xdeadbeef'
    }
}
