plugins {
    kotlin("jvm") version "2.3.20"
    application
}

application {
    mainClass.set("com.chingis.MainKt")
}

group = "com.chingis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}