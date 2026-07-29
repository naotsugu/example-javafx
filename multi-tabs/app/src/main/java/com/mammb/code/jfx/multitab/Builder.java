package com.mammb.code.jfx.multitab;

import com.mammb.code.jfx.multitab.internal.BranchNode;
import com.mammb.code.jfx.multitab.internal.Context;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class Builder {
    private Supplier<? extends ContentPane> supplier;
    private Function<String, ? extends ContentPane> supplierString = _ -> supplier.get();
    private Function<Path, ? extends ContentPane> supplierPath = _ -> supplier.get();
    private Function<BranchNode, Stage> newStage;

    public static Builder of(Supplier<? extends ContentPane> supplier) {
        var tc = new Builder();
        tc.supplier = supplier;
        return tc;
    }

    public Builder fromString(Function<String, ? extends ContentPane> supplierString) {
        this.supplierString = supplierString;
        return this;
    }

    public Builder fromPath(Function<Path, ? extends ContentPane> supplierPath) {
        this.supplierPath = supplierPath;
        return this;
    }

    public Builder toStage(Function<BranchNode, Stage> newStage) {
        this.newStage = newStage;
        return this;
    }

    public Pane build(Stage stage) {
        Context ctx = new Context(stage,
            supplier,
            supplierString,
            supplierPath,
            newStage);
        return new BranchNode(ctx, ctx.create());
    }

}
