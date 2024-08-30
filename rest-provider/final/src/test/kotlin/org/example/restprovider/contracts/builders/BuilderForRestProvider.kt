package org.example.restprovider.contracts.builders

import org.example.restprovider.model.GroupReferenceDto
import org.example.restprovider.model.UserDto
import kotlin.reflect.KClass

class BuilderForRestProvider<C : Any>(
	targetClass: KClass<C>
) : Builder<C>(targetClass.java) {
	init {
		this
			.withTypeFactory(UserDto::class) { aUserDto() }
			.withTypeFactory(GroupReferenceDto::class) { aGroupReferenceDto() }
	}

	override fun getNewBuilder(jvmType: Class<*>): Builder<*> = BuilderForRestProvider(jvmType.kotlin)
}
