package org.example.eventconsumer.contracts

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.junit5.PactConsumerTest
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.consumer.junit5.ProviderType
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.V4Interaction
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import org.example.eventconsumer.services.UserCreationMessageDispatcher
import org.example.eventconsumer.services.UserCreationMessageMetrics
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.doAnswer
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service


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

	@Pact(consumer = "User Service")
	fun userCreationMessage(
		builder: MessagePactBuilder
	): V4Pact =
		builder
			.expectsToReceive("A user creation message")
			.withMetadata(mapOf("contentType" to "application/json"))
			.withContent(
				newJsonBody ({ theObject ->
					theObject.uuid("id")
					theObject.stringType("initiator", "the string")
				}).build()
			)
			.toPact(V4Pact::class.java)

	@Test
	@PactTestFor(pactMethod = "userCreationMessage")
	fun userCreationMessageTest(
		messages: List<V4Interaction.AsynchronousMessage>
	) {
		doAnswer {
			throw AssertionError(
				"Unroutable Error detected, error code: ${it.arguments[1]}"
			)
		}.whenever(
			this.metrics
		).unroutableErrors(
			anyOrNull(),
			anyOrNull()
		)

		assertDoesNotThrow {
			dispatcher.handleUserCreationMessage(
				messages[0].contentsAsString(),
				messages[0].metadata as Map<String, String>
			)
		}
	}
}
