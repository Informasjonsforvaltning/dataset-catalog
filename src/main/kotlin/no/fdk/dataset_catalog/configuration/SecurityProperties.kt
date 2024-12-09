package no.fdk.dataset_catalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("security")
data class SecurityProperties(
    val fdkIssuer: String,
    val corsOriginPatterns: List<String>
)
