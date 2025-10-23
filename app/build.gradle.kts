plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

android {
    namespace = "com.saulhdev.feeder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.saulhdev.neofeed"
        minSdk = 26
        targetSdk = 36
        versionCode = 1801
        versionName = "1.8.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Feed_${variant.versionName}_${variant.name}.apk"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        all {
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
            assets.srcDirs("src/main/assets")
            res.srcDirs("src/main/res")
        }
    }

    compileOptions {
        sourceCompatibility(libs.versions.jvmVersion.get())
        targetCompatibility(libs.versions.jvmVersion.get())
    }

    kotlin {
        jvmToolchain(libs.versions.jvmVersion.get().toInt())
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    packaging {
        resources.pickFirsts.add("rome-utils-2.1.0.jar")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":google-gsa"))

    implementation(libs.symbol.processing.api)
    implementation(libs.stdlib)
    implementation(libs.serialization.json)

    //Core
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.multidex)
    implementation(libs.swiperefreshlayout)
    implementation(libs.work.runtime.ktx)
    implementation(libs.datetime)
    implementation(libs.material)
    implementation(libs.browser)
    implementation(libs.collections.immutable)

    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.service)
    implementation(libs.lifecycle.viewmodel.ktx)

    //Compose
    api(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.navigationsuite)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.adaptive.layout)
    implementation(libs.compose.adaptive.navigation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.navigation.compose)

    //Room
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    //Squareup
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit) { exclude(module = "okhttp") }
    implementation(libs.retrofit.converter.gson)

    //Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    //Koin
    api(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.startup)
    implementation(libs.koin.annotations)
    ksp(libs.koin.compiler)

    //Libs
    implementation(libs.threetenabp)
    implementation(libs.rome) { exclude(module = "rome-utils") }
    implementation(libs.rome.modules)
    implementation(libs.readability4j)
    implementation(libs.tagsoup)
    implementation(libs.jsoup)
    implementation(libs.slf4j)

    // Test
    testImplementation(libs.test.runner)
    testImplementation(libs.test.rules)
    testImplementation(libs.test.ext)
}