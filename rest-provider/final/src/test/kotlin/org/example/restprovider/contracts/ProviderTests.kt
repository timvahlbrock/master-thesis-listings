package org.example.restprovider.contracts

import StateChangeHandler
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(
	includeFilters=[
		ComponentScan.Filter(
			classes = [StateChangeHandler::class],
			type = FilterType.ASSIGNABLE_TYPE
		)
	]
)
@Import(ContractTestConfiguration::class)
@Provider("User Service")
@PactFolder("pacts")
class ProviderTests {
	@Autowired
	private lateinit var stateChangeHandlers: MutableList<StateChangeHandler>

	@Autowired
	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun before(context: PactVerificationContext) {
		MissingBeansMocker.initMocks()

		context.target = MockMvcTestTarget(mockMvc)
		stateChangeHandlers.forEach { context.withStateChangeHandlers(it) }
	}

	@TestTemplate
	@ExtendWith(PactVerificationSpringProvider::class)
	@WithMockUser
	fun pactVerificationTestTemplate(context: PactVerificationContext) {
		context.verifyInteraction()
	}
}
