/*
 * Copyright 2026- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mammb.code.gclog.view;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.generational.GenerationalGCPauseEvent;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * The HeapSubscriber.
 * @author Naotsugu Kobayashi
 */
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
