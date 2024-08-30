package org.example.eventprovider.contracts

import au.com.dius.pact.provider.MessageAndMetadata
import au.com.dius.pact.provider.PactVerifyProvider
import au.com.dius.pact.provider.junit5.MessageTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.example.eventprovider.contracts.builders.aCredentialsUserMessageDto
import org.example.eventprovider.services.UserCreationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@WebMvcTest(
	includeFilters = [
		ComponentScan.Filter(
			classes = [Service::class],
			type = FilterType.ANNOTATION,
		)
	],
)
@Provider("User Creation Publisher")
@PactFolder("pacts")
@Import(ContractTestConfiguration::class)
class ProviderTests {
	@Autowired
	private lateinit var service: UserCreationService

	@MockBean(name = "kafkaTemplateUserCreation")
	private lateinit var kafkaTemplate: KafkaTemplate<String, String>

	@TestTemplate
	@ExtendWith(PactVerificationSpringProvider::class)
	fun pactVerificationTestTemplate(context: PactVerificationContext) {
		context.verifyInteraction()
	}

	@BeforeEach
	fun before(context: PactVerificationContext) {
		context.target = MessageTestTarget()
	}

	@PactVerifyProvider("A user creation message")
	fun verifyAUserCreationMessage(): MessageAndMetadata {
		var message: Message<Any>? = null
		doAnswer {
			message = it.arguments[0] as Message<Any>
			CompletableFuture<SendResult<String, String>>().also { future ->
				future.complete(mock())
			}
		}.whenever(this.kafkaTemplate)
			.send(any<Message<*>>())

		this.service.publishCreationOf(aCredentialsUserMessageDto())

		return MessageAndMetadata(
		message!!.payload.toString().encodeToByteArray(),
		message!!.headers,
		)
	}
}
