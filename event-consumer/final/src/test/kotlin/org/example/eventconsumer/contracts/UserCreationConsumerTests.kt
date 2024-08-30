package org.example.eventconsumer.contracts

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.newJsonObject
import au.com.dius.pact.consumer.junit5.PactConsumerTest
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.consumer.junit5.ProviderType
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.V4Interaction
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import org.example.eventconsumer.contracts.matchers.aCredentialsUserMessageDto
import org.example.eventconsumer.contracts.matchers.anLdapUserMessageDto
import org.example.eventconsumer.model.UserCreationMessageDto
import org.example.eventconsumer.model.UserCreationMessageEntity
import org.example.eventconsumer.services.MessageDatabaseService
import org.example.eventconsumer.services.UserCreationMessageDispatcher
import org.example.eventconsumer.services.UserCreationMessageMetrics
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@PactConsumerTest
@PactTestFor(
	providerName = "User Creation Publisher",
	providerType = ProviderType.ASYNCH,
	pactVersion = PactSpecVersion.V4
)
@Import(ContractTestConfiguration::class)
@WebMvcTest(
	includeFilters = [
		ComponentScan.Filter(
			classes = [Service::class],
			type = FilterType.ANNOTATION
		)
	]
)
class UserCreationConsumerTests {
	@Autowired
	private lateinit var dispatcher: UserCreationMessageDispatcher

	@MockBean
	private lateinit var metrics: UserCreationMessageMetrics

	@MockBean
	private lateinit var dbService: MessageDatabaseService

	@Test
	@PactTestFor(pactMethod = "userCreationMessage")
	fun userCreationMessageMessageTest(
		messages: List<V4Interaction.AsynchronousMessage>
	) {
		assertAll(messages.map { m -> { -> receive(m) } })
	}

	private fun receive(
		message: V4Interaction.AsynchronousMessage
	) {
		var userCreationMessage: UserCreationMessageEntity? = null
		doAnswer {
			userCreationMessage =
				UserCreationMessageEntity(
					"some id",
					 "some initiator"
				)
			null
		}
			.whenever(this.dbService)
			.handleMessage(anyString(), any())

		assertDoesNotThrow {
			this.dispatcher.handleUserCreationMessage(
				message.contentsAsString(),
				message.metadata as Map<String, String>
			)

			this.dispatcher.parseTypeSpecificFields(
				message.contentsAsString(),
				userCreationMessage!!,
				message.metadata as Map<String, String>
			)
		}
	}

	@Pact(consumer = "User Service")
	fun userCreationMessage(builder: MessagePactBuilder): V4Pact {
		builder.expectsToReceive("A CredentialsUserMessage")
			.withMetadata(mapOf("contentType" to "application/json"))
			.withContent(aCredentialsUserMessageDto())

		builder.expectsToReceive("An LdapUserMessage")
		.withMetadata(mapOf("contentType" to "application/json"))
			.withContent(anLdapUserMessageDto())

		return builder.toPact(V4Pact::class.java)
	}
}
