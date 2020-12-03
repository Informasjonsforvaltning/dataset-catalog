package no.fdk.dataset_catalog.security

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpServletRequest

@Configuration
open class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()

        http.cors()
            .and()
                .authorizeRequests()
                .requestMatchers(TurtleMatcher())
                    .permitAll()
                .antMatchers(HttpMethod.OPTIONS)
                    .permitAll()
                .antMatchers(HttpMethod.GET,"/v2/ping")
                    .permitAll()
                .antMatchers(HttpMethod.GET,"/v2/ready")
                    .permitAll()
                .anyRequest()
                    .authenticated()
            .and()
                .oauth2ResourceServer()
                    .jwt()
    }

    @Bean
    open fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder? {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(AudienceValidator()))
        return jwtDecoder
    }
}

private class TurtleMatcher: RequestMatcher{
    override fun matches(request: HttpServletRequest?): Boolean =
        request?.getHeader("Accept") != null &&
            request.getHeader("Accept") == "text/turtle" &&
            request.method == "GET"
}