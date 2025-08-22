plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

javafx {
    version = "24"
    modules("javafx.controls")
}

application {
    mainClass = "org.example.Launcher"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
