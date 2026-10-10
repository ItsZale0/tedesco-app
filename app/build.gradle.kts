plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.alessandro.tedesco"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alessandro.tedesco"
        minSdk = 26
        targetSdk = 35
        versionCode = 111
        versionName = "1.35.3"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// ── Genera version.json da build.gradle.kts (single source of truth) ──
// Elimina il bug ricorrente: version.json e versionCode divergevano.
// Dopo aver cambiato versionCode/versionName qui, lancia:
//   ./gradlew generateVersionJson
// Poi builda e committa: version.json è sempre allineato.
tasks.register("generateVersionJson") {
    doLast {
        val vc = android.defaultConfig.versionCode ?: 0
        val vn = android.defaultConfig.versionName ?: "0.0.0"
        val apkUrl = "https://github.com/ItsZale0/tedesco-app/releases/download/v$vn/Tedesco-v$vn.apk"
        val json = """
    {
      "versionCode": $vc,
      "versionName": "$vn",
      "changelog": "",
      "minVersionCode": 0,
      "apkUrl": "$apkUrl"
    }
    """.trimIndent()
        val out = rootProject.layout.projectDirectory.file("version.json").asFile
        out.writeText(json)
        println("generateVersionJson: wrote versionCode=$vc versionName=$vn to version.json")
    }
}
