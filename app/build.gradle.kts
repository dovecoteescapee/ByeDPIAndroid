import com.android.build.gradle.internal.tasks.factory.dependsOn

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
        versionCode = 6
        versionName = "1.0.2-rc1"

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

abstract class BaseTun2SocksTask : DefaultTask() {
    @get:InputDirectory
    val tun2socksDir: File
        get() = project.file("libs/tun2socks")

    @get:OutputFile
    val tun2socksOutput: File
        get() = project.file("libs/tun2socks.aar")

    @Internal
    protected fun isUpToDate(): Boolean {
        if (tun2socksOutput.exists()) {
            val lastModified = tun2socksOutput.lastModified()
            return !tun2socksDir.walkTopDown().any {
                it.isFile && it.lastModified() > lastModified
            }
        }
        return false
    }
}

abstract class GetGomobileBind : BaseTun2SocksTask() {
    @TaskAction
    fun getBind() {
        if (isUpToDate()) {
            logger.lifecycle("No changes detected, skipping getBind.")
            return
        }

        project.exec {
            workingDir = tun2socksDir

            commandLine("go", "get", "golang.org/x/mobile/bind")
        }
    }
}

abstract class BuildTun2Socks : BaseTun2SocksTask() {
    @TaskAction
    fun buildTun2Socks() {
        if (isUpToDate()) {
            logger.lifecycle("No changes detected, skipping buildTun2Socks.")
            return
        }

        project.exec {
            workingDir = tun2socksDir

            commandLine(
                "gomobile", "bind",
                "-target", "android",
                "-androidapi", "21",
                "-o", tun2socksOutput,
                "-trimpath",
                "./engine"
            )
        }
    }
}

val getGomobileBind = tasks.register<GetGomobileBind>("getGomobileBind") {
    group = "build"
    description = "Get gomobile bind for compiling tun2socks"
}

val buildTun2Socks = tasks.register<BuildTun2Socks>("buildTun2Socks") {
    group = "build"
    description = "Build tun2socks for Android"
    dependsOn(getGomobileBind)
}

tasks.preBuild.dependsOn(buildTun2Socks)
