package com.github.lookout.serviceartifact.scm

import groovy.transform.TypeChecked

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TypeChecked
abstract class AbstractScmHandler {
    /** Supplied environment variables */
    protected Map<String, String> env

    protected Logger logger = LoggerFactory.getLogger(this.class)

    /** Return true if the runtime environment has the necessary information to
     * make this SCM Handler available for use
     */
    abstract boolean isAvailable()

    /** Return the current revision of the tree */
    abstract String getRevision()

    /** Return an annotated version string with data provided by the handler */
    abstract String annotatedVersion(String baseVersion)

    /** Build an instance of this handler */
    static AbstractScmHandler build(Map<String, String> env) {
        throw new NoSuchMethodException("A subclass of AbstractScmHandler has not implemented build()!")
    }

    Map<String, String> getEnvironment() {
        return System.getenv()
    }
}
