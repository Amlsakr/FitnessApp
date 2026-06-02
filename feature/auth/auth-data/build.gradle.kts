plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aml_sakr.fitlife.feature.auth.data"
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
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":feature:auth:auth-domain"))
    testImplementation(libs.junit)
}
