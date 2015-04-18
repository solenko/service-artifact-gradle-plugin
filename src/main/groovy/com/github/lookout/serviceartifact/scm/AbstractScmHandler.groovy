package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked

@TypeChecked
abstract class AbstractScmHandler {
    /** Supplied environment variables */
    protected Map<String, String> env

    /** Return true if the runtime environment has the necessary information to
     * make this SCM Handler available for use
     */
    abstract boolean isAvailable()

    /** Return an annotated version string with data provided by the handler */
    abstract String annotatedVersion(String baseVersion)

    /** Build an instance of this handler */
    static AbstractScmHandler build(Map<String, String> env) {
        throw new NoSuchMethodException("A subclass of AbstractScmHandler has not implemented build()!")
    }
}
