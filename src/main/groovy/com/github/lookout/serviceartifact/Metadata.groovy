package com.github.lookout.serviceartifact

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

import com.github.lookout.serviceartifact.metadata.Data

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

    @JsonProperty
    Service service = new Service()

    @JsonProperty
    Component component = new Component()

    @JsonProperty
    Data data

    Metadata(String serviceName, String componentName, String componentVersion) {
        this.service.name = serviceName
        this.component.name = componentName
        this.component.version = componentVersion
    }

    /**
     * @return This instance as a YAML String
     */
    String toYaml() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.writeValueAsString(this)
    }
}
