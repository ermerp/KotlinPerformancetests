plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
}

application {
    // Define the main class for running the application
    mainClass.set("performancetests.bank.MainKt")
}

tasks.test {
    useJUnitPlatform()  // Enables JUnit platform for running tests
}

kotlin {
    jvmToolchain(21)  // Make sure JDK 21 is installed and compatible with your project
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "performancetests.bank.MainKt"  // Make sure this points to your actual Main class
    }

    // Include runtime dependencies in the JAR file
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE  // Avoid duplicate files in the JAR
    archiveBaseName.set("bankKotlin")  // Name of the final JAR file
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("bankKotlinFat")
    archiveClassifier.set("")
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Main-Class" to "performancetests.bank.MainKt"))
    }
}
