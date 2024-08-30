package org.example.restprovider.controllers

import org.example.restprovider.model.UserDto
import org.example.restprovider.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserRestController(
	private val userService: UserService
) {
	@GetMapping("/v1/users/{id}")
	fun getUserById(@PathVariable(value="id") id: UUID): ResponseEntity<UserDto> {
		val user = userService.getUserByIdOrNull(id)
		return if (user != null) {
			ResponseEntity.ok(user)
		} else {
			ResponseEntity.notFound().build()
		}
	}
}