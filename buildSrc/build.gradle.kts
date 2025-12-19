plugins {
    `kotlin-dsl`
}

// 👇 C'est ce bloc qui manquait !
repositories {
    mavenCentral()
}