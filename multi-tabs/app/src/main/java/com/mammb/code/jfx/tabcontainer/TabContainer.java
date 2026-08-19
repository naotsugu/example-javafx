package com.mammb.code.jfx.tabcontainer;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface TabContainer {

    interface Require<T> { void accept(T t); }
    interface Argument<T> { T argument(); }
    interface Aware<T> { T aware(); }

    interface RequireContent extends Require<ContentPane> { }
    interface RequireStage extends Argument<Pane>, Require<Stage> { }
    interface RequestContent extends Argument<List<Path>>, Aware<TabContainer> { }

    interface Handler<T> { void handle(T event); }


    Optional<ContentPane> find(Predicate<ContentPane> predicate);

    void select(ContentPane contentPane);

}
