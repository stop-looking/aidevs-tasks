package org.j55.aidevs.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aidevs")
data class AiDevsProperties(val apiKey: String)
