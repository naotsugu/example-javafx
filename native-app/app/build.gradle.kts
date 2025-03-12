plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.graalvm.buildtools.native") version "0.10.5"
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
        languageVersion = JavaLanguageVersion.of(23)
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

application {
    mainClass = "org.example.Launcher"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "23.0.2"
    modules("javafx.controls")
}