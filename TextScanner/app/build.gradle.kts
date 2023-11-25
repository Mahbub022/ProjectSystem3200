plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.textscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.textscanner"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // user permission
    implementation ("pub.devrel:easypermissions:3.0.0")
    //openCV to capture image from camera or gallery
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    // To recognize Latin script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    // XWPFD documnet  Apache POI Library
//    implementation ("org.apache.poi:poi:4.1.2")
//    implementation ("org.apache.poi:poi-ooxml:4.1.2")
//    implementation ("org.apache.pdfbox:pdfbox-android:2.0.27")
}