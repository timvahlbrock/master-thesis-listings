package org.example.restprovider.contracts.stateChangeHandlers

import StateChangeHandler
import au.com.dius.pact.provider.junitsupport.State
import org.example.restprovider.contracts.builders.aUserDto
import org.example.restprovider.services.UserService
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class UsersStateChangeHandler: StateChangeHandler {
	@Autowired
	private lateinit var userService: UserService

	@State("there is a user")
	fun toUserState(params: Map<String, Any>) {
		val sampleUser = aUserDto()

		whenever(
			userService.getUserByIdOrNull(
				UUID.fromString(params["id"] as String)
			)
		).thenReturn(sampleUser)
	}
}