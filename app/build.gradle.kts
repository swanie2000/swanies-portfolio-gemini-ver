plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

import java.util.Properties
import java.util.zip.ZipFile

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

/** Google Play / store builds: RevenueCat *public* SDK key (dashboard → usually `goog_…`). Never use a `test_` key here — the SDK exits the app on release if it sees a test key. */
fun resolveRevenueCatPublicApiKey(): String = resolveLocalSecret("REVENUECAT_PUBLIC_API_KEY").trim()

/** Last calendar day beta unlock codes can be redeemed (yyyy-MM-dd). Public in APK; not secret. */
fun resolveBetaUnlockProgramEnd(): String =
    resolveLocalSecret("BETA_UNLOCK_PROGRAM_END").trim().ifBlank { "2027-06-01" }

/** Fails release bundles if the Play key is missing or still a sandbox `test_` key. */
fun validateRevenueCatPublicApiKeyForRelease() {
    val key = resolveRevenueCatPublicApiKey()
    check(key.isNotBlank()) {
        "Release build blocked: set REVENUECAT_PUBLIC_API_KEY=goog_… in local.properties " +
            "(RevenueCat dashboard → Swanies Portfolio → API keys → Public app-specific key). " +
            "REVENUECAT_API_KEY (test_…) is for debug only."
    }
    check(!key.startsWith("test_")) {
        "Release build blocked: REVENUECAT_PUBLIC_API_KEY must be the Play production key (goog_…), not test_…"
    }
    check(!key.contains("paste", ignoreCase = true) && key.length >= 20) {
        "Release build blocked: REVENUECAT_PUBLIC_API_KEY looks like a placeholder. " +
            "Paste the real goog_… key from RevenueCat → Project → API keys → Google Play Store."
    }
}

/** Beta unlock codes are HMAC-signed at compile time — empty secret means every code shows "not valid". */
fun validateBetaUnlockSecretForRelease() {
    val secret = resolveLocalSecret("BETA_UNLOCK_SECRET").trim()
    check(secret.isNotBlank()) {
        "Release build blocked: set BETA_UNLOCK_SECRET in local.properties (same value as GitHub secret). " +
            "Without it, beta unlock codes never validate in Play builds."
    }
    check(secret.length >= 8) {
        "Release build blocked: BETA_UNLOCK_SECRET must be at least 8 characters."
    }
}

/** Free key from https://web3forms.com — bug reports + website join-testing form (domain-restrict in dashboard). */
fun resolveWeb3FormsAccessKey(): String = resolveLocalSecret("WEB3FORMS_ACCESS_KEY").trim()

android {
    namespace = "com.swanie.portfolio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.swanie.portfolio"
        minSdk = 24
        targetSdk = 35
        versionCode = 19
        versionName = "1.0.19"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "WEB3FORMS_ACCESS_KEY", "\"${resolveWeb3FormsAccessKey()}\"")
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
            buildConfigField("String", "BETA_UNLOCK_SECRET", "\"${resolveLocalSecret("BETA_UNLOCK_SECRET")}\"")
            buildConfigField("String", "BETA_UNLOCK_PROGRAM_END", "\"${resolveBetaUnlockProgramEnd()}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField(
                "String",
                "REVENUECAT_API_KEY",
                "\"${resolveRevenueCatPublicApiKey()}\"",
            )
            buildConfigField("String", "REVENUECAT_PRO_ENTITLEMENT", "\"Swanies Portfolio Pro\"")
            buildConfigField("String", "REVENUECAT_OFFERING_ID", "\"default\"")
            buildConfigField("String", "BETA_UNLOCK_SECRET", "\"${resolveLocalSecret("BETA_UNLOCK_SECRET")}\"")
            buildConfigField("String", "BETA_UNLOCK_PROGRAM_END", "\"${resolveBetaUnlockProgramEnd()}\"")
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

    // In-app language picker (19 locales): Play must ship all translations, not only the
    // device language split. Otherwise setApplicationLocales("ko") falls back to English.
    bundle {
        language {
            enableSplit = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

/** Run before every release AAB/APK so Play never gets a test RevenueCat key again. */
listOf("bundleRelease", "assembleRelease", "packageRelease").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("validateRevenueCatReleaseKey", "validateBetaUnlockReleaseSecret")
    }
}
tasks.register("validateRevenueCatReleaseKey") {
    group = "verification"
    description = "Ensures REVENUECAT_PUBLIC_API_KEY is set to goog_… (not test_…)"
    doLast { validateRevenueCatPublicApiKeyForRelease() }
}
tasks.register("validateBetaUnlockReleaseSecret") {
    group = "verification"
    description = "Ensures BETA_UNLOCK_SECRET is set so beta codes validate in Play builds"
    doLast { validateBetaUnlockSecretForRelease() }
}

