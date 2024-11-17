plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Basic Kotlin dependency
    implementation(kotlin("stdlib"))

    // Add coroutines dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Test dependency
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
}

application {
    mainClass.set("performancetests.mergesort.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)  // Set Java toolchain to version 21
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "performancetests.mergesort.MainKt"  // Main class for running the app
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) // Include runtime dependencies
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("mergesortKotlin.jar")
}