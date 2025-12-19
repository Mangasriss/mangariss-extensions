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

// 👇 LA SOLUTION MAGIQUE : On injecte les dépendances dans TOUS les sous-projets (Core + Extension)
subprojects {
    afterEvaluate {
        dependencies {
            // On ajoute "compileOnly" pour ne pas alourdir l'APK, mais permettre la compilation
            add("compileOnly", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            add("compileOnly", "io.reactivex:rxjava:1.3.8")
            add("compileOnly", "io.reactivex:rxandroid:1.2.1")
            add("compileOnly", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            add("compileOnly", "org.jsoup:jsoup:1.15.4")
            add("compileOnly", "com.github.infinum:org.json:20210307") // Parfois nécessaire pour le core
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}