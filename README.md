# Service Artifact Gradle Plugin

[![Build
Status](https://travis-ci.org/lookout/service-artifact-gradle-plugin.svg?branch=master)](https://travis-ci.org/lookout/service-artifact-gradle-plugin)
[ ![Download](https://api.bintray.com/packages/lookout/systems/service-artifact-plugin/images/download.svg) ](https://bintray.com/lookout/systems/service-artifact-plugin/\_latestVersion)

Gradle plugin for building a prim and proper service artifact. This
plugin is meant to codify some standards and conventions around building
service artifacts that can be easily built, tested and deployed

## Dependencies

This plugin will include the appropriate versions of the following plugins:

 * jruby-gradle base
 * jruby-gradle jar
 * asciidoctor


## Usage


### Example

```gradle
plugins {
    id "com.github.lookout.service-artifact" version "0.4.0"
}

/* serviceVersion is a helper method which will add SNAPSHOT when appropriate,
 * but also introduce the appropriate Gerrit or Travis meta-data into the version
 * number (e.g. 1.0.{GERRIT_CHANGE}.{GERRIT_PATCH})
 */
version = service.version('1.0')
description = 'A sample Service to be built with Gradle'
group = 'com.github.lookout'

/* The following is an example of what a Gradle file might look like
 * for a JRuby-based service artifact.
 *
 * A service artifact is a self-contained artifact containing everything
 * execute a service. An artifact will take the form of a .tar.gz file,
 * and inside would be:
 *
 *  my-fancy-service.tar.gz
 *      - bin/ # managmeent scripts, as determined by @mbbx6spp
 *          - start
 *          - stop
 *      - my-fancy-service.jar
 */

service {
    name "amazingserv"

    component("app", type: JRuby) {
        /* Include these directories into the service jar.
         *
         * By default everything in src/main/ruby will be included
         */
        include 'backend', 'lib'
        mainScript 'backend/main'
    }
}

dependencies {
    gems "rubygems:faraday:1.0"
    gems "rubygems:sinatra:1.4.6"
}
```

### Tasks

(*NOTE:* many of these tasks are not yet implemented)

#### Build

* **assemble** prepare all the service artifacts

#### Test

* check

#### Publish

* publish

#### Documentation

* docs
* (asciidoctor) asciidoctor
