package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.persistence.FlywayConfig
import de.bund.digitalservice.useid.persistence.FlywayProperties
import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import java.util.UUID

@DataR2dbcTest
@Import(FlywayConfig::class, FlywayProperties::class)
class IdentificationSessionRepositoryIntegrationTest : PostgresTestcontainerIntegrationTest() {
    companion object {
        private const val REFRESH_ADDRESS: String = "some-refresh-address"
        private const val DG1 = "DG1"
        private const val DG2 = "DG2"
        private val DATA_GROUPS: List<String> = listOf(DG1, DG2)
        private val USEID_SESSION_ID: UUID = UUID.randomUUID()
        private val EID_SESSION_ID: UUID = UUID.randomUUID()
    }

    @Autowired
    private lateinit var client: DatabaseClient

    @Autowired
    private lateinit var identificationSessionRepository: IdentificationSessionRepository

    private lateinit var template: R2dbcEntityTemplate

    @BeforeAll
    fun setup() {
        template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    }

    @Test
    fun `find identification by useidSessionId and eidSessionId returns the inserted entity`() {
        // Given
        val identificationSession = IdentificationSession(USEID_SESSION_ID, REFRESH_ADDRESS, DATA_GROUPS)
        identificationSession.eidSessionId = EID_SESSION_ID

        // When
        template.insert(identificationSession).then().`as`(StepVerifier::create).verifyComplete()

        // Then
        identificationSessionRepository.findByUseidSessionId(USEID_SESSION_ID).`as`(StepVerifier::create)
            .assertNext { validateIdentificationSession(it) }
            .verifyComplete()
        identificationSessionRepository.findByEidSessionId(EID_SESSION_ID).`as`(StepVerifier::create)
            .assertNext { validateIdentificationSession(it) }
            .verifyComplete()
    }

    private fun validateIdentificationSession(identificationSession: IdentificationSession) {
        assertThat(identificationSession.id, notNullValue())
        assertThat(identificationSession.eidSessionId, equalTo(EID_SESSION_ID))
        assertThat(identificationSession.useidSessionId, equalTo(USEID_SESSION_ID))
        assertThat(identificationSession.refreshAddress, equalTo(REFRESH_ADDRESS))
        assertThat(identificationSession.requestDataGroups, hasItems(DG1))
        assertThat(identificationSession.requestDataGroups, hasItems(DG2))
    }
}
