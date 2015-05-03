package com.github.lookout.serviceartifact

import spock.lang.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 */
class JRubyServiceExtensionSpec extends Specification {
    protected Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "it should implement AbstractServiceExtension"() {
        given:
        def ext = new JRubyServiceExtension(project)

        expect:
        ext instanceof AbstractServiceExtension
    }

    def "its static getClosureName() method should return 'jruby'"() {
        expect:
        JRubyServiceExtension.closureName == 'jruby'
    }
}

class JRubyServiceExtensionPluginsSpec extends JRubyServiceExtensionSpec {
    boolean hasPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar') &&
                project.plugins.findPlugin('com.github.johnrengelman.shadow'))
    }

    def "applyPlugins should install the necessary plugins"() {
        given:
        JRubyServiceExtension ext = new JRubyServiceExtension(project)

        when:
        ext.applyPlugins()

        then:
        hasPlugins(project)
    }

}
