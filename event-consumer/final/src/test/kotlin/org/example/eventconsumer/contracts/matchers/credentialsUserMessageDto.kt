package org.example.eventconsumer.contracts.matchers

import org.example.eventconsumer.model.CredentialsUserMessageDto

fun aCredentialsUserMessageDto() = Matcher.`for`(CredentialsUserMessageDto::class.java)