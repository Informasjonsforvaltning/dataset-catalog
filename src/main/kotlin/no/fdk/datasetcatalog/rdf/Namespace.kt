package no.fdk.datasetcatalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class ADMS {
    companion object {
        const val URI = "http://www.w3.org/ns/adms#"

        val identifier: Property = ResourceFactory.createProperty("${URI}identifier")
        val sample: Property = ResourceFactory.createProperty("${URI}sample")
    }
}

class Schema {
    companion object {
        const val URI = "http://schema.org/"
        var startDate: Property = ResourceFactory.createProperty("${URI}startDate")
        var endDate: Property = ResourceFactory.createProperty("${URI}endDate")
    }
}

class DQV {
    companion object {
        const val URI = "http://www.w3.org/ns/dqv#"
        const val ISO = "http://iso.org/25012/2008/dataquality/"

        val hasQualityAnnotation: Property = ResourceFactory.createProperty("${URI}hasQualityAnnotation")
        val inDimension: Property = ResourceFactory.createProperty("${URI}inDimension")

        val QualityAnnotation: Resource = ResourceFactory.createResource("${URI}QualityAnnotation")
        val Accuracy: Resource = ResourceFactory.createResource("${ISO}Accuracy")
        val Availability: Resource = ResourceFactory.createResource("${ISO}Availability")
        val Completeness: Resource = ResourceFactory.createResource("${ISO}Completeness")
        val Currentness: Resource = ResourceFactory.createResource("${ISO}Currentness")
        val Relevance: Resource = ResourceFactory.createResource("${ISO}Relevance")
    }
}

class PROV {
    companion object {
        const val URI = "http://www.w3.org/ns/prov#"
        val Attribution: Resource = ResourceFactory.createResource("${URI}Attribution")

        val agent: Property = ResourceFactory.createProperty("${URI}agent")
        val qualifiedAttribution: Property = ResourceFactory.createProperty("${URI}qualifiedAttribution")
    }
}

class ELI {
    companion object {
        const val URI = "http://data.europa.eu/eli/ontology#"

        val LegalResource: Resource = ResourceFactory.createResource("${URI}LegalResource")
    }
}

class CPSV {
    companion object {
        const val URI = "http://purl.org/vocab/cpsv#"

        val Rule: Resource = ResourceFactory.createResource("${URI}Rule")

        val follows: Property = ResourceFactory.createProperty("${URI}follows")
        val implements: Property = ResourceFactory.createProperty("${URI}implements")
    }
}

class CPSVNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/cpsvno#"

        val ruleForDataProcessing: Resource = ResourceFactory.createResource("${URI}ruleForDataProcessing")
        val ruleForDisclosure: Resource = ResourceFactory.createResource("${URI}ruleForDisclosure")
        val ruleForNonDisclosure: Resource = ResourceFactory.createResource("${URI}ruleForNonDisclosure")
    }
}

class CV {
    companion object {
        const val URI = "http://data.europa.eu/m8g/"

        val Cost: Resource = ResourceFactory.createResource("${URI}Cost")

        val currency: Property = ResourceFactory.createProperty("${URI}currency")
        val hasCost: Property = ResourceFactory.createProperty("${URI}hasCost")
        val hasValue: Property = ResourceFactory.createProperty("${URI}hasValue")
    }
}

class DCATAP {
    companion object {
        const val URI = "http://data.europa.eu/r5r/"

        val applicableLegislation: Property = ResourceFactory.createProperty("${URI}applicableLegislation")
    }
}

class MOBILITYDCATAP {
    companion object {
        const val URI = "https://w3id.org/mobilitydcat-ap#"

        val mobilityTheme: Property = ResourceFactory.createProperty("${URI}mobilityTheme")
        val mobilityDataStandard: Property = ResourceFactory.createProperty("${URI}mobilityDataStandard")
    }
}
