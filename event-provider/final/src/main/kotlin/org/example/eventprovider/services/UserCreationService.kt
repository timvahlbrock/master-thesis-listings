package org.example.eventprovider.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.eventprovider.model.UserCreationMessageDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class UserCreationService(
	@Qualifier("kafkaTemplateUserCreation")
	private val template: KafkaTemplate<String, String>
) {
	fun publishCreationOf(message: UserCreationMessageDto) {
		val messageString = ObjectMapper().writeValueAsString(message)
		this.template.send(MessageBuilder.withPayload(messageString).build())
	}
}