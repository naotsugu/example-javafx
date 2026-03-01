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
    private long timeMin = 0;
    private long timeMax = 0;

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

            var stats1 = sizeSeries.stream().map(HeapAggregation.DataPoint::dateTime)
                    .mapToLong(Double::longValue).summaryStatistics();
            var stats2 = usedSeries.stream().map(HeapAggregation.DataPoint::dateTime)
                    .mapToLong(Double::longValue).summaryStatistics();
            timeMin = Math.min(stats1.getMin(), stats2.getMin());
            timeMax = Math.max(stats1.getMax(), stats2.getMax());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long timeMin() {
        return timeMin;
    }

    public long timeMax() {
        return timeMax;
    }

    public void acceptSize(BiConsumer<Number, Number> consumer) {
        sizeSeries.forEach(p -> consumer.accept(p.dateTime(), p.value()));
    }

    public void acceptUsed(BiConsumer<Number, Number> consumer) {
        usedSeries.forEach(p -> consumer.accept(p.dateTime(), p.value()));
    }

}
