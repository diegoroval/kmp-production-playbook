import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "dev.playbook.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withDeviceTestBuilder { sourceSetTreeName = "test" }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    iosArm64()
    iosSimulatorArm64()
    // solo para testear el Keychain, en el simulador iOS falla con -25291
    macosArm64()
    // solo para correr commonTest rápido en CI
    jvm()

    // android y jvm comparten el cliente OkHttp
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("okhttp") {
                withCompilations {
                    it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
        }
        getByName("okhttpMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.okhttp.tls)
            implementation(libs.okhttp.mockwebserver)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
        }
    }
}
