package no.fdk.dataset_catalog.rdf

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

class DCATNO {
    companion object {
        private val model = ModelFactory.createDefaultModel()

        val uri = "http://difi.no/dcatno#"

        val legalBasisForProcessing: Property = model.createProperty("${uri}legalBasisForProcessing")
        val legalBasisForRestriction: Property = model.createProperty("${uri}legalBasisForRestriction")
        val legalBasisForAccess: Property = model.createProperty("${uri}legalBasisForAccess")
        val informationModel: Property = model.createProperty("${uri}informationModel")
    }
}

class ADMS {
    companion object {
        private val model = ModelFactory.createDefaultModel()

        val uri = "http://www.w3.org/ns/adms#"

        val Identifier: Resource = model.createResource("${uri}Identifier")

        val identifier = model.createProperty("${uri}identifier")
        val schemaAgency: Property = model.createProperty("${uri}schemaAgency")
        val sample = model.createProperty("${uri}sample")    }

}

class AT {
    companion object {
        private val model = ModelFactory.createDefaultModel()

        const val uri = "http://publications.europa.eu/ontology/authority/"

        val authorityCode = model.createProperty("${uri}authority-code")
    }
}

class Schema {
    companion object {
        private val model = ModelFactory.createDefaultModel()
        const val uri = "http://schema.org/"
        var startDate = model.createProperty("${uri}startDate")
        var endDate = model.createProperty("${uri}endDate")
    }
}

class DQV {
    companion object {
        private val model = ModelFactory.createDefaultModel()

        const val uri = "http://www.w3.org/ns/dqv#"
        const val ISO = "http://iso.org/25012/2008/dataquality/"


        val hasQualityAnnotation = model.createProperty("${uri}hasQualityAnnotation")
        val inDimension = model.createProperty("${uri}inDimension")

        val QualityAnnotation = model.createResource("${uri}QualityAnnotation")
        val Accuracy = model.createResource("${ISO}Accuracy")
        val Availability = model.createResource("${ISO}Availability")
        val Completeness = model.createResource("${ISO}Completeness")
        val Currentness = model.createResource("${ISO}Currentness")
        val Relevance = model.createResource("${ISO}Relevance")

        val dimensions = arrayOf(Accuracy, Availability, Completeness, Currentness, Relevance)

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
        private val model = ModelFactory.createDefaultModel()
        const val uri = "http://www.w3.org/ns/prov#"
        val Attribution = model.createResource("${uri}Attribution")

        // TODO: PROV vocabulary does not have "hasBody" attribute
        @Deprecated("")
        val hasBody = model.createProperty("${uri}hasBody")
        val agent = model.createProperty("${uri}agent")
        val qualifiedAttribution = model.createProperty("${uri}qualifiedAttribution")

    }
}