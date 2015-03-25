package com.github.lookout

import org.gradle.api.Plugin
import org.gradle.api.Project

class ServiceArtifactPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'application'
    }
}
