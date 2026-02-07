import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
        }
    }
}

android {
    namespace = "com.yavorcool.mvucore"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

group = project.findProperty("LIBRARY_GROUP") as String
version = project.findProperty("LIBRARY_VERSION") as String

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("MVU Core")
            description.set("A lightweight Model-View-Update (MVU) architecture library for Kotlin Multiplatform")
            url.set("https://github.com/yavorcool/mvucore")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("yavorcool")
                    name.set("Nikita")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/yavorcool/mvucore.git")
                developerConnection.set("scm:git:ssh://github.com/yavorcool/mvucore.git")
                url.set("https://github.com/yavorcool/mvucore")
            }
        }
    }
}
