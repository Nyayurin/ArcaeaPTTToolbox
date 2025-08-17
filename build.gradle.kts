@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	kotlin("multiplatform") version "2.2.10"
	id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
	id("org.jetbrains.compose") version "1.8.2"
	id("com.android.application") version "8.11.0"
	kotlin("plugin.serialization") version "2.1.0"
	id("com.goncalossilva.resources") version "0.10.1"
}

repositories {
	google()
	mavenCentral()
}

kotlin {
	jvmToolchain(17)

	jvm()

	androidTarget()
	iosArm64()
	iosSimulatorArm64()
	iosX64()

	/*wasmJs {
		browser {
			commonWebpackConfig {
				outputFileName = "apt.js"
			}
		}
		binaries.executable()
	}*/

	sourceSets {
		commonMain {
			kotlin.setSrcDirs(listOf("src/main/kotlin"/*, "build/generated/moko-resources/"*/))
			resources.setSrcDirs(listOf("src/main/resources"))
			dependencies {
				implementation(compose.runtime)
				implementation(compose.ui)
				implementation(compose.foundation)
				implementation(compose.material3)
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
				implementation("io.github.panpf.sketch4:sketch-compose:4.0.0")
				implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
				implementation("io.github.vinceglb:filekit-compose:0.8.8")
				implementation("io.ktor:ktor-client-core:3.2.3")
				implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
				implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
				implementation("com.russhwolf:multiplatform-settings:1.3.0")
				implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
				implementation("com.goncalossilva:resources:0.10.1")
			}
		}

		jvmMain {
			kotlin.setSrcDirs(listOf("src/main@jvm/kotlin"))
			resources.setSrcDirs(listOf("src/main@jvm/resources"))
			dependencies {
				implementation(compose.desktop.currentOs)
				implementation("io.ktor:ktor-client-cio:3.2.3")
			}
		}

		androidMain {
			kotlin.setSrcDirs(listOf("src/main@android/kotlin"))
			resources.setSrcDirs(listOf("src/main@android/resources"))
			dependencies {
				implementation("androidx.activity:activity-compose:1.10.1")
				implementation("io.ktor:ktor-client-cio:3.2.3")
			}
		}

		iosMain {
			kotlin.setSrcDirs(listOf("src/main@ios/kotlin"))
			resources.setSrcDirs(listOf("src/main@ios/resources"))
			dependencies {
				implementation("io.ktor:ktor-client-cio:3.2.3")
			}
		}

		wasmJsMain {
			kotlin.setSrcDirs(listOf("src/main@wasm/kotlin"))
			resources.setSrcDirs(listOf("src/main@wasm/resources"))
			dependencies {
				implementation("io.ktor:ktor-client-js:3.2.3")
			}
		}
	}
}

android {
	sourceSets["main"].apply {
		resources.setSrcDirs(listOf("src/main@android/resources"))
		java.setSrcDirs(listOf("src/main@android/java"))
		res.setSrcDirs(listOf("src/main@android/res"))
		assets.setSrcDirs(listOf("src/main@android/assets"))
		baselineProfiles.setSrcDirs(listOf("src/main@android/baselineProfiles"))
		shaders.setSrcDirs(listOf("src/main@android/shaders"))
	}

	signingConfigs {
		create("release") {
			storeFile = file("ArcaeaPTTToolbox.jks")
			storePassword = System.getenv("SIGNING_STORE_PASSWORD")
			keyAlias = System.getenv("SIGNING_KEY_ALIAS")
			keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
		}
	}

	namespace = "cn.yurin.arcaea.ptt.toolbox"
	compileSdk = 36

	defaultConfig {
		minSdk = 26
		targetSdk = 36

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