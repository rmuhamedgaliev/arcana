plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.cloud.tools.jib") version "3.4.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("io.github.rmuhamedgaliev.arcana.TelegramGameLauncher")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
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
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(23)
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
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

jib {
    from {
        image = "eclipse-temurin:23-jre-alpine"
    }
    to {
        image = "arcana-bot"
        tags = setOf("latest")
    }
    container {
        mainClass = application.mainClass.get()
        ports = listOf("8080")
        volumes = listOf("/app/games")
        environment = mapOf("GAMES_DIRECTORY" to "/app/games")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}
