package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// documentation: https://github.com/Yubico/java-webauthn-server#2-instantiate-a-relyingparty
@Configuration
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class RelyingPartyConfig(val userCredentialService: UserCredentialService) {

    @Bean
    fun relyingParty(): RelyingParty {
        val rpIdentity = RelyingPartyIdentity.builder()
            .id(applicationProperties.baseUrl) // Set this to a parent domain that covers all subdomains where users' credentials should be valid
            .name("BundesIdent")
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(userCredentialService)
            .build()
    }
}
