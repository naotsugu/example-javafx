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
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "org.example.App"
    applicationDefaultJvmArgs = listOf(
        "-Djavafx.enablePreview=true",
        "--enable-native-access=javafx.graphics",
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "25"
    modules("javafx.controls")
}
