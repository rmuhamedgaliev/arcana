import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("io.github.rmuhamedgaliev.arcana.MainKt")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    // Database
    implementation("com.h2database:h2:2.2.224") // H2 Database
    implementation("com.zaxxer:HikariCP:5.1.0") // Connection pooling

    // Telegram Bot API
    implementation("org.telegram:telegrambots:6.9.7.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("arcana-bot")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) // Must match jvmToolchain
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    register("buildImage") {
        dependsOn("shadowJar")
        dependsOn("jibDockerBuild")
        description = "Builds the application and creates a Docker/Podman image"
        group = "build"
    }

    register("pushImage") {
        dependsOn("shadowJar")
        dependsOn("jib")
        description = "Builds the application and pushes the Docker/Podman image to a registry"
        group = "publishing"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

jib {
    from {
        image = "quay.io/lib/eclipse-temurin:21-jre"
    }
    to {
        image = "arcana-bot"
        tags = setOf("latest")
    }
    container {
        mainClass = application.mainClass.get()
        jvmFlags = listOf("--enable-native-access=ALL-UNNAMED")
        ports = listOf("8080")
        volumes = listOf("/app/games")
        environment = mapOf("GAMES_DIRECTORY" to "/app/games")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}
