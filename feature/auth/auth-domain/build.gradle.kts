plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":core:core-domain"))
    testImplementation(libs.junit)
}
