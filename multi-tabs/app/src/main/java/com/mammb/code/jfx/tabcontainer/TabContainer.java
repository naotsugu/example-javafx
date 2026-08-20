package com.mammb.code.jfx.tabcontainer;

import com.mammb.code.jfx.tabcontainer.internal.Context;
import com.mammb.code.jfx.tabcontainer.internal.Handlers;
import com.mammb.code.jfx.tabcontainer.internal.Resume;
import com.mammb.code.jfx.tabcontainer.internal.TabContainerImpl;
import javafx.geometry.Side;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.swing.text.AbstractDocument;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TabContainer {

    interface Require<T> { void accept(T t); }
    interface Argument<T> { T argument(); }
    interface ContainerAware<T> { T container(); }

    interface RequireContent extends Require<ContentPane> { }
    interface RequireStage extends Require<Stage> {
        Pane pane();
    }
    interface RequestContent extends Argument<Path>, ContainerAware<TabContainer> { }

    interface Handler<T> { void handle(T event); }

    static TabContainer of(
        Handler<RequireContent> requireContentHandler,
        Handler<RequestContent> requestContentHandler,
        Handler<RequireStage> requireStageHandler) {

        var handlers = new Handlers(requireContentHandler,
            requestContentHandler,
            requireStageHandler);

        return new TabContainerImpl(new Context(handlers));
    }

    Pane resume(Stage stage, Path path, Function<String, ? extends ContentPane> resumeToContent);

    void add(ContentPane contentPane);

    void add(Side side, ContentPane contentPane);

    Optional<ContentPane> find(Predicate<ContentPane> predicate);

    void select(ContentPane contentPane);

}
