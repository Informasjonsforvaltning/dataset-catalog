package no.fdk.dataset_catalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application")
data class ApplicationProperties(
    val conceptCatalogueHost: String,
    val organizationCatalogueHost: String,
    val catalogUriHost: String,
)