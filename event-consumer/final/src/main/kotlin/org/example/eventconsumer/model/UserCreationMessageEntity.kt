package org.example.eventconsumer.model

import com.fasterxml.jackson.annotation.JsonProperty

class UserCreationMessageEntity(
	@JsonProperty("id")	val id: String,
	@JsonProperty("initiator")	val initiator: String,
) {}
