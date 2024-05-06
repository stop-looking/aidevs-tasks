import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.freefair.lombok") version "8.6"
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring") version "1.9.23"
}

group = "org.j55"
version = "0.1.0-SNAPSHOT"

extra["springAiVersion"] = "0.8.1"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    implementation("org.springframework.ai:spring-ai-qdrant-store-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
//    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
/*
    implementation("com.google.protobuf:protobuf-java:3.20.0")
    implementation("io.grpc:grpc-protobuf:1.59.0")
    implementation("io.grpc:grpc-services:1.59.0")
    implementation("io.grpc:grpc-stub:1.59.0")
    implementation("io.qdrant:client:1.7.2")*/

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
