package org.j55.aidevs.configuration

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = ["org.j55"])
@ConfigurationPropertiesScan(basePackages = ["org.j55"])
class ApplicationConfiguration