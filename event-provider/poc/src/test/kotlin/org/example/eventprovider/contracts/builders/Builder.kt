package org.example.eventprovider.contracts.builders

import au.com.dius.pact.core.support.property
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType

abstract class Builder<C : Any>(
	private val targetClass: Class<C>
) {
	private val fallbacks = mutableListOf<Fallback<*>>()
	private val overrides = mutableListOf<Override<*>>()
	private val modifiers = mutableListOf<C.() -> Unit>()

	init {
		if (Modifier.isAbstract(targetClass.modifiers)) {
			throw IllegalArgumentException("Cannot create instance for ${targetClass.simpleName} because it is abstract.")
		}

		this.withTypeValue(String::class, "the string")
			.withTypeValue(Boolean::class, false)
			.withTypeValue(Int::class, 1)
			.withTypeValue(Long::class, 1)
			.withTypeValue(Double::class, 1.0)
			.withTypeValue(Float::class, 1.0f)
			.withTypeFactory(Instant::class) { Instant.now() }
			.withTypeFactory(LocalDate::class) { LocalDate.now() }
			.withTypeFactory(LocalTime::class) { LocalTime.now() }
			.withTypeFactory(UUID::class) { UUID.randomUUID() }
			.withTypeFactory(ByteArray::class) { ByteArray(0) }
	}

	fun build(): C {
		try {
			return this.buildInternal()
		} catch (e: Throwable) {
			throw Exception("Failed to build " + targetClass.simpleName + ".", e)
		}
	}

	private fun buildInternal(): C {
		val constructor = getConstructorJava()
		val parameters = getParametersFor(constructor.javaConstructor!!)

		// invoke kotlin constructor to make sure that kotlin does its magic and invokes the correct field initializers
		return constructor.call(*parameters).also { instance ->
			fillPropertiesOn(instance)
			modifiers.forEach { it(instance) }
		}
	}

	private fun getConstructorJava(): KFunction<C> {
		return targetClass.kotlin.primaryConstructor ?: targetClass.kotlin.constructors.first()
	}

	private fun getParametersFor(constructor: Constructor<C>): Array<Any?> {
		val requiredArgs = constructor.parameters
		val requiredArgClasses = constructor.genericParameterTypes
		val actualArgs = mutableListOf<Any?>()

		for (p in requiredArgs.indices) {
			val param = requiredArgs[p]
			val arg = getValueFor(param.name!!, requiredArgClasses[p]!!)

			if (
				arg != null &&
				param.type != arg::class.javaPrimitiveType &&
				!param.type.isAssignableFrom(arg.javaClass)
			) {
				throw IllegalArgumentException(
					"Calculated value '$arg' for constructor argument ${param.name} is not an instance of required type ${param.type}"
				)
			}

			actualArgs.add(arg)
		}
		val parameters = actualArgs.toTypedArray()
		return parameters
	}

	private fun fillPropertiesOn(instance: C) {
		val passedFields = mutableSetOf<String>()
		instance::class.memberProperties.forEach {
			try {
				if (it !is KMutableProperty<*>) return@forEach

				val isAlreadySet = !it.isLateinit && instance.property(it.name)?.get(instance) != null
				if (isAlreadySet) {
					val override = getOverrideFor(it.name)
					if (override != null) {
						it.setter.call(instance, override.factory())
						passedFields.add(it.name)
					}
					return@forEach
				}

				it.setter.call(instance, getValueFor(it.name, it.returnType.javaType))
				passedFields.add(it.name)
			} catch (e: IllegalCallableAccessException) {
				// try again with next possible implementation
			} catch (e: Throwable) {
				throw Exception("Failed to set property ${it.name}.", e)
			}
		}

		instance::class.java.methods.forEach {
			if (passedFields.contains(it.name)) return@forEach

			val isSetter =
				it.returnType.isAssignableFrom(instance::class.java) &&
					it.parameters.size == 1
			if (!isSetter) return@forEach
			val name =
				if (it.name.matches(Regex("set[A-Z]\\w*"))) {
					it.name.substring(3).replaceFirstChar { char -> char.lowercaseChar() }
				} else if (it.name.matches(Regex("add[A-Z]\\w*"))) {
					return@forEach
				} else if (it.name.matches(Regex("toDto"))) {
					return@forEach
				} else {
					it.name
				}

			try {
				it.invoke(instance, getValueFor(name, it.genericParameterTypes[0]))
				passedFields.add(name)
			} catch (e: IllegalCallableAccessException) {
				// try again with the next possible implementation
			} catch (e: Throwable) {
				throw IllegalStateException("Failed to set property $name (through method ${it.name}).", e)
			}
		}
	}

	fun <P : Any> with(
		name: String,
		value: P? = null
	): Builder<C> {
		this.overrides +=
			object : Override<P> {
				override val name = name
				override val factory = { value }
			}
		return this
	}

	fun <P : Any> with(
		name: String,
		factory: Factory<P>
	): Builder<C> {
		this.overrides +=
			object : Override<P> {
				override val name = name
				override val factory = factory
			}
		return this
	}

	fun with(modifier: C.() -> Unit): Builder<C> {
		this.modifiers.add(modifier)
		return this
	}

	fun <P : Any> withTypeValue(
		type: KClass<P>,
		value: P
	): Builder<C> {
		this.fallbacks +=
			object : Fallback<P> {
				override val type = type
				override val factory = { value } as Factory<P>
			}
		return this
	}

	fun <P : Any> withTypeFactory(
		type: KClass<P>,
		factory: Factory<P>
	): Builder<C> {
		this.fallbacks +=
			object : Fallback<P> {
				override val type = type
				override val factory = factory
			}
		return this
	}

	private fun getValueFor(
		name: String,
		type: Type
	): Any? {
		val preset = getOverrideFor(name) ?: getFallbackFor(name, type)
		return preset.factory()
	}

	private fun getOverrideFor(name: String): Override<*>? = getOverrideMatching(name)

	@Suppress("UNCHECKED_CAST")
	private fun getFallbackFor(
		name: String,
		type: Type
	): Fallback<*> {
		val clazz =
			if (Class::class.java.isAssignableFrom(type.javaClass)) {
				type as Class<*>
			} else {
				(type as ParameterizedType).rawType as Class<*>
			}
		val preset = getFallbackMatching(clazz)

		if (preset != null) {
			return preset
		}

		if (Enum::class.java.isAssignableFrom(clazz)) {
			return object : Fallback<Any> {
				override val type = clazz.kotlin as KClass<Any>
				override val factory = { clazz.enumConstants.first() }
			}
		}

		if (List::class.java == clazz) {
			return object : Fallback<Any> {
				override val type = clazz.kotlin as KClass<Any>
				override val factory = {
					val innerType = getGenericTypeFromType(type)
					if (innerType != null) {
						mutableListOf(getValueFor("$name[0]", innerType))
					} else {
						mutableListOf<Any>()
					}
				}
			}
		}

		if (Set::class.java == clazz) {
			return object : Fallback<Any> {
				override val type = clazz.kotlin as KClass<Any>
				override val factory = {
					mutableSetOf(
						getValueFor("$name[0]", (type as ParameterizedType).actualTypeArguments[0] as Class<*>)
					)
				}
			}
		}

		if (Map::class.java == clazz) {
			return object : Fallback<Any> {
				override val type = clazz.kotlin as KClass<Any>
				override val factory = {
					mapOf(
						getValueFor(
							"$name.key",
							(type as ParameterizedType).actualTypeArguments[0] as Class<*>
						) to
							getValueFor(
								"$name.value",
								type.actualTypeArguments[1] as Class<*>
							)
					)
				}
			}
		}

		return object : Fallback<Any> {
			override val type = clazz.kotlin as KClass<Any>
			override val factory = { getNewBuilder(clazz).build() }
		}
	}

	abstract fun getNewBuilder(jvmType: Class<*>): Builder<*>

	private fun getOverrideMatching(name: String) = overrides.findLast { preset -> preset.name == name }

	private fun getFallbackMatching(type: Class<*>) = fallbacks.findLast { preset -> preset.type == type.kotlin }

	private fun getGenericTypeFromType(type: Type): Type? {
		if (type !is ParameterizedType) {
			return null
		}
		val typeArg = type.actualTypeArguments[0]
		if (typeArg is WildcardType) return (typeArg).upperBounds[0]
		return typeArg
	}
}

interface Preset<C : Any> {
	val factory: Factory<C>
}

interface Override<C : Any> : Preset<C> {
	val name: String
}

interface Fallback<C : Any> : Preset<C> {
	val type: KClass<C>
}

typealias Factory<C> = () -> C?
