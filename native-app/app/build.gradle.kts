import java.nio.file.Files
import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

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

graalvmNative {
    graalvmNative {
        binaries {
            named("main") {
                javaLauncher.set(javaToolchains.launcherFor {
                    project.extensions.getByType<JavaPluginExtension>().toolchain
                })
            }
        }
    }
}

tasks.named<BuildNativeImageTask>("nativeCompile") {
    // Gradle's "Copy" task cannot handle symbolic links, see https://github.com/gradle/gradle/issues/3982.
    // That is why links contained in the GraalVM distribution archive get broken during provisioning and are replaced by empty
    // files. Address this by recreating the links in the toolchain directory.
    val toolchainDir = options.get().javaLauncher.get().executablePath.asFile.parentFile.run {
        if (name == "bin") parentFile else this
    }

    val toolchainFiles = toolchainDir.walkTopDown().filter { it.isFile }
    val emptyFiles = toolchainFiles.filter { it.length() == 0L }

    // Find empty toolchain files that are named like other toolchain files and assume these should have been links.
    val links = toolchainFiles.mapNotNull { file ->
        emptyFiles.singleOrNull { it != file && it.name == file.name }?.let {
            file to it
        }
    }

    // Fix up symbolic links.
    links.forEach { (target, link) ->
        logger.quiet("Fixing up '$link' to link to '$target'.")

        if (link.delete()) {
            Files.createSymbolicLink(link.toPath(), target.toPath())
        } else {
            logger.warn("Unable to delete '$link'.")
        }
    }
}