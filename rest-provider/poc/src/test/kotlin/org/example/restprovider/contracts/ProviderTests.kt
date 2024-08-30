package org.example.restprovider.contracts

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.example.restprovider.model.UserDto
import org.example.restprovider.model.GroupReferenceDto
import org.example.restprovider.controllers.UserRestController
import org.example.restprovider.services.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.util.*

@WebMvcTest(UserRestController::class)
@Provider("User Service")
@PactFolder("pacts")
class ProviderTests {
	@MockBean
	private lateinit var userService: UserService

	@Autowired
	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun before(context: PactVerificationContext) {
		context.target = MockMvcTestTarget(mockMvc)
	}

	@TestTemplate
	@ExtendWith(PactVerificationSpringProvider::class)
	@WithMockUser
	fun pactVerificationTestTemplate(context: PactVerificationContext) {
		context.verifyInteraction()
	}

	@State("there is a user")
	fun toUserState() {
		val sampleUser = UserDto(
			id = UUID.fromString("9a9845c4-8a46-4bf9-89a5-ecd3c3b66479"),
			firstName = "Jane",
			lastName = "Appleseed",
			groups = listOf(
				GroupReferenceDto(
					id = UUID.fromString("1f2a451b-843c-4e6a-904d-fbee06dc61d7"),
					isAdmin = false
				)
			)
		)

		whenever(
			userService.getUserByIdOrNull(
				any(),
			)
		).thenReturn(sampleUser)
	}
}
