package com.github.lookout.serviceartifact

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Representation class for the metadata structure
 */
class Metadata {

    class Service {
        @JsonProperty
        String name

        @JsonProperty
        List<String> dependencies = []
    }

    class Component {
        @JsonProperty
        String name

        @JsonProperty
        String version
    }

    class Data {
        @JsonProperty
        List<String> dependencies = []

        @JsonProperty
        List<String> migrations = []
    }


    @JsonProperty
    Service service = new Service()

    @JsonProperty
    Component component = new Component()

    @JsonProperty
    Data data = new Data()

    Metadata(String serviceName, String componentName, String componentVersion) {
        this.service.name = serviceName
        this.component.name = componentName
        this.component.version = componentVersion
    }
}
