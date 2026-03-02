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

import javafx.scene.Cursor;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

/**
 * The heap chart.
 * @author Naotsugu Kobayashi
 */
public class HeapChart extends AreaChart<Number, Number> {

    private ZoneId zoneId = ZoneId.of("UTC");

    public HeapChart(GcLog gcLog) {

        super(new NumberAxis(gcLog.timeMin(), gcLog.timeMax(), niceTickUnit(gcLog.timeMax() - gcLog.timeMin())),
              new NumberAxis());

        var xAxis = (NumberAxis) getXAxis();
        var yAxis = (NumberAxis) getYAxis();

        xAxis.setTickLabelFormatter(new DateLabelFormatter(zoneId));
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

        var sizeSeries = new XYChart.Series<Number, Number>();
        sizeSeries.setName("Heap size");
        gcLog.acceptSize((t, v) -> sizeSeries.getData().add(new XYChart.Data<>(t, v)));

        var usedSeries = new XYChart.Series<Number, Number>();
        usedSeries.setName("Used heap");
        gcLog.acceptUsed((t, v) -> usedSeries.getData().add(new XYChart.Data<>(t, v)));

        setCreateSymbols(false);
        getData().add(sizeSeries);
        getData().add(usedSeries);

        setupMouseHandlers(xAxis);
    }


    private void setupMouseHandlers(NumberAxis xAxis) {

        // zoom
        setOnScroll(this::handleScroll);

        // pan by key
        setOnKeyPressed(this::handleKeyPressed);

        // panning support
        final double[] pressX = { 0 };
        final double[] pressLower = { 0 };
        final double[] pressUpper = { 0 };
        setOnMousePressed(e -> {
            if (e.getClickCount() == 2) {
                openRangeDialog(xAxis);
            } else if (e.isPrimaryButtonDown()) {
                pressX[0] = e.getX();
                pressLower[0] = xAxis.getLowerBound();
                pressUpper[0] = xAxis.getUpperBound();
                setCursor(Cursor.MOVE);
            }
        });
        setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                double dragX = e.getX();
                double valueDiff = xAxis.getValueForDisplay(pressX[0]).doubleValue() - xAxis.getValueForDisplay(dragX).doubleValue();
                xAxis.setLowerBound(pressLower[0] + valueDiff);
                xAxis.setUpperBound(pressUpper[0] + valueDiff);
            }
        });
        setOnMouseReleased(e -> setCursor(Cursor.DEFAULT));
    }


    private void handleScroll(ScrollEvent e) {
        if (e.getEventType() != ScrollEvent.SCROLL ||
            e.getDeltaY() == 0 ||
            !e.isShortcutDown()) {
            return;
        }

        var xAxis = (NumberAxis) getXAxis();
        double lowerBound = xAxis.getLowerBound();
        double upperBound = xAxis.getUpperBound();

        // the value on the axis corresponding to the mouse position
        double mouseValue = xAxis.getValueForDisplay(e.getX()).doubleValue();
        double zoomFactor = (e.getDeltaY() > 0) ? 0.9 : 1.1;

        double newLowerBound = mouseValue - (mouseValue - lowerBound) * zoomFactor;
        double newUpperBound = mouseValue + (upperBound - mouseValue) * zoomFactor;

        // prevent zooming in too much
        if (newUpperBound - newLowerBound < 1000) {
            return;
        }

        xAxis.setLowerBound(newLowerBound);
        xAxis.setUpperBound(newUpperBound);

        e.consume();
    }


    private void handleKeyPressed(KeyEvent e) {
        var xAxis = (NumberAxis) getXAxis();
        double range = xAxis.getUpperBound() - xAxis.getLowerBound();
        double shift = range * 0.1;

        switch (e.getCode()) {
            case LEFT:
                xAxis.setLowerBound(xAxis.getLowerBound() - shift);
                xAxis.setUpperBound(xAxis.getUpperBound() - shift);
                e.consume();
                break;
            case RIGHT:
                xAxis.setLowerBound(xAxis.getLowerBound() + shift);
                xAxis.setUpperBound(xAxis.getUpperBound() + shift);
                e.consume();
                break;
            default:
                // do nothing
        }
    }


    private void openRangeDialog(NumberAxis xAxis) {
        var dialog = new SettingDialog(
            LocalDateTime.ofInstant(Instant.ofEpochMilli((long) xAxis.getLowerBound()), zoneId),
            LocalDateTime.ofInstant(Instant.ofEpochMilli((long) xAxis.getUpperBound()), zoneId),
            zoneId);
        dialog.showAndWait().ifPresent(setting -> {
            zoneId = setting.zoneId();
            xAxis.setLowerBound(setting.from().atZone(zoneId).toInstant().toEpochMilli());
            xAxis.setUpperBound(setting.to().atZone(zoneId).toInstant().toEpochMilli());
            xAxis.setTickLabelFormatter(new DateLabelFormatter(zoneId));
        });
    }


    private static double niceTickUnit(double range) {
        final List<Double> ticks = List.of(
                1000d, 2000d, 5000d,
                10000d, 15000d, 30000d,
                60000d, 120000d, 300000d, 600000d,
                900000d, 1800000d, 3600000d);
        double raw = range / 10;
        return ticks.stream()
                .min(Comparator.comparingDouble(d -> Math.abs(d - raw)))
                .orElse(raw);
    }

}
