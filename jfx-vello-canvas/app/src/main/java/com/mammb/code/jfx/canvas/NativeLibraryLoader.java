package com.mammb.code.jfx.canvas;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeLibraryLoader {

    private NativeLibraryLoader() {
    }

    /**
     * @param name the name ("mylib" for libmylib.so / mylib.dll / libmylib.dylib)
     */
    public static void loadByName(String name) {
        try {
            String mappedName = System.mapLibraryName(name);
            URI uri = getUrl(mappedName).toURI();
            Path libraryPath = resolveOrExtract(uri, mappedName);
            System.load(libraryPath.toAbsolutePath().toString());
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