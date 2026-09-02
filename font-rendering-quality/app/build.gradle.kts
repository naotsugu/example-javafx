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
        languageVersion = JavaLanguageVersion.of(26)
    }
}

application {
    mainClass = "org.example.Main"
    applicationDefaultJvmArgs = listOf(
        // Restricted methods will be blocked in a future release unless native access is enabled
        "--enable-native-access=javafx.graphics",
    )

}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "26"
    modules("javafx.controls")
}