package de.bund.digitalservice.useid.wellknown

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.nio.file.Files

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WellKnownControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    val jacksonMapper = jacksonObjectMapper()

    @Test
    fun `iOS deeplink endpoint returns JSON file`() {
        val iosAssociationResource = ClassPathResource("iosAssociation.json").file.toPath()
        val iosAssociation = String(Files.readAllBytes(iosAssociationResource))

        val response = webTestClient
            .get()
            .uri("/.well-known/apple-app-site-association")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>().returnResult().responseBody

        assertEquals(jacksonMapper.readTree(response), jacksonMapper.readTree(iosAssociation))
    }

    @Test
    fun `Android deeplink endpoint returns JSON file`() {
        val androidAssociationResource = ClassPathResource("androidAssociation.json").file.toPath()
        val androidAssociation = String(Files.readAllBytes(androidAssociationResource))

        val response = webTestClient
            .get()
            .uri("/.well-known/assetlinks.json")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<String>().returnResult().responseBody

        assertEquals(jacksonMapper.readTree(response), jacksonMapper.readTree(androidAssociation))
    }
}
