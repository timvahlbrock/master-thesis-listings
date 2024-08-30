package org.example.restprovider.model

import java.util.UUID

class UserDto(
	val id: UUID,
	val firstName: String,
	val lastName: String,
	val groups: List<GroupReferenceDto>
)