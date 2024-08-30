package org.example.eventconsumer.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import java.util.UUID


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	Type(
		value = CredentialsUserMessageDto::class,
		name = "credentialsUserMessage"
	),
	Type(
		value = LdapUserMessageDto::class,
		name = "ldapUserMessage"
	)
)
abstract class UserCreationMessageDto(
	val id: UUID,
	val initiator: String,
	val type: String
) {
}