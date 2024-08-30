package org.example.eventconsumer.contracts

import org.mockito.Mockito
import org.springframework.beans.BeansException
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.PropertyValue
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.core.type.StandardMethodMetadata

/**
 * Based on https://github.com/rinoto/spring-auto-mock .
 */
class MissingBeansMocker() : BeanDefinitionRegistryPostProcessor {
	@Throws(BeansException::class)
	override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
		for (beanName in registry.beanDefinitionNames) {
			val beanDefinition = registry.getBeanDefinition(beanName)
			registerMocksForBean(registry, beanDefinition)
		}
	}

	fun registerMocksForBean(
		registry: BeanDefinitionRegistry,
		beanDefinition: BeanDefinition
	) {
		val beanClass = getBeanClass(beanDefinition)
		registerMocksForClass(registry, beanClass)
	}

	private fun getBeanClass(beanDefinition: BeanDefinition): Class<*>? {
		val beanClassName = beanDefinition.beanClassName ?: return getClassFromMethodMetadata(beanDefinition)
		return try {
			Class.forName(beanClassName)
		} catch (ex: ClassNotFoundException) {
			throw RuntimeException("Class not found for bean: $beanClassName")
		}
	}

	private fun getClassFromMethodMetadata(beanDefinition: BeanDefinition): Class<*>? {
		val source = beanDefinition.source
		if (source != null && StandardMethodMetadata::class.java.isInstance(source)) {
			val methodMetadata = StandardMethodMetadata::class.java.cast(source)
			val introspectedMethod = methodMetadata.introspectedMethod
			return introspectedMethod.returnType
		}
		return null
	}

	private fun registerMocksForClass(
		registry: BeanDefinitionRegistry,
		beanClass: Class<*>?
	) {
		if (beanClass == null) {
			return
		}
		for (fieldDef in findAllAutoWired(beanClass)) {
			if (!isBeanAlreadyRegistered(registry, fieldDef)) {
				registerMockFactoryBeanForField(registry, fieldDef)
			}
		}
		// the parents also need to be registered
		registerMocksForClass(registry, beanClass.superclass)
	}

	private fun findAllAutoWired(targetBean: Class<*>): Set<Class<*>> {
		// first finding all fields
		val autowired: MutableSet<Class<*>> = HashSet()
		val constructors = targetBean.declaredConstructors
		for (constructor in constructors) {
			val typeParameters = constructor.parameterTypes
			for (typeParameter in typeParameters) {
				autowired.add(typeParameter)
			}
		}
		return autowired
	}

	private fun isBeanAlreadyRegistered(
		registry: BeanDefinitionRegistry,
		fieldDef: Class<*>
	): Boolean {
		if (ListableBeanFactory::class.java.isInstance(registry)) {
			val listableBeanFactory = ListableBeanFactory::class.java.cast(registry)
			return listableBeanFactory.getBeanNamesForType(fieldDef).isNotEmpty()
		}
		return registry.isBeanNameInUse(fieldDef.simpleName)
	}

	private fun registerMockFactoryBeanForField(
		registry: BeanDefinitionRegistry,
		fieldDef: Class<*>
	) {
		val mockFactoryBeanDefinition = GenericBeanDefinition()
		mockFactoryBeanDefinition.setBeanClass(MockFactoryBean::class.java)
		val values = MutablePropertyValues()
		values.addPropertyValue(PropertyValue("type", fieldDef))
		mockFactoryBeanDefinition.propertyValues = values
		mockFactoryBeanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, fieldDef)
		registry.registerBeanDefinition(fieldDef.simpleName, mockFactoryBeanDefinition)
	}

	companion object {
		private val MOCKS = mutableMapOf<Class<*>, Any>()

		fun initMocks() {
			for (mock in MOCKS) {
				Mockito.reset(mock.value)
			}
		}
	}

	class MockFactoryBean : FactoryBean<Any> {
		var type: Class<*>? = null

		@Throws(Exception::class)
		override fun getObject(): Any {
			return Mockito.mock(type).also {
				MOCKS[type!!] = it
			}
		}

		override fun getObjectType(): Class<*>? {
			return type
		}

		override fun isSingleton(): Boolean {
			return true
		}
	}
}
