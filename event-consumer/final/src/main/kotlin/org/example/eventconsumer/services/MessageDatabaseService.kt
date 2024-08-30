package org.example.eventconsumer.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.eventconsumer.model.UserCreationMessageEntity
import org.springframework.stereotype.Service

@Service
class MessageDatabaseService {
	/**
	 * This is just a demo implementation, no actual database insertion is performed.
	 */
	fun handleMessage(contentsAsString: String, callback: (message: UserCreationMessageEntity) -> Unit) {
		val message = ObjectMapper().readValue(contentsAsString, UserCreationMessageEntity::class.java)
		// Callback is not invoked, because the database service is mocked.
		// callback.invoke(message)
	}
}