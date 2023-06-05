plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

application {
    mainClass.set("com.example.App")
    applicationDefaultJvmArgs = listOf(
        "--add-exports", "javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED",
        )
}

javafx {
    version = "20"
    modules("javafx.graphics")
}
