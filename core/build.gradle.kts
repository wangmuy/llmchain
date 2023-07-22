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
    implementation(libs.theokanningOpenai.api)
    implementation(libs.theokanningOpenai.client)
    implementation(libs.theokanningOpenai.service)
    implementation(libs.jacksonDatabind)
    implementation(libs.okhttp)
    implementation(libs.okhttp.loggingInterceptor)
    implementation(libs.retrofitJackson)

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

apply(from = "../publish.gradle")
