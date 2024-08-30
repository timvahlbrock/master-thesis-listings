package org.example.eventconsumer.contracts.matchers

import org.example.eventconsumer.model.LdapUserMessageDto

fun anLdapUserMessageDto() = Matcher.`for`(LdapUserMessageDto::class.java)