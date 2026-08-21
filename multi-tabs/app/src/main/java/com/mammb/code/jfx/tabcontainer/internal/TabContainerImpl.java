package com.mammb.code.jfx.tabcontainer.internal;

import com.mammb.code.jfx.tabcontainer.ContainerHandle;
import com.mammb.code.jfx.tabcontainer.ContentPane;
import com.mammb.code.jfx.tabcontainer.TabContainer;
import javafx.geometry.Side;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TabContainerImpl implements TabContainer, ContainerHandle {

    private final Context ctx;

    public TabContainerImpl(Context ctx) {
        this.ctx = Objects.requireNonNull(ctx);
    }

    @Override
    public Pane resume(Stage stage, Path path, Function<String, ? extends ContentPane> resumeToContent) {

        var suspend = new Suspend(path);
        var suspendHandler = new SuspendHandler(suspend);
        suspendHandler.bind(stage);
        ctx.handlers().stageHandler(suspendHandler::bind);

        return new Resume(ctx, path).load(stage, resumeToContent);
    }

    @Override
    public Pane create(Stage stage) {
        stage.setWidth(600);
        stage.setHeight(400);
        return new BranchNode(ctx, ctx.handlers().requireContent());
    }

    @Override
    public void add(ContentPane contentPane) {
        ctx.currentTab().parent().add(contentPane);
    }

    @Override
    public void add(Side side, ContentPane contentPane) {
        ctx.currentTab().parent().add(contentPane, side);
    }

    @Override
    public Optional<ContentPane> find(Predicate<ContentPane> predicate) {
        return ctx.find(predicate);
    }

    @Override
    public void select(ContentPane contentPane) {
        ctx.allTabs().stream()
            .filter(tab -> Objects.equals(tab.content(), contentPane))
            .forEach(Tab::requestSelect);
    }

}
