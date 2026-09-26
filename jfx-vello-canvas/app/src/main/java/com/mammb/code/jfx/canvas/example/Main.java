package com.mammb.code.jfx.canvas.example;

import com.mammb.code.canvas.lib.lib_h;

public class Main {

    //        // Must run first: this loads the cdylib bundled at the jar root
    //        // before the jextract-generated class (mylib_h) is initialized.
    //        // "mylib" here is the base name -> resolves to libmylib.so /
    //        // mylib.dll / libmylib.dylib depending on the OS.
    //        NativeLibraryLoader.loadFromClasspathRoot("mylib");
    static void main(String[] args) {
        int x = 15;
        int y = 27;
        int result = lib_h.add(x, y);
        System.out.println(x + " + " + y + " = " + result);
    }
}
