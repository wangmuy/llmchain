plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    id("maven-publish")
    alias(libs.plugins.dokka)
}

dependencies {
    implementation(libs.aallam.openai)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonJsonSchema)
    implementation(libs.jacksonModuleKotlin)

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // https://junit.org/junit5/docs/current/user-guide/
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testImplementation(libs.slf4j.simple)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

apply(from = "../publish.gradle")