/** After bundleRelease, fails if the AAB still embeds a sandbox `test_` RevenueCat key. */
tasks.register("verifyReleaseBundleRevenueCatKey") {
    group = "verification"
    description = "Scans release AAB for test_ vs goog_ RevenueCat SDK key strings"
    dependsOn("bundleRelease")
    doLast {
        val bundleDir = layout.buildDirectory.dir("outputs/bundle/release").get().asFile
        val aab =
            bundleDir
                .listFiles()
                ?.firstOrNull { it.isFile && it.extension == "aab" }
                ?: error("No release .aab under ${bundleDir.absolutePath} — run bundleRelease first")
        var hasTestKey = false
        var hasProductionKey = false
        ZipFile(aab).use { zip ->
            zip.entries().asSequence().filter { it.name.endsWith(".dex") }.forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val text = input.readBytes().decodeToString()
                    if (text.contains("test_dz")) hasTestKey = true
                    if (text.contains("goog_")) hasProductionKey = true
                }
            }
        }
        check(!hasTestKey) {
            "Release AAB still contains sandbox RevenueCat key (test_dz…). " +
                "Build → Clean Project, confirm REVENUECAT_PUBLIC_API_KEY=goog_… in local.properties, " +
                "then rebuild a signed release bundle."
        }
        check(hasProductionKey) {
            "Release AAB is missing production RevenueCat key (goog_…). " +
                "Set REVENUECAT_PUBLIC_API_KEY in local.properties and rebuild."
        }
        logger.lifecycle("Verified ${aab.name}: production RevenueCat key (goog_…), no test_dz")
    }
}

/** After bundleRelease, fails if BETA_UNLOCK_SECRET from local.properties is not embedded in the AAB. */
tasks.register("verifyReleaseBundleBetaUnlockSecret") {
    group = "verification"
    description = "Scans release AAB for BETA_UNLOCK_SECRET from local.properties"
    dependsOn("bundleRelease")
    doLast {
        val secret = resolveLocalSecret("BETA_UNLOCK_SECRET").trim()
        check(secret.isNotBlank()) {
            "Set BETA_UNLOCK_SECRET in local.properties before verifying the release bundle."
        }
        val needle = secret.take(16)
        val bundleDir = layout.buildDirectory.dir("outputs/bundle/release").get().asFile
        val aab =
            bundleDir
                .listFiles()
                ?.firstOrNull { it.isFile && it.extension == "aab" }
                ?: error("No release .aab under ${bundleDir.absolutePath} — run bundleRelease first")
        var found = false
        ZipFile(aab).use { zip ->
            zip.entries().asSequence().filter { it.name.endsWith(".dex") }.forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    if (input.readBytes().decodeToString().contains(needle)) found = true
                }
            }
        }
        check(found) {
            "Release AAB is missing BETA_UNLOCK_SECRET from local.properties. " +
                "Build → Clean Project, Sync Gradle, rebuild signed bundle, then regenerate unlock codes."
        }
        logger.lifecycle("Verified ${aab.name}: BETA_UNLOCK_SECRET embedded — codes from this PC should validate.")
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
    implementation("com.github.yalantis:ucrop:2.2.8")

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
