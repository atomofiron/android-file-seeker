plugins {
    `java-gradle-plugin` // needed?
    `kotlin-dsl`
}

group = "app.fileseeker.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        create("androidApplication") {
            id = "app.fileseeker.convention.application"
            implementationClass = "app.fileseeker.convention.AndroidApplicationConventionPlugin"
        }
        create("androidLibrary") {
            id = "app.fileseeker.convention.library"
            implementationClass = "app.fileseeker.convention.AndroidLibraryConventionPlugin"
        }
    }
}