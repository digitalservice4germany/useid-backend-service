package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.integration.RedisTestContainerConfig
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Tag("integration")
@Transactional
class EidHealthServiceIntegrationTest() : RedisTestContainerConfig() {

    @Autowired
    private lateinit var eidServiceRepository: EidServiceRepository

    @Autowired
    private lateinit var eidHealthService: EidHealthService

    @BeforeAll
    fun setupBeforeAll() {
        mockkConstructor(EidService::class)
        eidServiceRepository.deleteAll()
    }

    @BeforeEach
    fun setup() {
        eidServiceRepository.deleteAll()
    }

    @AfterAll
    fun teardown() {
        eidServiceRepository.deleteAll()
    }

    @Test
    fun `should store true if eIdService responds with no error`() {
        val mockTCToken = mockk<TCTokenType>()
        every { mockTCToken.refreshAddress } returns "https://www.foobar.com?sessionId=1234"
        every { anyConstructed<EidService>().getTcToken(any()) } returns mockTCToken

        eidHealthService.checkEidServiceAvailability()
        val foundResults = eidServiceRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(true))
    }

    @Test
    fun `should store false if eIdService responds with an exception`() {
        every { anyConstructed<EidService>().getTcToken(any()) } throws Exception("some error")

        eidHealthService.checkEidServiceAvailability()
        val foundResults = eidServiceRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(false))
    }
}