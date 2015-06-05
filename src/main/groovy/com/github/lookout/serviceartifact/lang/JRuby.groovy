package com.github.lookout.serviceartifact.lang

import com.github.lookout.serviceartifact.AbstractServiceExtension
import com.github.lookout.serviceartifact.ServiceArtifactPlugin
import org.gradle.api.Project
import org.gradle.api.Task

import com.github.jrubygradle.jar.JRubyJar

/**
 * Implement the "jruby {}" configurator in the service extension
 *
 */
class JRuby extends AbstractServiceExtension {
    static final String CLOSURE_NAME = 'jruby'
    final String JAR_TASK = 'serviceJar'

    protected Project project

    /**
     * Class encapsulating the DSL inside of the service { jruby {} } closure
     */
    class JRubyDSL {
        protected Project project

        JRubyDSL(Project project) {
            this.project = project
        }

        /**
         * Include the listed directories and files inside of the JRuby-based artifact
         * @param args
         */
        void include(Object ... args) {
            if (args != null) {
                Task serviceJar = this.project.tasks.getByName(JAR_TASK)
                args.each { Object path ->
                    /* If the arg is a directory, we want to put that into the jar
                     * with the same name, not changing the path at all
                     */
                    if ((new File(path)).isDirectory()) {
                        serviceJar.from(this.project.projectDir) { include "${path}/**" }
                    }
                    else {
                        serviceJar.from path
                    }
                }
            }
        }

        /**
         * Provide a custom start script for the executable jar artifact
         */
        void mainScript(String script) {
            Task jar = this.project.tasks.getByName(JAR_TASK)
            jar.jruby {
                initScript script
            }
        }
    }

    JRuby(Project project) {
        this.project = project
    }

    void apply(Object serviceExtension, Closure extraConfig) {
        applyPlugins()

        disableJarTask()
        setupJRubyJar()
        setupCompressedArchives(this.project, serviceExtension.scmHandler)

        extraConfig.delegate = new JRubyDSL(this.project)
        extraConfig.call(this.project)
    }

    void applyPlugins(Project project) {
        this.project.apply plugin: 'com.github.jruby-gradle.base'
        this.project.apply plugin: 'com.github.jruby-gradle.jar'
        this.project.apply plugin: 'java'
    }

    protected void setupJRubyJar() {
        Task jar = this.project.task(JAR_TASK, type: JRubyJar) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Build a JRuby-based service jar"

            /* Include our Ruby code into the tree */
            from("${this.project.projectDir}/src/main/ruby")
            /* Include our main source sets output, including the JarBootstrap code */
            from(this.project.sourceSets.main.output)

            /* Exclude some basic stupid files from making their way in */
            exclude '*.swp', '*.gitkeep', '*.md',
                    'META-INF/INDEX.LIST', 'META-INF/*.SF',
                    'META-INF/*.DSA', 'META-INF/*.RSA'

            dependsOn this.project.tasks.findByName('assemble')

            jruby {
                defaultMainClass()
                defaultGems()
                /* We will default to a runnable() jar unless somebody tells
                 * us otherwise
                 */
                initScript runnable()
            }
        }
    }
}
