plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.hydrotech"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hydrotech"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

}

dependencies {
    // Core Android libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")

    // Networking (OkHttp for HTTP requests)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")

    implementation ("com.github.lzyzsd:circleprogress:1.2.1")
    implementation ("org.json:json:20210307")// For handling JSON2")

    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    //graph
    //implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //implementation ("com.jjoe64:graphview:4.2.2")
    //implementation("com.github.AnyChart:AnyChart-Android:1.0.0")
    implementation ("com.github.AnyChart:AnyChart-Android:1.1.5")


}
