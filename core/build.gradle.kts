plugins {
    kotlin("jvm") version "1.8.20"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.theokanning.openai-gpt3-java:api:0.12.0")
    implementation("com.theokanning.openai-gpt3-java:client:0.12.0")
    implementation("com.theokanning.openai-gpt3-java:service:0.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")

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