plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.aml_sakr.fitlife.feature.progress.ui"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":feature:progress:progress-domain"))
    implementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
}
