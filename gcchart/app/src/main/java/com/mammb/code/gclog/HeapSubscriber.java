package com.mammb.code.gclog;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.time.DateTimeStamp;

@Aggregates({ EventSource.G1GC, EventSource.GENERATIONAL })
public class HeapSubscriber extends Aggregator<HeapAggregation> {

    public HeapSubscriber(HeapAggregation aggregation) {
        super(aggregation);
        register(GenerationalGCPauseEvent.class, this::extract);
        register(G1GCPauseEvent.class, this::extract);
    }

    private void extract(GCEvent event) {
        DateTimeStamp timeStamp = event.getDateTimeStamp();
        if (timeStamp == null || !timeStamp.hasDateStamp()) return;

        MemoryPoolSummary heep = switch (event) {
            case G1GCPauseEvent e -> e.getHeap();
            case GenerationalGCPauseEvent e -> e.getHeap();
            default -> null;
        };
        if (heep == null) return;
        aggregation().addSize(timeStamp.toEpochInMillis(), heep.getSizeBeforeCollection());
        aggregation().addUsed(timeStamp.toEpochInMillis(), heep.getOccupancyBeforeCollection());
        aggregation().addSize(timeStamp.toEpochInMillis(), heep.getSizeAfterCollection());
        aggregation().addUsed(timeStamp.toEpochInMillis(), heep.getOccupancyAfterCollection());
    }

}
