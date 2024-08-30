package org.example.eventconsumer.contracts.matchers

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.core.support.json.JsonValue
import org.example.eventconsumer.model.CredentialsUserMessageDto
import org.example.eventconsumer.model.LdapUserMessageDto
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType

class Matcher {
	companion object {
		private val registry = mapOf<Class<*>, (PactDslJsonBody) -> Unit>(
			CredentialsUserMessageDto::class.java to {
				it.equalTo("type", "credentialsUserMessage")
			},
			LdapUserMessageDto::class.java to {
				it.equalTo("type", "ldapUserMessage")
			}
		)

		@OptIn(ExperimentalStdlibApi::class)
		fun <T : Any> `for`(
			clazz: Class<T>,
			existingBody: PactDslJsonBody = PactDslJsonBody()
		): PactDslJsonBody {
			var body = existingBody

			val overrideFunction = registry[clazz]
			if (overrideFunction != null) {
				overrideFunction(body)
			}

			val setters = clazz.methods.filter { it.name.startsWith("set") }
			for (setter in setters) {
				var fieldName =
					setter.name.substringAfter("set")
						.replaceFirstChar { it.lowercase(Locale.getDefault()) }

				if (body.body.has(fieldName)) continue
				if (body.body is JsonValue.Array && (body.body as JsonValue.Array)[0].has(fieldName)) {
					continue
				}
				val type = setter.parameterTypes[0]

				body = getBodyValue(body, fieldName, type, setter.genericParameterTypes[0])
			}
			val fields = clazz.kotlin.memberProperties
			for (field in fields) {
				var fieldName = field.name
				if(body.body.has(fieldName)) continue
				if (body.body is JsonValue.Array && (body.body as JsonValue.Array)[0].has(fieldName)) {
					continue
				}
				val type = field.returnType

				body = getBodyValue(body, fieldName, type.javaType as Class<*>, null)
			}
			return body
		}

		private fun getBodyValue(
			body: PactDslJsonBody,
			fieldName: String,
			clazz: Class<*>,
			genericType: Type? = null
		): PactDslJsonBody {
			if (clazz == String::class.java) {
				return body.stringType(fieldName)
			}

			if (clazz == Boolean::class.javaObjectType || clazz == Boolean::class.javaPrimitiveType) {
				return body.booleanType(fieldName)
			}

			if (clazz == Integer::class.javaObjectType || clazz == Integer::class.javaPrimitiveType) {
				return body.integerType(fieldName)
			}

			if (clazz == Float::class.javaObjectType || clazz == Float::class.javaPrimitiveType) {
				return body.decimalType(fieldName)
			}

			if (clazz == Double::class.javaObjectType || clazz == Double::class.javaPrimitiveType) {
				return body.decimalType(fieldName)
			}

			if (clazz == Long::class.javaObjectType || clazz == Long::class.javaPrimitiveType) {
				return body.integerType(fieldName)
			}

			if (clazz.isEnum) {
				val sample =
					(clazz.enumConstants[0] as Enum<*>)::class.java
						.getMethod("getValue")
						.invoke(clazz.enumConstants[0])

				return if (sample::class.java == String::class.java) {
					body.stringMatcher(fieldName, clazz.enumConstants.joinToString("|") { it.toString() })
				} else {
					body.integerMatching(
						fieldName,
						clazz.enumConstants.joinToString("|") { it.toString() },
						sample as Int
					)
				}
			}

			if (
				List::class.java.isAssignableFrom(clazz) ||
				Set::class.java.isAssignableFrom(clazz)
			) {
				return getArrayValue(body, fieldName, (genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)
			}

			if (Array::class.java.isAssignableFrom(clazz)) {
				return getArrayValue(body, fieldName, clazz.componentType)
			}

			if (clazz.simpleName.endsWith("Dto")) {
				return body.`object`(fieldName, `for`(clazz))
			}

			if (Instant::class.java.isAssignableFrom(clazz)) {
				return body.datetime(fieldName, "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
			}

			if (LocalDate::class.java.isAssignableFrom(clazz)) {
				return body.datetime(fieldName, "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
			}

			if (UUID::class.java.isAssignableFrom(clazz)) {
				return body.uuid(fieldName)
			}

			throw IllegalArgumentException("Unable to infer Pact-Type for $fieldName: ${clazz.name}")
		}

		private fun getArrayValue(
			body: PactDslJsonBody,
			fieldName: String,
			clazz: Class<*>
		): PactDslJsonBody {
			if (clazz == String::class.java) {
				return body.minArrayLike(fieldName, 1, PactDslJsonRootValue.stringType())
			}

			if (clazz == Boolean::class.javaObjectType) {
				return body.minArrayLike(fieldName, 1, PactDslJsonRootValue.booleanType())
			}

			if (clazz == Integer::class.javaObjectType) {
				return body.minArrayLike(fieldName, 1, PactDslJsonRootValue.integerType())
			}

			if (clazz == Float::class.javaObjectType) {
				return body.minArrayLike(fieldName, 1, PactDslJsonRootValue.decimalType())
			}

			if (clazz == Double::class.javaObjectType) {
				return body.minArrayLike(fieldName, 1, PactDslJsonRootValue.decimalType())
			}

			if (clazz.isEnum) {
				return body
					.unorderedMinArray(fieldName, 1)
					.stringMatcher(clazz.enumConstants.joinToString("|") { it.toString() }, clazz.enumConstants[0].toString())
					.closeArray() as PactDslJsonBody
			}

			if (
				List::class.java.isAssignableFrom(clazz) ||
				Set::class.java.isAssignableFrom(clazz)
			) {
				throw IllegalArgumentException("Currently not able to determine generic type of Lists that are not fields.")
			}

			if (Array::class.java.isAssignableFrom(clazz)) {
				throw IllegalArgumentException("Nested Arrays not supported")
			}

			if (clazz.simpleName.endsWith("Dto")) {
				return body.eachLike(fieldName, `for`(clazz))
			}

			if (Instant::class.java.isAssignableFrom(clazz)) {
				return body.unorderedMinArray(fieldName, 1).datetime("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'").closeArray() as PactDslJsonBody
			}

			if (UUID::class.java.isAssignableFrom(clazz)) {
				return body.unorderedMinArray(fieldName, 1).uuid().closeArray() as PactDslJsonBody
			}

			throw IllegalArgumentException("Unable to infer Pact-Type for array item of type: ${clazz.name}")
		}
	}
}
