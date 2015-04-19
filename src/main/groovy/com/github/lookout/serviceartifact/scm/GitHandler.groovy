package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked
import org.ajoberstar.grgit.Grgit

/**
 * Git handler for a project in a traditional Git repository
 */
@TypeChecked
class GitHandler extends AbstractScmHandler {
    private Grgit _git = null

    GitHandler(Map<String, String> environment) {
        this.env = environment
    }

    boolean isAvailable() {
        if (this.git != null) {
            return true
        }
        return false
    }

    String getRevision() {
        return ''
    }

    String annotatedVersion(String baseVersion) {
        return baseVersion
    }

    @Override
    static AbstractScmHandler build(Map<String, String> env) {
        return new GitHandler(env)
    }


    /** Return an {@code Grgit} object for internal use */
    private Grgit getGit() {
        if (this._git == null) {
            this._git = Grgit.open('.')
        }

        return this._git
    }
}
