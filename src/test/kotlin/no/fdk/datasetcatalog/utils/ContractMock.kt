package no.fdk.datasetcatalog.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import no.fdk.datasetcatalog.model.CatalogCount
import no.fdk.datasetcatalog.utils.jwk.JwkStore

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if (!mockserver.isRunning) {
        mockserver.stubFor(
            get(urlEqualTo("/ping"))
                .willReturn(
                    aResponse()
                        .withStatus(200),
                ),
        )
        mockserver.stubFor(
            get(urlEqualTo("/api"))
                .willReturn(
                    aResponse()
                        .withBody(
                            listOf(
                                CatalogCount(
                                    id = "123456789",
                                    datasetCount = 1,
                                ),
                            ).toString(),
                        ).withStatus(200),
                ),
        )
        mockserver.stubFor(
            get(urlEqualTo("/realms/fdk/protocol/openid-connect/certs"))
                .willReturn(okJson(JwkStore.get())),
        )
        mockserver.start()
    }
}

fun stopMockServer() {
    if (mockserver.isRunning) mockserver.stop()
}
