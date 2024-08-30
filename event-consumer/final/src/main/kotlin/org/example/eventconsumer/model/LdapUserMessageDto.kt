package org.example.eventconsumer.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

class LdapUserMessageDto(
	@JsonProperty("id") messageId: UUID,
	@JsonProperty("initiator") initiator: String,
	@JsonProperty("userId") val userId: String
): UserCreationMessageDto(
	messageId,
	initiator,
	"ldapUserMessage"
) {
}