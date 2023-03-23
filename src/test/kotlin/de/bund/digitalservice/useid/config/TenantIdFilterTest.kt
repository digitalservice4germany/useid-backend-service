package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@Tag("test")
internal class TenantIdFilterTest {

    private val tenantProperties: TenantProperties = mockk()
    private val filter = TenantIdFilter(tenantProperties)

    private lateinit var validTenant: Tenant

    @BeforeEach
    fun beforeEach() {
        validTenant = Tenant().apply {
            id = "some-tenant-id"
            refreshAddress = "address"
            apiKey = "valid-api-key"
        }
    }

    @AfterAll
    fun afterAll() {
        unmockkAll()
    }

    @Test
    fun `should assign the tenant id unknown for all unknown calls`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals("unknown", getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
        }
    }

    @Test
    fun `should assign the tenant based on the hostname for calls to the widget`() {
        // Given
        val allowedHost = "foo"
        every { tenantProperties.findByAllowedHost(any()) } returns validTenant
        val request = MockHttpServletRequest()
        request.servletPath = "/widget"
        request.addParameter("hostname", allowedHost)
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(validTenant.id, getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByAllowedHost(allowedHost)
        }
    }

    @Test
    fun `should assign the tenant based on a valid tenant id query param`() {
        // Given
        every { tenantProperties.findByTenantId(any()) } returns validTenant
        val request = MockHttpServletRequest()
        request.addParameter("tenant_id", validTenant.id)
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(validTenant.id, getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByTenantId(any())
        }
    }

    @Test
    fun `should assign the tenant based on a valid api key`() {
        // Given
        val authentication = ApiKeyAuthenticationToken(validTenant.apiKey, validTenant.refreshAddress, emptyList(), true)
        val securityContext: SecurityContext = mockk()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { tenantProperties.findByApiKey(any()) } returns validTenant
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(validTenant.id, getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByApiKey(validTenant.apiKey)
        }

        SecurityContextHolder.clearContext()
    }

    private fun getTenantIdFromRequest(request: MockHttpServletRequest) =
        (request.getAttribute("tenant") as Tenant).id
}
