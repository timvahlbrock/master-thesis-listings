package org.example.eventprovider.model

import java.util.UUID

abstract class UserCreationMessageDto(
	val id: UUID,
	val initiator: String,
	val type: String
) {
}