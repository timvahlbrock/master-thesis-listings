package org.example.restprovider.services

import org.example.restprovider.model.UserDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService {
	fun getUserByIdOrNull(userId: UUID): UserDto? {
		return null
	}
}