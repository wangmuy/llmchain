plugins {
    kotlin("multiplatform")
    id("maven-publish")
    alias(libs.plugins.dokka)
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
        dependencies {
        }
    }
    wasm {
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }
        val jvmTest by getting

        val jsMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }
        val jsTest by getting

        val wasmMain by getting {
            dependencies {
                api(libs.coroutines.wasm)
                api(libs.kotlinx.datetime.wasm)
                api(libs.kotlinx.atomicfu.wasm)
            }
        }
        val wasmTest by getting

        val nativeMain by getting {
            dependencies {
                api(libs.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }
        val nativeTest by getting
    }
}

apply(from = "../publish.gradle")
