package com.github.lookout.serviceartifact

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.lookout.serviceartifact.scm.AbstractScmHandler

/**
 * ServiceArtifactExtension provides the service{} DSL into Gradle files which
 * use the plugin
 */
class ServiceArtifactExtension {
    protected final Project project
    protected final Map<String, String> env
    protected Logger logger = LoggerFactory.getLogger(ServiceArtifactExtension.class)
    /** List of scm handler classes, in priority order */
    private final List<Class<AbstractScmHandler>> scmHandlerImpls = [
            scm.GerritHandler.class,
            scm.GitHandler.class,
    ]
    /** SCM Handler appropriate for this execution */
    protected AbstractScmHandler _scmHandler


    ServiceArtifactExtension(final Project project) {
        this(project, [:])
    }

    ServiceArtifactExtension(final Project project,
                            final Map<String, String> env) {
        this.project = project
        this.env = env
    } 

    /**
     * Lazily look up our SCM Handler
     */
    AbstractScmHandler getScmHandler() {
        if (this._scmHandler != null) {
            return this._scmHandler
        }

        this.scmHandlerImpls.find {
            AbstractScmHandler handler = it.build(this.env)

            if (handler.isAvailable()) {
                this._scmHandler = handler
                return true
            }
        }

        return this._scmHandler
    }

    /** Enable the building of a JRuby service artifact */
    void jruby(Closure c) {
        this.project.apply plugin: 'com.github.jruby-gradle.base'
        this.project.apply plugin: 'com.github.jruby-gradle.jar'

        setupJRubyShadowJar()
        disableJarTask()
    }

    void useJRuby() {
        this.jruby {}
    }

    /**
     * Set up the shadowJar task for packaging up a JRuby-based artifact
     */
    protected void setupJRubyShadowJar() {
        /* The java (or groovy) plugin is a pre-requisite for the shadowjar plugin
         * to properly initialize with a shadowJar{} task
         */
        this.project.apply plugin: 'java'
        this.project.apply plugin: 'com.github.johnrengelman.shadow'
        ShadowJar shadow = this.project.tasks.findByName('shadowJar')
        this.project.tasks.remove(shadow)

        Task jar = this.project.task('serviceJar', type: ShadowJar) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Build a JRuby-based service jar"

            /* Include our Ruby code into the tree */
            from("${this.project.projectDir}/src/main/ruby")
            /* Include our main source sets output, including the JarBootstrap code */
            from(this.project.sourceSets.main.output)

            /* Exclude some basic stupid files from making their way in */
            exclude '*.sw*', '*.gitkeep', '*.md',
                    'META-INF/INDEX.LIST', 'META-INF/*.SF',
                    'META-INF/*.DSA', 'META-INF/*.RSA'
            dependsOn this.project.tasks.findByName('assemble'),
                        this.project.tasks.findByName('compileJava')

            jruby {
                defaultMainClass()
                defaultGems()
            }
        }
        jar.configurations.add(this.project.configurations.getByName('jrubyJar'))

        Task tar = this.project.tasks.findByName('serviceTarGz')
        Task zip = this.project.tasks.findByName('serviceZip')

        [tar, zip].each {
            it.dependsOn(jar)
            it.from(jar.outputs.files)
        }
    }

    protected void disableJarTask() {
        Task jarTask = this.project.tasks.findByName('jar')

        if (jarTask instanceof Task) {
            jarTask.enabled = false
        }
    }

    /**
     * Return the appropriately computed version string based on our executing
     * environment
     */
    String version(final String baseVersion) {
        if (this.scmHandler instanceof AbstractScmHandler) {
            return this.scmHandler.annotatedVersion(baseVersion)
        }

        return baseVersion
    }
}

