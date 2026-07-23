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
    //mainClass = "com.mammb.tabs.App"
    mainClass = "com.mammb.code.jfx.multitab.ExampleApp"
    applicationDefaultJvmArgs = listOf(
        "-XX:+UseCompactObjectHeaders",
        "--enable-native-access=javafx.graphics", // Restricted methods will be blocked in a future release unless native access is enabled
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "25"
    modules("javafx.controls")
}
