import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
val os   = DefaultNativePlatform.getCurrentOperatingSystem()
val arch = DefaultNativePlatform.getCurrentArchitecture()

// rust
val cargo = file(System.getProperty("user.home") + "/.cargo/bin/cargo")
val rustPrjDir = layout.projectDirectory.dir("src/main/rust").asFile
val rustTgtDir = rustPrjDir.resolve("target")
val rustSrcDir = rustPrjDir.resolve("src")
val cbindhFile = rustTgtDir.resolve("lib.h")

// jextract
val jextractUrl = "https://download.java.net/java/early_access/jextract/25/2/"
val jextractOutDir = layout.buildDirectory.dir("generated/main/java").get().asFile

plugins {
    application
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
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Exec>("cargoBuild") {
    description = "Builds the Rust library using Cargo"
    group = "rust"
    workingDir = rustPrjDir
    inputs.dir(rustSrcDir).withPropertyName("rustSrcDir")
    inputs.files(rustPrjDir.resolve("Cargo.toml")).withPropertyName("cargoToml")
    outputs.dir(rustTgtDir).withPropertyName("rustTargetDir")
    commandLine = listOf(cargo.absolutePath, "build", "--release")
}

tasks.register<Exec>("cargoClean") {
    description = "Cleans the Rust build"
    group = "rust"
    workingDir = rustPrjDir
    commandLine = listOf(cargo.absolutePath, "clean")
}

tasks.register<Copy>("downloadJextract") {
    description = "Downloads the jextract binaries"
    group = "jextract"
    val url = when {
        os.isMacOsX  && arch.isArm64 -> "$jextractUrl/openjdk-25-jextract+2-4_macos-aarch64_bin.tar.gz"
        os.isMacOsX  && arch.isAmd64 -> "$jextractUrl/openjdk-25-jextract+2-4_macos-x64_bin.tar.gz"
        os.isLinux   && arch.isArm64 -> "$jextractUrl/openjdk-25-jextract+2-4_linux-aarch64_bin.tar.gz"
        os.isLinux   && arch.isAmd64 -> "$jextractUrl/openjdk-25-jextract+2-4_linux-x64_bin.tar.gz"
        os.isWindows && arch.isAmd64 -> "$jextractUrl/openjdk-25-jextract+2-4_windows-x64_bin.tar.gz"
        else -> throw Error("Unsupported OS: $os, ARCH: $arch")
    }
    val tgz = layout.buildDirectory.file("jextract.tar.gz")
    tgz.get().asFile.apply {
        if (!exists()) {
            parentFile.mkdirs()
            uri(url).toURL().openStream().use { input ->
                outputStream().use { out -> input.copyTo(out) }
            }
        }
    }
    from(tarTree(resources.gzip(tgz.get())))
    into(layout.buildDirectory.dir("jextract"))
}

tasks.register<Exec>("jextract") {
    dependsOn("downloadJextract", "cargoBuild")
    group = "jextract"
    description = "Generates Java bindings from C header using jextract"

    inputs.files(cbindhFile).withPropertyName("cbindHeaderFile")
    outputs.dir(jextractOutDir).withPropertyName("jextractOutputDir")

    val jextract = layout.buildDirectory.dir("jextract/jextract-25/bin/jextract")
        .get().asFile.absolutePath + if (os.isWindows) ".bat" else ""

    commandLine(jextract,
        cbindhFile.absolutePath,
        "--output", jextractOutDir.absolutePath,
        "-t", "com.mammb.code.canvas.lib",
        "-l", "lib"
    )
}

tasks.named("compileJava") {
    dependsOn("jextract")
}

tasks.named<JavaExec>("run") {
    val rustLibDir = rustTgtDir.resolve("release")
    if (os.isMacOsX) {
        environment("DYLD_LIBRARY_PATH", rustLibDir)
    } else if (os.isWindows) {
        environment("PATH", rustLibDir)
    } else {
        environment("LD_LIBRARY_PATH", rustLibDir)
    }
}
