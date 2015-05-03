package com.github.lookout.serviceartifact

import groovy.transform.InheritConstructors

/**
 * Thrown when an invalid service extension is registered
 */
@InheritConstructors
class InvalidServiceExtensionError extends Exception {
}
