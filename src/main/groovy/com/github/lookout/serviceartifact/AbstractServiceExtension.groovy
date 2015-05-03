package com.github.lookout.serviceartifact

/**
 * Base abstract class for defining extensions inside of the service {}
 * DSL
 *
 */
abstract class AbstractServiceExtension {
    abstract void configure(Closure configClosure)
    abstract void configureWithDefaults()
}
