package com.github.lookout.serviceartifact

import spock.lang.*

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

/**
 */
class MetadataSpec extends Specification {
    def "constructor arguments are implemented properly"() {
        given:
        String serviceName = 'sap'
        String componentName = 'www'
        String version = '1.0'
        Metadata metadata

        when:
        metadata = new Metadata(serviceName, componentName, version)

        then:
        metadata.service.name == serviceName
        metadata.component.name == componentName
        metadata.component.version == version
    }

    def "metadata should serialize to YAML"() {
        given:
        Metadata metadata = new Metadata('sap', 'api', '1.0')
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String output

        when:
        output = mapper.writeValueAsString(metadata)

        then:
        output instanceof String
        println output
    }
}
