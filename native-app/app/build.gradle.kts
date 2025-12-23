plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.graalvm.buildtools.native") version "0.11.1"
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
        vendor = JvmVendorSpec.GRAAL_VM
        nativeImageCapable = true
    }
}

application {
    mainClass = "org.example.Launcher"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "25"
    modules("javafx.controls")
}