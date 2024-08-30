package org.example.eventprovider.contracts.builders

import org.example.eventprovider.model.CredentialsUserMessageDto
import kotlin.reflect.KClass

class BuilderForEventProvider<C : Any>(
	targetClass: KClass<C>
) : Builder<C>(targetClass.java) {
	init {
		this
			.withTypeFactory(CredentialsUserMessageDto::class, { aCredentialsUserMessageDto() })
	}

	override fun getNewBuilder(jvmType: Class<*>): Builder<*> = BuilderForEventProvider(jvmType.kotlin)
}
