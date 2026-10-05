package com.mammb.code.jfx.canvas;

import com.mammb.code.canvas.lib.lib_h;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;

public final class NativeGlobal {

    private static final Cleaner cleaner = Cleaner.create();
    private static final NativeGlobal instance = new NativeGlobal();

    private NativeGlobal() {
        // init native library
        NativeLibraryLoader.loadByName("lib");
        // hook release to the native shared resources
        Runtime.getRuntime().addShutdownHook(new Thread(lib_h::release_shared_resources));
    }

    public static NativeGlobal instance() {
        return instance;
    }

    public Cleaner.Cleanable cleaner(Object obj, MemorySegment ctxSegment, Arena arena) {
        return cleaner.register(obj, new CleanerState(ctxSegment, arena));
    }

    record CleanerState(MemorySegment ctxSegment, Arena arena) implements Runnable {
        @Override
        public void run() {
            if (ctxSegment != null && !ctxSegment.equals(MemorySegment.NULL)) {
                lib_h.destroy_render_context(ctxSegment);
            }
            if (arena != null) arena.close();
        }
    }


}
