package org.j55.aidevs.configuration

import org.j55.infrastructure.AiDevsClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiDevsConfiguration {

    @Bean
    fun aiDevsClient(aidevsProperties: AiDevsProperties): AiDevsClient {
        return AiDevsClient(aidevsProperties.apiKey, 120000L, "ownapipro")
    }
}