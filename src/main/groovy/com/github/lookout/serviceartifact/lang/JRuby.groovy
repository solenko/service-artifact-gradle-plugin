package com.github.lookout.serviceartifact.lang

import com.github.lookout.serviceartifact.AbstractServiceExtension
import com.github.lookout.serviceartifact.ServiceArtifactPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.Task

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
                        serviceJar.into(path) { from path }
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
            this.project.tasks.getByName('jrubyJavaBootstrap') {
                jruby {
                    initScript = script
                }
            }
        }
    }

    JRuby(Project project) {
        this.project = project
    }

    void apply(Object serviceExtension, Closure extraConfig) {
        applyPlugins()

        setupJRubyShadowJar()
        setupCompressedArchives(this.project, serviceExtension.scmHandler)
        disableJarTask()

        extraConfig.delegate = new JRubyDSL(this.project)
        extraConfig.call(this)
    }

    void applyPlugins(Project project) {
        this.project.apply plugin: 'com.github.jruby-gradle.base'
        this.project.apply plugin: 'com.github.jruby-gradle.jar'

        /* The java (or groovy) plugin is a pre-requisite for the shadowjar plugin
         * to properly initialize with a shadowJar{} task
         */
        this.project.apply plugin: 'java'
        this.project.apply plugin: 'com.github.johnrengelman.shadow'
    }

    /**
     * Set up the shadowJar task for packaging up a JRuby-based artifact
     */
    protected void setupJRubyShadowJar() {
        removeDefaultShadowTask(this.project)

        Task jar = this.project.task(JAR_TASK, type: ShadowJar) {
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

            mergeServiceFiles()

            jruby {
                defaultMainClass()
                defaultGems()
            }
        }

        /* Add the configuration which includes the proper JRuby-related dependencies
         * from the jruby-gradle-jar-plugin
         */
        jar.configurations.add(this.project.configurations.getByName('jrubyJar'))
    }

    protected void removeDefaultShadowTask(Project project) {
        ShadowJar shadow = project.tasks.findByName('shadowJar')
        project.tasks.remove(shadow)
    }

}
