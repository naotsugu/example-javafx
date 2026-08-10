/*
 * Copyright 2026- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.jfx.multitab.example;

import com.mammb.code.jfx.multitab.ContentPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.nio.file.Path;

public class LabelContent extends ContentPane {

    private final SimpleObjectProperty<String> shortNameProperty = new SimpleObjectProperty<>("");
    private final SimpleObjectProperty<String> fullNameProperty = new SimpleObjectProperty<>("");

    public LabelContent(Object arg) {
        switch (arg) {
            case Path path -> {
                shortNameProperty.set(path.getFileName().toString());
                fullNameProperty.set(path.toString());
            }
            case String string when !string.isBlank() -> {
                String[] strip = string.split(System.getProperty("path.separator", ";"), 2);
                shortNameProperty.set(strip[0]);
                if (strip.length > 1) {
                    fullNameProperty.set(strip[1]);
                }
            }
            case null, default -> shortNameProperty.set("Untitled");
        }
    }

    @Override
    public void focus() {

    }

    @Override
    public boolean canCloseQuiet() {
        return true;
    }

    @Override
    public boolean closeRequest() {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public String asString() {
        return shortNameProperty.get() + System.getProperty("path.separator", ";") + fullNameProperty.get();
    }

    @Override
    public ReadOnlyObjectProperty<String> shortNameProperty() {
        return shortNameProperty;
    }

    @Override
    public ReadOnlyObjectProperty<String> fullNameProperty() {
        return fullNameProperty;
    }

}
