package de.bund.digitalservice.useid.identification

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID
import javax.sql.DataSource

@DataJpaTest
// @AutoConfigureTestDatabase
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration")
class IdentificationSessionRepositoryIntegrationTest {
    companion object {
        private const val REFRESH_ADDRESS: String = "some-refresh-address"
        private const val DG1 = "DG1"
        private const val DG2 = "DG2"
        private val DATA_GROUPS: List<String> = listOf(DG1, DG2)
        private val USEID_SESSION_ID: UUID = UUID.randomUUID()
        private val EID_SESSION_ID: UUID = UUID.randomUUID()
    }

    @Autowired
    private lateinit var identificationSessionRepository: IdentificationSessionRepository

    @Autowired
    private lateinit var template: JdbcTemplate

    @Autowired
    private lateinit var dataSource: DataSource

    @BeforeAll
    fun setup() {
        template = JdbcTemplate(dataSource)
    }

    @Test
    fun `find identification session by useIdSessionId and eIdSessionId returns the inserted entity`() {
        // Given
        val identificationSession = IdentificationSession(USEID_SESSION_ID, REFRESH_ADDRESS, DATA_GROUPS)
        identificationSession.eIdSessionId = EID_SESSION_ID

        // When
        identificationSessionRepository.save(identificationSession)

        // Then
        validateIdentificationSession(identificationSessionRepository.findByUseIdSessionId(USEID_SESSION_ID))
        validateIdentificationSession(identificationSessionRepository.findByEIdSessionId(EID_SESSION_ID))
    }

    private fun validateIdentificationSession(identificationSession: IdentificationSession?) {
        assertThat(identificationSession?.id, notNullValue())
        assertThat(identificationSession?.eIdSessionId, equalTo(EID_SESSION_ID))
        assertThat(identificationSession?.useIdSessionId, equalTo(USEID_SESSION_ID))
        assertThat(identificationSession?.refreshAddress, equalTo(REFRESH_ADDRESS))
        assertThat(identificationSession?.requestDataGroups, hasItems(DG1))
        assertThat(identificationSession?.requestDataGroups, hasItems(DG2))
    }
}
