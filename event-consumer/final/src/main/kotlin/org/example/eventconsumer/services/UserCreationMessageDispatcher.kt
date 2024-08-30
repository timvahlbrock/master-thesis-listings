package org.example.eventconsumer.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.eventconsumer.model.UserCreationMessageDto
import org.example.eventconsumer.model.UserCreationMessageEntity
import org.springframework.stereotype.Service

@Service
class UserCreationMessageDispatcher(
	private val messageDatabaseService: MessageDatabaseService
) {
	fun handleUserCreationMessage(contentsAsString: String, map: Map<String, String>) {
		this.messageDatabaseService.handleMessage(contentsAsString) { savedMessage ->
			parseTypeSpecificFields(
				contentsAsString,
				savedMessage,
				map
			)
		}
	}

	fun parseTypeSpecificFields(rawMessage: String, message: UserCreationMessageEntity, map: Map<String, String>) {
		ObjectMapper().readValue(rawMessage, UserCreationMessageDto::class.java)
	}
}