package no.fdk.dataset_catalog.utils.jwk

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*


class JwtToken (private val access: Access) {
    private val exp = Date().time + 120 * 1000
    private val aud = listOf("fdk-registration-api",
        "concept-catalogue",
        "records-of-processing-activities",
        "account")

    private fun buildToken() : String{
        val claimset = JWTClaimsSet.Builder()
                .audience(aud)
                .expirationTime(Date(exp))
                .claim("iss", "https://sso.staging.fellesdatakatalog.digdir.no/auth/realms/fdk")
                .claim("user_name","1924782563")
                .claim("name", "TEST USER")
                .claim("given_name", "TEST")
                .claim("family_name", "USER")
                .claim("authorities", access.authorities)
                .build()

        val signed = SignedJWT(JwkStore.jwtHeader(), claimset)
        signed.sign(JwkStore.signer())

        return signed.serialize()
    }

    override fun toString(): String {
        return buildToken()
    }

}

enum class Access(val authorities: String) {
    ORG_READ("organization:123456789:read,organization:246813579:read, organization:111111111:read"),
    ORG_WRITE("organization:123456789:write,organization:246813579:write, organization:111111111:write,organization:554433221:admin,organization:111222333:write"),
    SERIES_WRITE("organization:123123123:admin"),
    ROOT("system:root:admin")
}
