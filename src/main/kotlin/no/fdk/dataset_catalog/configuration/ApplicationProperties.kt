package no.fdk.dataset_catalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(
    val fdkBaseHost: String,
    val organizationCatalogHost: String,
    val catalogUriHost: String,
    val datasetCatalogUriHost: String,
    val harvestDelay: Long,
    val catalogHarvestRoute: String,
    val newDataSourceRoute: String,
    val exchangeName: String,
)
