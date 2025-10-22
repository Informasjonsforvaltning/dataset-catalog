package no.fdk.dataset_catalog.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.utils.jwk.JwkStore

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if(!mockserver.isRunning) {
        mockserver.stubFor(get(urlEqualTo("/ping"))
                .willReturn(aResponse()
                        .withStatus(200))
        )
        mockserver.stubFor(get(urlEqualTo("/api"))
            .willReturn(aResponse()
                .withBody(
                    listOf(
                        CatalogCount(
                            id = "123456789",
                            datasetCount = 1,
                        )
                    ).toString()
                ).withStatus(200))
        )
        mockserver.stubFor(get(urlEqualTo("/realms/fdk/protocol/openid-connect/certs"))
            .willReturn(okJson(JwkStore.get())))
        mockserver.start()
    }
}

fun stopMockServer() {

    if (mockserver.isRunning) mockserver.stop()

}