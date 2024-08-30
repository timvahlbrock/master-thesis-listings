package org.example.eventconsumer.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class CredentialsUserMessageDto(
	@JsonProperty("id") messageId: UUID,
	@JsonProperty("initiator") initiator: String,
	@JsonProperty("username") val username: String,
	@JsonProperty("password") val password: String
) : UserCreationMessageDto(
	messageId,
	initiator
) {
}