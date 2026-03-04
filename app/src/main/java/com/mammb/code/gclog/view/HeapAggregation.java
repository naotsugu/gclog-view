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

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The HeapAggregation.
 * @author Naotsugu Kobayashi
 */
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
