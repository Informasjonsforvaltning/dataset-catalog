package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.RestTemplate

@Tag("unit")
class PublishingServiceTest {

    private val applicationProperties: ApplicationProperties = mock()
    private val restTemplate: RestTemplate = mock()
    private val publishingService = PublishingService(applicationProperties, restTemplate)

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sendNewDataSourceMessage forwards bearer token`() {
        whenever(applicationProperties.harvestAdminUri).thenReturn("https://harvest-admin")

        val jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "user")
            .build()
        val auth = UsernamePasswordAuthenticationToken(jwt, "n/a", emptyList())
        SecurityContextHolder.getContext().authentication = auth

        whenever(
            restTemplate.postForEntity(
                any<java.net.URI>(),
                any<HttpEntity<Any>>(),
                eq(Any::class.java)
            )
        ).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build())

        publishingService.createNewDataSource("123456789")

        argumentCaptor<HttpEntity<Any>>().apply {
            verify(restTemplate, times(1)).postForEntity(
                any<java.net.URI>(),
                capture(),
                eq(Any::class.java)
            )
            val headers = firstValue.headers
            assert(headers.getFirst(HttpHeaders.AUTHORIZATION) == "Bearer test-token")
        }
    }

    @Test
    fun `triggerHarvest forwards bearer token`() {
        whenever(applicationProperties.harvestAdminUri).thenReturn("https://harvest-admin")

        val jwt = Jwt.withTokenValue("test-token-2")
            .header("alg", "none")
            .claim("sub", "user")
            .build()
        val auth = UsernamePasswordAuthenticationToken(jwt, "n/a", emptyList())
        SecurityContextHolder.getContext().authentication = auth

        whenever(
            restTemplate.postForEntity(
                any<java.net.URI>(),
                any<HttpEntity<Any>>(),
                eq(Any::class.java)
            )
        ).thenReturn(ResponseEntity.status(HttpStatus.OK).build())

        publishingService.triggerHarvest("123456789")

        argumentCaptor<HttpEntity<Any>>().apply {
            verify(restTemplate, times(1)).postForEntity(
                any<java.net.URI>(),
                capture(),
                eq(Any::class.java)
            )
            val entity = firstValue
            val headers = entity.headers
            assert(headers.getFirst(HttpHeaders.AUTHORIZATION) == "Bearer test-token-2")
            val body = entity.body as StartHarvestByUrlRequest
            assert(body.url == "${applicationProperties.datasetCatalogUriHost}/123456789")
            assert(body.dataType == "dataset")
        }
    }
}