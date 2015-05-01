package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked
import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException

/**
 * Git handler for a project in a traditional Git repository
 */
@TypeChecked
class GitHandler extends AbstractScmHandler {
    private final String gitDir = '.git'
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
        return this.git?.head().abbreviatedId
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
    protected Grgit getGit() {
        if (this._git == null) {
            try {
                File repoDir = findGitRoot('.')

                if (!repoDir) {
                    return null
                }

                this._git = Grgit.open(repoDir)
            }
            catch (RepositoryNotFoundException ex) {
                this.logger.debug("Repository not found", ex)
            }
        }

        return this._git
    }

    /**
     * Locate a directory, starting with our current working directory,
     * which has a .git/ subdirectory
     *
     * @param currentDirectory A string representing a relative or absolute path
     * @return File instance if we've found a git root, or null
     */
    protected File findGitRoot(String currentDirectory) {
        /* this means we've been invoked recursively with a null parent */
        if (currentDirectory == null) {
            return null
        }

        File current = new File(currentDirectory)
        File gitDir = getGitDirFor(current.absolutePath)

        if (gitDir.isDirectory()) {
            return current
        }
        return findGitRoot((new File(current.absolutePath)).parent)
    }

    protected File getGitDirFor(String absolutePath) {
        return new File(absolutePath, this.gitDir)
    }
}
