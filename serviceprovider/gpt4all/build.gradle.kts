plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    id("maven-publish")
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.gpt4all)
    implementation(libs.slf4j.simple)

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // https://junit.org/junit5/docs/current/user-guide/
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

apply(from = "../../publish.gradle")
