package org.example.restprovider.contracts.builders

import org.example.restprovider.model.UserDto
import java.util.*

fun aUserDto() = aUserDtoWith { }

fun aUserDtoWith(buildUserDto: UserDto.() -> Unit) = aUserDtoBuilderWith(buildUserDto).build()

fun aUserDtoBuilderWith(buildUserDto: UserDto.() -> Unit) =
	BuilderForRestProvider(UserDto::class)
		.with("id") { UUID.randomUUID() }
		.with(buildUserDto)
