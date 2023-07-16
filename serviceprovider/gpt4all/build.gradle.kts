plugins {
    kotlin("jvm")
    `java-library`
    id("maven-publish")
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":core"))
    implementation("com.hexadevlabs:gpt4all-java-binding:1.1.3")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // https://junit.org/junit5/docs/current/user-guide/
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

apply(from = "../../publish.gradle")
