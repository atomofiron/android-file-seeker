plugins {
    id("app.fileseeker.convention.application")
}

android {
    namespace = "app.atomofiron.searchboxapp"

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.play.core)
    implementation(libs.play.core.ktx)
}