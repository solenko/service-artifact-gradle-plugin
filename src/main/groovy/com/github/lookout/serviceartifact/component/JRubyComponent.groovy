package com.github.lookout.serviceartifact.component

import com.github.lookout.serviceartifact.AbstractComponent
import com.github.lookout.serviceartifact.ServiceArtifactPlugin
import org.gradle.api.Project
import org.gradle.api.Task

import com.github.jrubygradle.jar.JRubyJar
import org.gradle.api.tasks.Optional

/**
 * THe JRubyComponent provides all the necessary tasks and configuration to create
 * an appropriate JRuby-based service artifact
 *
 * It can be used/accessed via:
 *      service {
 *          name 'fancyserv'
 *
 *          component('rubyapi', type: JRuby) {
 *              mainScript 'app/main.rb'
 *              include 'lib', 'app'
 *          }
 *      }
 */
class JRubyComponent extends AbstractComponent {
    String mainScript = null

    @Override
    void apply(Project project, Object ext, String name) {
        super.apply(project, ext, name)

        project.apply plugin: 'com.github.jruby-gradle.base'
        project.apply plugin: 'com.github.jruby-gradle.jar'
        project.apply plugin: 'java'

        artifactTask = createJRubyJarTask(project, computeArtifactTaskName(name))

        /* Let's make sure assemble exists in some form so we can chain off it */
        project.tasks.maybeCreate('assemble').dependsOn(artifactTask)
    }

    /**
     * Set the Ruby script which will act as an entry point for the artifact
     *
     * @param mainRbScript Full or relative path to a Ruby script which JRuby can execute
     */
    @Optional
    void mainScript(String mainRbScript) {
        logger.info("Using ${mainRbScript} as the entry point for the JRuby artifact")
        this.mainScript = mainRbScript
        artifactTask.jruby {
            initScript mainRbScript
        }
    }

    /**
     * Include files of any format inside of the artifact
     */
    void include(Object... arguments) {
        if (arguments == null) {
            return
        }

        arguments.each { Object path ->
            /* If the arg is a directory, we want to put that into the jar
             * with the same name, not changing the path at all
             */
            if ((new File(path)).isDirectory()) {
                artifactTask.from(this.project.projectDir) { include "${path}/**" }
            }
            else {
                artifactTask.from(this.project.projectDir) { include path }
            }
        }
    }

    protected String computeArtifactTaskName(String componentName) {
        return String.format("assemble%s", componentName.capitalize())
    }

    protected Task createJRubyJarTask(Project project, String taskName) {
        logger.info("Defining a task named ${taskName} of type JRubyJar")

        Task jar = project.task(taskName, type: JRubyJar) {
            group ServiceArtifactPlugin.GROUP_NAME
            description "Build a JRuby-based service jar"

            /* Include our Ruby code into the tree */
            from("${project.projectDir}/src/main/ruby")
            /* Include our main source sets output, including the JarBootstrap code */
            from(project.sourceSets.main.output)

            /* Exclude some basic stupid files from making their way in */
            exclude '*.swp', '*.gitkeep', '*.md',
                    'META-INF/INDEX.LIST', 'META-INF/*.SF',
                    'META-INF/*.DSA', 'META-INF/*.RSA'
        }

        project.afterEvaluate {
            String mainScript = this.mainScript

            jar.jruby {
                defaultMainClass()
                defaultGems()
                /* We will default to a runnable() jar unless somebody tells
                 * us otherwise
                 */
                initScript mainScript ?: runnable()
            }
        }

        return jar
    }
}
