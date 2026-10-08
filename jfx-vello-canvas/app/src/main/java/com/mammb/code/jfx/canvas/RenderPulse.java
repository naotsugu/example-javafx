package com.mammb.code.jfx.canvas;

import javafx.animation.AnimationTimer;

public class RenderPulse extends AnimationTimer {

    private volatile boolean dirty = false;

    private final Runnable render;

    /**
     * Constructor.
     * @param render the paint runnable
     */
    public RenderPulse(Runnable render) {
        this.render = render;
    }

    /**
     * Marks the animation as dirty, indicating that the paint action should
     * be executed during the next handle call in the animation cycle.
     */
    public void request() {
        dirty = true;
    }

    @Override
    public void handle(long now) {
        if (dirty) {
            render.run();
            dirty = false;
        }
    }

}

