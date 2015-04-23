package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked
import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException

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
        return this.git?.head().getId()
    }

    String annotatedVersion(String baseVersion) {
        if (this.git == null) {
            return baseVersion
        }
        return String.format("%s+%s", baseVersion, this.revision)
    }

    @Override
    static AbstractScmHandler build(Map<String, String> env) {
        return new GitHandler(env)
    }


    /** Return an {@code Grgit} object for internal use */
    private Grgit getGit() {
        if (this._git == null) {
            try {
                this._git = Grgit.open('.')
            }
            catch (RepositoryNotFoundException ex) {
                this.logger.debug("Repository not found", ex)
            }
        }

        return this._git
    }
}
