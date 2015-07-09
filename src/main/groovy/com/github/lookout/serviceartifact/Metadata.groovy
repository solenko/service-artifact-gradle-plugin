package com.github.lookout.serviceartifact

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.lookout.serviceartifact.metadata.Data

/**
 * Representation class for the metadata structure
 */
class Metadata {
    protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
    protected Logger logger = LoggerFactory.getLogger(this.class)

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

        String toString() {
            return String.format("<Component@%d> \"%s\" \"%s\"",
                                    hashCode(), this.name, this.version)
        }
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
        logger.info("Writing the following structures to yaml")
        logger.info("Service: ${this.service}")
        logger.info("Component: ${this.component}")
        logger.info("Data: ${this.data}")
        return mapper.writeValueAsString(this)
    }
}
