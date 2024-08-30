package org.example.restprovider.contracts

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
open class ContractTestConfiguration {
	@Bean
	open fun beanDefinitionRegistryPostProcessor(): BeanDefinitionRegistryPostProcessor {
		return MissingBeansMocker()
	}

	@Bean
	open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http.csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
		return http.build()
	}

	companion object {
		init {
			System.setProperty("pact_do_not_track", "true")
			System.setProperty("pact.showStacktrace", "true")
			System.setProperty(
				"pactbroker.url",
				System.getenv("PACT_BROKER_BASE_URL") ?: "https://the-pact-borker.url"
			)
			System.setProperty("pactbroker.auth.username", System.getenv("PACT_BROKER_USERNAME") ?: "the-username")
			System.setProperty("pactbroker.auth.password", System.getenv("PACT_BROKER_PASSWORD") ?: "the-password")

			if (System.getenv("CI") == "true") {
				val branch = System.getenv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME") ?: System.getenv("CI_COMMIT_BRANCH")
				System.setProperty("pact.verifier.publishResults", (branch == "main").toString())
				System.setProperty("pactbroker.providerBranch", branch)
				System.setProperty("pact.provider.branch", branch)
				System.setProperty("pact.provider.version", System.getenv("CI_COMMIT_SHORT_SHA"))
			}
		}
	}
}
