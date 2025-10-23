plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.google.android.libraries"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            aidl.srcDirs("src/main/aidl")
            res.srcDirs("src/main/res")
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility(libs.versions.jvmVersion.get())
        targetCompatibility(libs.versions.jvmVersion.get())
    }

    kotlin {
        jvmToolchain(libs.versions.jvmVersion.get().toInt())
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.service)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Test
    testImplementation(libs.test.runner)
    testImplementation(libs.test.rules)
    testImplementation(libs.test.ext)
}