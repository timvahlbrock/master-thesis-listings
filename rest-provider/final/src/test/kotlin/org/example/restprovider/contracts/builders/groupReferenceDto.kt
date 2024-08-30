package org.example.restprovider.contracts.builders

import org.example.restprovider.model.GroupReferenceDto

fun aGroupReferenceDto() = aGroupReferenceDtoWith { }

fun aGroupReferenceDtoWith(buildGroupReferenceDto: GroupReferenceDto.() -> Unit) = aGroupReferenceDtoBuilderWith(buildGroupReferenceDto).build()

fun aGroupReferenceDtoBuilderWith(buildGroupReferenceDto: GroupReferenceDto.() -> Unit) =
	BuilderForRestProvider(GroupReferenceDto::class)
		.with(buildGroupReferenceDto)