package com.github.lookout.serviceartifact.lang

import com.github.lookout.serviceartifact.AbstractServiceExtension
import com.github.lookout.serviceartifact.ServiceArtifactPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


/**
 */
class Scala extends AbstractServiceExtension {
    protected Project project
    /* TODO: make this a better property like Schalke has taught me */
    private String mainClass = ''

    void mainClass(String className) {
        this.mainClass = className
    }

    Scala(Project project) {
        this.project = project
    }

    void apply(Object serviceExtension, Closure configClosure) {
        this.project.apply plugin: 'scala'
        this.project.apply plugin: 'com.github.johnrengelman.shadow'

        Closure config = configClosure.clone()
        config.delegate = this
        config()

        removeDefaultShadowTask(this.project)

        Task jar = this.project.task('serviceJar', type: ShadowJar) {
            manifest { attributes 'Main-Class' : this.mainClass }
            group ServiceArtifactPlugin.GROUP_NAME
            description "Build a Scala-based service jar"

            from(this.project.sourceSets.main.output)
        }
        this.project.tasks.findByName('assemble').dependsOn(jar)
        this.project.artifacts.add(ServiceArtifactPlugin.ARCHIVES_CONFIG, jar)
        jar.configurations.add(this.project.configurations.getByName('compile'))
        setupCompressedArchives(this.project, serviceExtension.scmHandler)
        disableJarTask()
    }

    protected void removeDefaultShadowTask(Project project) {
        ShadowJar shadow = project.tasks.findByName('shadowJar')
        project.tasks.remove(shadow)
    }
}
