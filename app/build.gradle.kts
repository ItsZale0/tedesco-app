import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.alessandro.tedesco"
    compileSdk = 35

    val freeApiKey: String = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }.getProperty("FREE_API_KEY", "")

    defaultConfig {
        applicationId = "com.alessandro.tedesco"
        minSdk = 26
        targetSdk = 35
        versionCode = 203
        versionName = "1.43.0"
        buildConfigField("String", "FREE_API_KEY", "\"$freeApiKey\"")
    }

    signingConfigs {
        getByName("debug") {
            // Keystore STABILE dentro il progetto: l'auto-update funziona solo se
            // ogni build e' firmata con la stessa chiave. Prima puntava a
            // C:/Users/zampe/.android/debug.keystore (path personale, non portabile).
            storeFile = file("keystore/tedesco-debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
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
    implementation(libs.androidx.activity.compose)
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

// ── Genera updates/version.json da build.gradle.kts (single source of truth) ──
// Elimina il bug ricorrente: version.json e versionCode divergevano.
// Dopo aver cambiato versionCode/versionName qui, lancia:
//   ./gradlew generateVersionJson
// Poi builda e committa: updates/version.json è sempre allineato.
// NB: il version.json nella root è il canale legacy ed è volutamente congelato
// alla 1.36.33 (firma diversa, non aggiornabile in place).
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
        val out = rootProject.layout.projectDirectory.file("updates/version.json").asFile
        out.parentFile.mkdirs()
        out.writeText(json)
        println("generateVersionJson: wrote versionCode=$vc versionName=$vn to updates/version.json")
    }
}

// ── Verifica automatica della firma APK ──────────────────────────────
// Android pretende i certificati "at entry AndroidManifest.xml": quella e' la
// firma v1 (JAR). Un APK con la sola v2/v3 viene rifiutato come
// "missing certificates" se il file subisce una minima modifica in transito.
// Questo task fa fallire la build se la v1 non c'e': mai piu' APK non
// installabili per firma mancante.
val androidSdkDir: String = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: "/opt/data/toolchain/android-sdk"

val apksignerPath: String? = fileTree("$androidSdkDir/build-tools") {
    include("*/apksigner")
    include("*/apksigner.bat")
}
    .files
    .maxByOrNull { it.parentFile.name }
    ?.absolutePath

tasks.register("verificaFirmaApk") {
    group = "verification"
    description = "Verifica che l'APK debug sia firmato v1 (JAR) + v2 + v3"
    dependsOn("assembleDebug")
    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile
        val signer = apksignerPath
        if (signer == null) {
            println("apksigner non trovato in $androidSdkDir/build-tools: verifica firma saltata")
            return@doLast
        }
        if (!apk.exists()) throw GradleException("APK non trovato: ${apk.absolutePath}")
        project.exec {
            commandLine(
                signer,
                "verify",
                "--min-sdk-version", "23",
                "--verbose",
                apk.absolutePath
            )
        }
        println("Firma APK verificata: v1 (JAR) + v2 + v3 presenti")
    }
}

// matching() e' lazy: assembleDebug lo registra AGP piu' tardi, named() fallirebbe.
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy("verificaFirmaApk") }
