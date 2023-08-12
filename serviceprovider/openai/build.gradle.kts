import org.apache.tools.ant.taskdefs.condition.Os
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useMocha()
            }
        }
        nodejs {
        }
        binaries.executable()
        dependencies {
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.coroutines.core)
                implementation(libs.aallam.openai)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    implementation(libs.ktor.client.winhttp)
                } else {
                    implementation(libs.ktor.client.cio)
                }
            }
        }
        val nativeTest by getting
    }
}

apply(from = "../../publishUrl.gradle")
