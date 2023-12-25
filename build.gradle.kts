plugins {
    kotlin("jvm") version "1.9.21"
    idea
}
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        setUrl("https://repo.akka.io/maven")
    }
}

val versions = mapOf(
    "ScalaBinary" to "2.13"
)

dependencies {
    implementation(enforcedPlatform("com.typesafe.akka:akka-bom_${versions["ScalaBinary"]}:2.9.1"))

    implementation("com.typesafe.akka:akka-actor-typed_${versions["ScalaBinary"]}")
    implementation("ch.qos.logback:logback-classic:1.2.13")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}