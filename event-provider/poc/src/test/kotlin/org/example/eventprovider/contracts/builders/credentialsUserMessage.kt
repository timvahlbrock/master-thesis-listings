package org.example.eventprovider.contracts.builders

import org.example.eventprovider.model.CredentialsUserMessageDto


fun aCredentialsUserMessageDto() = aCredentialsUserMessageDtoWith { }

fun aCredentialsUserMessageDtoWith(
	buildCredentialsUserMessageDto: CredentialsUserMessageDto.() -> Unit,
) = aCredentialsUserMessageDtoBuilderWith(buildCredentialsUserMessageDto).build()

fun aCredentialsUserMessageDtoBuilderWith(
	buildCredentialsUserMessageDto: CredentialsUserMessageDto.() -> Unit,
) = BuilderForEventProvider(CredentialsUserMessageDto::class)
	.with("freeTextField") { listOf("a free text") }
	.with("mandant") { "bestellsystem" }
	.with(buildCredentialsUserMessageDto)
