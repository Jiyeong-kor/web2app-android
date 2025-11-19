import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.jeong.web2app.webview"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            val localProperties = Properties()
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localProperties.load(localFile.inputStream())
            }
            val rawUrl = localProperties.getProperty("pwa.url")
            val pwaUrl = "\"$rawUrl\""
            buildConfigField("String", "PWA_URL", pwaUrl)
        }

        release {
            isMinifyEnabled = false
            // 실제 서비스할 도메인 주소
            buildConfigField("String", "PWA_URL", "\"https://www.your-production-domain.com\"")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}