import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

fun localReleaseSigning(): Properties {
  val properties = Properties()
  val signingFile = rootProject.file(".keystore/release-signing.properties")
  if (signingFile.exists()) {
    signingFile.inputStream().use(properties::load)
  }
  return properties
}

android {
  namespace = "com.jeiel.daddygifttracker"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.jeiel.daddygifttracker"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val signing = localReleaseSigning()
      val keystorePath = System.getenv("KEYSTORE_PATH")
        ?: signing.getProperty("KEYSTORE_PATH")
        ?: "${rootDir}/.keystore/daddy-gift-tracker-upload.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: signing.getProperty("STORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS") ?: signing.getProperty("KEY_ALIAS") ?: "daddy-gift-tracker"
      keyPassword = System.getenv("KEY_PASSWORD") ?: signing.getProperty("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

tasks.register<Copy>("exportReleaseToDesktop") {
  dependsOn("bundleRelease")

  val versionName = android.defaultConfig.versionName ?: "dev"
  val versionCode = android.defaultConfig.versionCode ?: 0
  val desktop = providers.provider {
    File(System.getProperty("user.home"), "Desktop").takeIf { it.exists() }
      ?: File(System.getenv("USERPROFILE") ?: System.getProperty("user.home"), "OneDrive/바탕 화면")
  }
  val buildDirOnDesktop = desktop.map { File(it, "Build") }

  from(layout.buildDirectory.file("outputs/bundle/release/app-release.aab")) {
    rename { "DaddyGiftTracker-v${versionName}-vc${versionCode}.aab" }
  }
  from(rootProject.file("play_store/release_notes/v${versionName}.txt")) {
    rename { "DaddyGiftTracker-v${versionName}-release-notes.txt" }
  }
  into(buildDirOnDesktop)
}
