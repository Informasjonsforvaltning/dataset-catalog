package no.fdk.dataset_catalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.XSD

class ADMS {
    companion object {
        const val uri = "http://www.w3.org/ns/adms#"

        val identifier: Property = ResourceFactory.createProperty("${uri}identifier")
        val sample: Property = ResourceFactory.createProperty("${uri}sample")    }
}

class AT {
    companion object {
        const val uri = "http://publications.europa.eu/ontology/authority/"

        val authorityCode: Property = ResourceFactory.createProperty("${uri}authority-code")
    }
}

class Schema {
    companion object {
        const val uri = "http://schema.org/"
        var startDate: Property = ResourceFactory.createProperty("${uri}startDate")
        var endDate: Property = ResourceFactory.createProperty("${uri}endDate")
    }
}

class DQV {
    companion object {
        const val uri = "http://www.w3.org/ns/dqv#"
        const val ISO = "http://iso.org/25012/2008/dataquality/"


        val hasQualityAnnotation: Property = ResourceFactory.createProperty("${uri}hasQualityAnnotation")
        val inDimension: Property = ResourceFactory.createProperty("${uri}inDimension")

        val QualityAnnotation: Resource = ResourceFactory.createResource("${uri}QualityAnnotation")
        val Accuracy: Resource = ResourceFactory.createResource("${ISO}Accuracy")
        val Availability: Resource = ResourceFactory.createResource("${ISO}Availability")
        val Completeness: Resource = ResourceFactory.createResource("${ISO}Completeness")
        val Currentness: Resource = ResourceFactory.createResource("${ISO}Currentness")
        val Relevance: Resource = ResourceFactory.createResource("${ISO}Relevance")

        private val dimensions = arrayOf(Accuracy, Availability, Completeness, Currentness, Relevance)

        /**
         * Resolve dimension. Dimensions should be prefixed with 'iso:'
         * or defined with complete resource uri. Currently the following
         * dimensions are supported: Accuracy, Availability, Completeness,
         * Currentness, Relevance.
         */
        fun resolveDimensionResource(dimension: String?): Resource? {
            val dimensionUri = dimension?.replace("iso:", ISO)
            dimensions.forEach {
                if (dimensionUri != null && it.uri == dimensionUri) {
                    return it
                }
            }
            return null
        }
    }

}

class PROV {
    companion object {
        const val uri = "http://www.w3.org/ns/prov#"
        val Attribution: Resource = ResourceFactory.createResource("${uri}Attribution")

        val agent: Property = ResourceFactory.createProperty("${uri}agent")
        val qualifiedAttribution: Property = ResourceFactory.createProperty("${uri}qualifiedAttribution")

    }
}

class ELI {
    companion object {
        const val uri = "http://data.europa.eu/eli/ontology#"

        val LegalResource: Resource = ResourceFactory.createResource("${uri}LegalResource")
    }
}

class CPSV {
    companion object {
        const val uri = "http://purl.org/vocab/cpsv#"

        val Rule: Resource = ResourceFactory.createResource("${uri}Rule")

        val follows: Property = ResourceFactory.createProperty("${uri}follows")
        val implements: Property = ResourceFactory.createProperty("${uri}implements")
    }
}

class CPSVNO {
    companion object {
        const val uri = "https://data.norge.no/vocabulary/cpsvno#"

        val ruleForDataProcessing: Resource = ResourceFactory.createResource("${uri}ruleForDataProcessing")
        val ruleForDisclosure: Resource = ResourceFactory.createResource("${uri}ruleForDisclosure")
        val ruleForNonDisclosure: Resource = ResourceFactory.createResource("${uri}ruleForNonDisclosure")
    }
}
