plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

val os   = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()
val arch = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentArchitecture()

dependencies {
    implementation(libs.gctoolkit.parser)
    implementation(libs.gctoolkit.vertx)
    implementation(libs.atlantafx.base)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    if (os.isMacOsX) {
        if (arch.isAmd64) runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.118.Final:osx-x86_64")
        else runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.118.Final")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.mammb.code.gclog.Main"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "21"
    modules("javafx.controls")
}
