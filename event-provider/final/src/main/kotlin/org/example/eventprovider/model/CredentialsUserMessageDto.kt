package org.example.eventprovider.model

import java.util.*

class CredentialsUserMessageDto(
	messageId: UUID,
	initiator: String,
	val username: String,
	val password: String
) : UserCreationMessageDto(
	messageId,
	initiator,
	"credentialsUserMessage",
) {
}