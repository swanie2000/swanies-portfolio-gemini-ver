// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Explicitly defining KSP version here to fix the "Unable to load class" error
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false

    // Defining Hilt version
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}