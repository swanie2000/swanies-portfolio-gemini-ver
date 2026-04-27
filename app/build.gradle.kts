plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

import java.util.Properties

// 🛡️ Force Metadata Resolution to fix Kotlin 2.1 Compatibility
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    }
}

val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use(::load)
    }
}

fun resolveLocalSecret(name: String): String {
    val fromLocalProperties = localProperties.getProperty(name)
    val fromGradleProperty = project.findProperty(name) as? String
    val fromEnv = System.getenv(name)
    return fromLocalProperties ?: fromGradleProperty ?: fromEnv ?: ""
}

android {
    namespace = "com.swanie.portfolio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.swanie.portfolio"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "REVENUECAT_API_KEY",
                "\"${resolveLocalSecret("REVENUECAT_API_KEY")}\""
            )
            buildConfigField("String", "REVENUECAT_PRO_ENTITLEMENT", "\"Swanies Portfolio Pro\"")
            buildConfigField("String", "REVENUECAT_OFFERING_ID", "\"default\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField(
                "String",
                "REVENUECAT_API_KEY",
                "\"${resolveLocalSecret("REVENUECAT_API_KEY")}\""
            )
            buildConfigField("String", "REVENUECAT_PRO_ENTITLEMENT", "\"Swanies Portfolio Pro\"")
            buildConfigField("String", "REVENUECAT_OFFERING_ID", "\"default\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE.txt")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Core Android & Compose
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    // Native Fingerprint and Face ID support
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Drag-to-reorder (maintained)
    implementation("sh.calvin.reorderable:reorderable:3.0.0")

    // Networking & Images
    implementation(libs.retrofit.main)
    implementation(libs.retrofit.gson)
    implementation(libs.coil.compose)

    // Room Database (Using KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Lifecycle & Storage
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // HILT
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // GLANCE WIDGETS
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // WORK MANAGER
    implementation(libs.androidx.work.runtime.ktx)

    // BILLING (RevenueCat)
    implementation(libs.revenuecat.purchases)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
