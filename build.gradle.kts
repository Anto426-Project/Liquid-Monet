plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

allprojects {
    group = "com.anto426.liquidmonet"
    // Build number suffix is injected by the CI runner (GITHUB_RUN_NUMBER) so that
    // every push produces a unique, monotonically increasing Maven version.
    // Locally the version resolves to "2.0.0-local" which never clashes with CI builds.
    version = providers.gradleProperty("sdkVersion").orElse("2.0.${providers.environmentVariable("GITHUB_RUN_NUMBER").orElse("0-local").get()}").get()
}

