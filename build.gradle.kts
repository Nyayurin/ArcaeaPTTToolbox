@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
    id("com.android.application") version "8.7.3"
    kotlin("plugin.serialization") version "2.1.0"
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()
    /*js {
        browser {
            commonWebpackConfig {
                outputFileName = "apt.js"
            }
        }
        binaries.executable()
    }
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "apt.js"
            }
        }
        binaries.executable()
    }*/

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            implementation("io.github.panpf.sketch4:sketch-compose:4.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            implementation("io.github.vinceglb:filekit-compose:0.8.8")
            implementation("io.ktor:ktor-client-core:3.1.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
        }

        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.10.1")
            implementation("io.ktor:ktor-client-cio:3.1.1")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("io.ktor:ktor-client-cio:3.1.1")
        }

        jsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.1.1")
        }

        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.1.1")
        }
    }
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("ArcaeaPTTToolbox.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    namespace = "cn.yurin.arcaea.ptt.toolbox"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35

        applicationId = "cn.yurin.arcaea.ptt.toolbox"
        versionCode = 1
        versionName = System.getenv("VERSION")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

compose.desktop {
    application {
        mainClass = "cn.yurin.arcaea.ptt.toolbox.MainKt"

        nativeDistributions {
            val os = System.getProperty("os.name")
            when {
                os.contains("Windows") -> targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.AppImage)
                os.contains("Linux") -> targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
                os.contains("Mac OS") -> targetFormats(TargetFormat.Dmg, TargetFormat.Pkg)
                else -> error("Unsupported OS: $os")
            }
            packageName = "Arcaea PTT Toolbox"
            packageVersion = System.getenv("VERSION")
            jvmArgs("-Dfile.encoding=UTF-8")

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}