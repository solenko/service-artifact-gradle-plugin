package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked
import org.ajoberstar.grgit.Grgit
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.Project

/**
 * Git handler for a project in a traditional Git repository
 */
@TypeChecked
class GitHandler extends AbstractScmHandler {
    private final String gitDir = '.git'
    private Grgit _git = null

    GitHandler(Project project, Map<String, String> environment) {
        this.env = environment
        this.project = project
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

    /**
     * Return the build number present in the environment as defined by the CI
     * system. This supports BUILD_NUMBER (Jenkins) and TRAVIS_BUILD_NUMBER
     * (Travis CI)
     *
     */
    String getBuildNumberFromCI() {
        return this.env.BUILD_NUMBER ?: this.env.TRAVIS_BUILD_NUMBER
    }


    /**
     * return an annotated version with the CI and Git metadata added
     */
    String annotatedVersion(String baseVersion) {
        if (this.buildNumberFromCI) {
            baseVersion = String.format("%s.%s",
                                        baseVersion,
                                        this.buildNumberFromCI)
        }
        if (this.git == null) {
            return baseVersion
        }
        return String.format("%s+%s", baseVersion, this.revision)
    }

    @Override
    static AbstractScmHandler build(Project project, Map<String, String> env) {
        return new GitHandler(project, env)
    }


    /** Return an {@code Grgit} object for internal use */
    protected Grgit getGit() {
        if (this._git == null) {
            try {
                File repoDir = findGitRoot(project?.projectDir)

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
    protected File findGitRoot(File current) {
        /* this means we've been invoked recursively with a null parent */
        if (current == null) {
            return null
        }

        File gitDir = getGitDirFor(current.absolutePath)

        if (gitDir.isDirectory()) {
            return current
        }
        return findGitRoot(current.parentFile)
    }

    protected File getGitDirFor(String absolutePath) {
        return new File(absolutePath, this.gitDir)
    }
}
