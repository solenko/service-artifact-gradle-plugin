package com.github.lookout.serviceartifact.lang

import spock.lang.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import com.github.lookout.serviceartifact.AbstractServiceExtension

/**
 */
class JRubySpec extends Specification {
    protected Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "it should implement AbstractServiceExtension"() {
        given:
        def ext = new JRuby(project)

        expect:
        ext instanceof AbstractServiceExtension
    }
}

class JRubyPluginsSpec extends JRubySpec {
    boolean hasPlugins(Project project) {
        return (project.plugins.findPlugin('com.github.jruby-gradle.base') &&
                project.plugins.findPlugin('com.github.jruby-gradle.jar') &&
                project.plugins.findPlugin('com.github.johnrengelman.shadow'))
    }

    def "applyPlugins should install the necessary plugins"() {
        given:
        JRuby ext = new JRuby(project)

        when:
        ext.applyPlugins()

        then:
        hasPlugins(project)
    }

}
