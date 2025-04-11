package no.fdk.dataset_catalog.security

import jakarta.servlet.http.HttpServletRequest
import no.fdk.dataset_catalog.configuration.SecurityProperties
import org.apache.jena.riot.Lang
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsConfiguration

@Configuration
open class SecurityConfig(
    private val securityProperties: SecurityProperties
) {

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource { _ ->
                    val config = CorsConfiguration()
                    config.allowCredentials = false
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L
                    config.allowedOriginPatterns = securityProperties.corsOriginPatterns
                    config.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH")

                    config
                }
            }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(RDFMatcher()).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/ping", "/ready", "/swagger-ui/**", "/v3/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer -> resourceServer.jwt { } }
        return http.build()
    }

    @Bean
    open fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder? {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                listOf(
                    JwtTimestampValidator(),
                    JwtIssuerValidator(securityProperties.fdkIssuer),
                    JwtClaimValidator("aud") { aud: List<String> -> aud.contains("fdk-registration-api") }
                )
            ))
        return jwtDecoder
    }
}

private class RDFMatcher : RequestMatcher {
    override fun matches(request: HttpServletRequest?): Boolean =
        request?.method == "GET" && acceptHeaderIsRDF(request.getHeader("Accept"))
}

private fun acceptHeaderIsRDF(accept: String?): Boolean =
    when {
        accept == null -> false
        accept.contains(Lang.TURTLE.headerString) -> true
        accept.contains("text/n3") -> true
        accept.contains(Lang.RDFJSON.headerString) -> true
        accept.contains(Lang.JSONLD.headerString) -> true
        accept.contains(Lang.RDFXML.headerString) -> true
        accept.contains(Lang.NTRIPLES.headerString) -> true
        accept.contains(Lang.NQUADS.headerString) -> true
        accept.contains(Lang.TRIG.headerString) -> true
        accept.contains(Lang.TRIX.headerString) -> true
        else -> false
    }
