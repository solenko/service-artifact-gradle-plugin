package com.github.lookout.serviceartifact

import org.gradle.api.Project

/**
 * Implement the "jruby {}" configurator in the service extension
 *
 */
class JRubyServiceExtension extends AbstractServiceExtension {
    static final String CLOSURE_NAME = 'jruby'
    protected Project project

    /**
     * Return the String name of the configuration closure
     */
    static String getClosureName() {
        return CLOSURE_NAME
    }

    JRubyServiceExtension(Project project) {
        this.project = project
    }

    /**
     * Configure the JRuby service
     *
     * @param configClosure
     */
    void configure(Closure configClosure) {
        return
    }

    /**
     * To be used by something like "useJRuby()" which would just set
     * everything to a documented set of defaults
     */
    void configureWithDefaults() {
        return
    }


    void applyPlugins() {
        this.project.apply plugin: 'com.github.jruby-gradle.base'
        this.project.apply plugin: 'com.github.jruby-gradle.jar'
        /* The java (or groovy) plugin is a pre-requisite for the shadowjar plugin
         * to properly initialize with a shadowJar{} task
         */
        this.project.apply plugin: 'java'
        this.project.apply plugin: 'com.github.johnrengelman.shadow'
    }
}
