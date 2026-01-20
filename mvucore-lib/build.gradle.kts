plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.yavorcool.mvucore"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.findProperty("LIBRARY_GROUP") as String
            artifactId = project.findProperty("LIBRARY_ARTIFACT") as String
            version = project.findProperty("LIBRARY_VERSION") as String

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("MVU Core")
                description.set("A lightweight Model-View-Update (MVU) architecture library for Android")
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
}
