import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val roomDeckSigningProperties = rootProject.extra["roomDeckSigningProperties"] as Properties
val roomDeckSigningEnabled = rootProject.extra["roomDeckSigningEnabled"] as Boolean

android {
    namespace = "io.github.tatselkrik.roomdeck"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.tatselkrik.roomdeck"
        minSdk = 31
        targetSdk = 36
        versionCode = 20
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (roomDeckSigningEnabled) {
            create("release") {
                storeFile = rootProject.file(roomDeckSigningProperties.getProperty("storeFile"))
                storePassword = roomDeckSigningProperties.getProperty("storePassword")
                keyAlias = roomDeckSigningProperties.getProperty("keyAlias")
                keyPassword = roomDeckSigningProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            if (roomDeckSigningEnabled) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures.compose = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    implementation(project(":remote"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
