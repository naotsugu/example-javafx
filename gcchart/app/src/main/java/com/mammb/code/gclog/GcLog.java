package com.mammb.code.gclog;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GcLog {

    private List<HeapAggregation.DataPoint> sizeSeries = new ArrayList<>();
    private List<HeapAggregation.DataPoint> usedSeries = new ArrayList<>();

    public GcLog() {
    }

    public void analyze(Path path) {
        try {
            var gcToolKit = new GCToolKit();
            gcToolKit.loadAggregation(new HeapAggregation());

            var logFile = new SingleGCLogFile(path);
            gcToolKit.analyze(logFile)
                     .getAggregation(HeapAggregation.class)
                     .ifPresent(a -> {
                sizeSeries.addAll(a.sizeSeries());
                usedSeries.addAll(a.usedSeries());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptSize(BiConsumer<Number, Number> consumer) {
        sizeSeries.forEach(p -> consumer.accept(p.dateTime(), p.value()));
    }

    public void acceptUsed(BiConsumer<Number, Number> consumer) {
        usedSeries.forEach(p -> consumer.accept(p.dateTime(), p.value()));
    }

}
