package com.mammb.code.gclog;

import javafx.util.StringConverter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateLabelFormatter extends StringConverter<Number> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd\nHH:mm:ss");

    @Override
    public String toString(Number number) {
        return Instant.ofEpochMilli(number.longValue())
                .atZone(ZoneId.of("UTC"))
                .format(formatter);
    }

    @Override
    public Number fromString(String s) {
        return null;
    }
}
