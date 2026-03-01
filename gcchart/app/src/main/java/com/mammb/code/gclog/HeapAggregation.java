package com.mammb.code.gclog;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

@Collates(HeapSubscriber.class)
public class HeapAggregation extends Aggregation {

    private final ConcurrentLinkedQueue<DataPoint> sizeAggregations = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<DataPoint> usedAggregations = new ConcurrentLinkedQueue<>();


    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return sizeAggregations.isEmpty() && usedAggregations.isEmpty();
    }

    void addSize(double dateTime, long size) {
        sizeAggregations.add(new DataPoint(dateTime, size));
    }

    void addUsed(double dateTime, long occupancy) {
        usedAggregations.add(new DataPoint(dateTime, occupancy));
    }

    Collection<DataPoint> sizeSeries() {
        return Collections.unmodifiableCollection(sizeAggregations);
    }

    Collection<DataPoint> usedSeries() {
        return Collections.unmodifiableCollection(usedAggregations);
    }

    record DataPoint(double dateTime, long value) { }

}
