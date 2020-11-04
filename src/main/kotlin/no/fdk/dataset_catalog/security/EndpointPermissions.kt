package no.fdk.dataset_catalog.security

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

private const val ROLE_ROOT_ADMIN = "system:root:admin"
private fun roleOrgAdmin(orgnr: String) = "organization:$orgnr:admin"
private fun roleOrgWrite(orgnr: String) = "organization:$orgnr:write"
private fun roleOrgRead(orgnr: String) = "organization:$orgnr:read"

@Component
class EndpointPermissions {

    fun getOrgsByReadPermission(jwt: Jwt): Set<String> {
        val authorities: String? = jwt.claims["authorities"] as? String
        val regex = Regex("""[0-9]{9}""")

        return authorities
            ?.let { regex.findAll(it)}
            ?.map { matchResult -> matchResult.value }
            ?.toSet()
            ?: emptySet()
    }


    fun hasOrgReadPermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            authorities.contains(roleOrgRead(orgnr)) -> true
            else -> false
        }
    }

    fun hasOrgWritePermission(jwt: Jwt, orgnr: String): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return when {
            authorities == null -> false
            authorities.contains(roleOrgAdmin(orgnr)) -> true
            authorities.contains(roleOrgWrite(orgnr)) -> true
            else -> false
        }
    }

    fun hasSysAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROLE_ROOT_ADMIN) ?: false
    }

}