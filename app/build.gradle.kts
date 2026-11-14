plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "org.teslacast.poc"; compileSdk = 35
 defaultConfig { applicationId = "org.teslacast.poc"; minSdk = 29; targetSdk = 35; versionCode = 1; versionName = "0.1-poc" }
 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
}
kotlin { jvmToolchain(17) }
