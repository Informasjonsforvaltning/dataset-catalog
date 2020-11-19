package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Organization(
    val organizationId: String? = null,
    val name: String? = null,
    val allowDelegatedRegistration: Boolean? = null,
    )