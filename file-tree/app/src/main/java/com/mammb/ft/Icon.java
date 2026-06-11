package com.mammb.ft;

import javafx.scene.shape.SVGPath;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Icon {

    private static final Pattern SVG_PATH_PATTERN = Pattern.compile(" d=\"([^\"]*)\"");
    private static final String FOLDER_SVG = loadSvg("folder.svg");
    private static final String FILE_SVG = loadSvg("file.svg");

    public static SVGPath folder() {
        SVGPath svg = new SVGPath();
        svg.setContent(FOLDER_SVG);
        return svg;
    }

    public static SVGPath file() {
        SVGPath svg = new SVGPath();
        svg.setContent(FILE_SVG);
        return svg;
    }

    private static String loadSvg(String name) {
        try (var is = Icon.class.getResourceAsStream(name)) {
            String svg = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = SVG_PATH_PATTERN.matcher(svg);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                sb.append(matcher.group(1));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
