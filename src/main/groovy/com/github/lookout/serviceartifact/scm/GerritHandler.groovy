package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked

@TypeChecked
class GerritHandler extends AbstractScmHandler {
    private final String GERRIT_CHANGE = 'GERRIT_CHANGE_NUMBER'
    private final String GERRIT_PATCH  = 'GERRIT_PATCHSET_NUMBER'
    private final String GERRIT_REVISION  = 'GERRIT_PATCHSET_REVISION'

    GerritHandler(Map<String, String> environment) {
        this.env = environment
    }

    /**
     * Will indicate that this handler is available if we're executing in an
     * environment where a number of GERRIT_* environment variables are
     * present. Such as those provided by the Gerrit Trigger plugin for Jenkins
     */
    boolean isAvailable() {
        if (this.env == null) {
            return false
        }
        return this.env.containsKey(GERRIT_CHANGE)
    }

    /**
     * Return the value of GERRIT_PATCHSET_REVISION if present
     */
    String getRevision() {
        if (this.env.containsKey(GERRIT_REVISION)) {
            return this.env[GERRIT_REVISION]
        }
        return ""
    }

    /**
     * Return a {@code String} based on the environment variables provided with
     * Gerrit changeset information
     */
    String annotatedVersion(String baseVersion) {
        return String.format("%s.%s.%s+%s",
                             baseVersion,
                             this.env[GERRIT_CHANGE],
                             this.env[GERRIT_PATCH],
                             this.revision)
    }


    @Override
    static AbstractScmHandler build(Map<String, String> env) {
        return new GerritHandler(env)
    }
}
