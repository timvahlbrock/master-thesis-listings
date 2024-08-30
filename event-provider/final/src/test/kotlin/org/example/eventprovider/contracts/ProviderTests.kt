package org.example.eventprovider.contracts

import au.com.dius.pact.provider.MessageAndMetadata
import au.com.dius.pact.provider.PactVerifyProvider
import au.com.dius.pact.provider.junit5.MessageTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.example.eventprovider.contracts.builders.aCredentialsUserMessageDto
import org.example.eventprovider.contracts.builders.anLdapUserMessageDto
import org.example.eventprovider.model.UserCreationMessageDto
import org.example.eventprovider.services.UserCreationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.firstValue
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

	@PactVerifyProvider("A CredentialsUserMessage")
	fun verifyACredentialsUserMessage(): MessageAndMetadata =
		captureTransmissionOf(aCredentialsUserMessageDto())

	@PactVerifyProvider("An LdapUserMessage")
	fun verifyAnLdapUserMessage(): MessageAndMetadata =
		captureTransmissionOf(anLdapUserMessageDto())

	private fun captureTransmissionOf(message: UserCreationMessageDto): MessageAndMetadata {
		val messageCapture = ArgumentCaptor.forClass(Message::class.java)
		doAnswer {
			CompletableFuture<SendResult<String, String>>().also { it.complete(mock()) }
		}.whenever(this.kafkaTemplate)
			.send(messageCapture.capture())

		this.service.publishCreationOf(message)

		return MessageAndMetadata(
			messageCapture.firstValue.payload.toString().encodeToByteArray(),
			messageCapture.firstValue.headers,
		)
	}
}
