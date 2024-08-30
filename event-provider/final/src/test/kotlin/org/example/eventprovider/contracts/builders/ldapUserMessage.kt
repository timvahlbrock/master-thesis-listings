package org.example.eventprovider.contracts.builders

import org.example.eventprovider.model.LdapUserMessageDto


fun anLdapUserMessageDto() = anLdapUserMessageDtoWith { }

fun anLdapUserMessageDtoWith(
	buildLdapUserMessageDto: LdapUserMessageDto.() -> Unit,
) = anLdapUserMessageDtoBuilderWith(buildLdapUserMessageDto).build()

fun anLdapUserMessageDtoBuilderWith(
	buildLdapUserMessageDto: LdapUserMessageDto.() -> Unit,
) = BuilderForEventProvider(LdapUserMessageDto::class)
	.with("freeTextField") { listOf("a free text") }
	.with("mandant") { "bestellsystem" }
	.with(buildLdapUserMessageDto)
