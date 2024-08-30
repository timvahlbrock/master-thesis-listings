package org.example.eventprovider.model

import java.util.*

class LdapUserMessageDto(
	messageId: UUID,
	initiator: String,
	val userId: String,
) : UserCreationMessageDto(
	messageId,
	initiator,
	"ldapUserMessage",
) {
}