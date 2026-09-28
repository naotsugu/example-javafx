package com.mammb.code.jfx.canvas.example;

import com.mammb.code.canvas.lib.lib_h;
import com.mammb.code.jfx.canvas.NativeLibraryLoader;

public class Main {

    static void main(String[] args) {
        NativeLibraryLoader.loadByName("lib");
        int x = 15;
        int y = 27;
        int result = lib_h.add(x, y);
        System.out.println(x + " + " + y + " = " + result);
    }
}
