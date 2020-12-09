package no.fdk.dataset_catalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application")
data class ApplicationProperties(
    val conceptCatalogueHost: String,
    val organizationCatalogueHost: String,
    val catalogUriHost: String,
    val harvestDelay: Long,
    val catalogHarvestRoute: String,
    val newDataSourceRoute: String,
    val exchangeName: String,
)
