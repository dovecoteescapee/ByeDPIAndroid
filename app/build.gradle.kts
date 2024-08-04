import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.dovecoteescapee.byedpi"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.dovecoteescapee.byedpi"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}\"")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}-debug\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(files("libs/tun2socks.aar"))

    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.takisoft.preferencex:preferencex:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

abstract class BuildTun2Socks : DefaultTask() {
    @TaskAction
    fun buildTun2Socks() {
        val projectDir = project.projectDir
        val tun2socksDir = projectDir.resolve("libs/tun2socks")
        val tun2socksOutput = projectDir.resolve("libs/tun2socks.aar")

        if (tun2socksOutput.exists()) {
            return
        }
        project.exec {
            workingDir = tun2socksDir
            commandLine("gomobile", "bind", "-o", tun2socksOutput, "-trimpath", "./engine")
        }
    }
}

tasks.register<BuildTun2Socks>("buildTun2Socks") {
    group = "build"
    description = "Build tun2socks"
}

tasks.withType(KotlinCompile::class).configureEach {
    dependsOn("buildTun2Socks")
}
