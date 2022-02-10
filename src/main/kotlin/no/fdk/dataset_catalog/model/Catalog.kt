package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

data class CatalogDTO(
    val _embedded: Map<String, List<Catalog>>?
)

@Document( collection = "catalogs")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Catalog(
    @Id
    val id: String? = null,
    val uri: String? = null,

//    dct:title
    val title: Map<String, String>? = null,

//    dct:description
    val description: Map<String, String>? = null,

//    dct:publisher
    val publisher: Publisher? = null,

//    dct:issued
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val issued: LocalDate? = null,

//    dct:modified
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val modified: LocalDate? = null,

//    dct:language
    val language: String? = null,

    val hasPublishedDataSource: Boolean? = false,

    )