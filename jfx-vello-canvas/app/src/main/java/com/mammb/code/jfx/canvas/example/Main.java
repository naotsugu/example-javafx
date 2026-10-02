package com.mammb.code.jfx.canvas.example;

import com.mammb.code.jfx.canvas.NativeLibraryLoader;

public class Main {

    static void main(String[] args) {
        NativeLibraryLoader.loadByName("lib");
        App.launch(App.class, args);
    }
}
