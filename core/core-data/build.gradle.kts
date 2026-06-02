plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aml_sakr.fitlife.core.data"
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
    implementation(project(":core:core-domain"))
    testImplementation(libs.junit)
}
