package com.mammb.code.jfx.tabcontainer.internal;

import com.mammb.code.jfx.tabcontainer.ContentPane;
import com.mammb.code.jfx.tabcontainer.TabContainer;
import javafx.geometry.Side;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class TabContainerImpl implements TabContainer {

    private final Context ctx;

    public TabContainerImpl(Context ctx) {
        this.ctx = Objects.requireNonNull(ctx);
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
