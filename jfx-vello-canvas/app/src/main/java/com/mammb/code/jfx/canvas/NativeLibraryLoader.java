package com.mammb.code.jfx.canvas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public final class NativeLibraryLoader {

    /** The logger. */
    private static final System.Logger log = System.getLogger(NativeLibraryLoader.class.getName());

    private static final Arena ARENA = Arena.global();

    private NativeLibraryLoader() {
    }

    public static void loadByName(String name) {

        // `mylib` for libmylib.so / mylib.dll / libmylib.dylib
        String mappedName = System.mapLibraryName(name);

        String os = System.getProperty("os.name").toLowerCase();
        String env;
        if (os.contains("win")) {
            env = System.getenv("PATH");
        } else if (os.contains("mac") || os.contains("darwin")) {
            env = System.getenv("DYLD_LIBRARY_PATH");
        } else {
            env = System.getenv("LD_LIBRARY_PATH");
        }
        if (env != null && Arrays.stream(env.split(File.pathSeparator))
                .anyMatch(path -> path.contains(mappedName))) {
            return;
        }

        try {
            URI uri = getUrl(mappedName).toURI();
            Path libraryPath = resolveOrExtract(uri, mappedName);
            log.log(System.Logger.Level.INFO, "libraryPath: {0}", libraryPath.toAbsolutePath().toString());
            SymbolLookup.libraryLookup(libraryPath, ARENA);
            //System.load(libraryPath.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new RuntimeException("failed to load native library: " + name, e);
        }
    }

    private static URL getUrl(String mappedName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = NativeLibraryLoader.class.getClassLoader();
        }
        return classLoader.getResource(mappedName);
    }

    private static Path resolveOrExtract(URI uri, String mappedName) throws IOException {
        if ("file".equals(uri.getScheme())) {
            // Unpacked case (e.g. IDE run with an exploded output directory):
            return Path.of(uri);
        } else {
            // "jar" (packaged inside a jar) or "jrt" (packaged inside a jlink'ed runtime image module)
            return extractToTempFile(uri.toURL(), mappedName);
        }
    }

    private static Path extractToTempFile(URL url, String mappedName) throws IOException {
        try (InputStream in = url.openStream()) {
            Path tempDir = Files.createTempDirectory("native-lib-");
            tempDir.toFile().deleteOnExit();
            Path tempFile = tempDir.resolve(mappedName);
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
    }

}