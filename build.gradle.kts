plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20" apply false
    id("org.jmailen.kotlinter") version "3.12.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    afterEvaluate {
        val isAndroid = plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")

        if (isAndroid) {
            dependencies {
                add("compileOnly", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
                add("compileOnly", "io.reactivex:rxjava:1.3.8")
                add("compileOnly", "io.reactivex:rxandroid:1.2.1")
                add("compileOnly", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                add("compileOnly", "org.jsoup:jsoup:1.15.4")
                // 👇 REMPLACEMENT : On utilise la version officielle publique au lieu de la version bloquée
                add("compileOnly", "org.json:json:20210307") 
            }
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}