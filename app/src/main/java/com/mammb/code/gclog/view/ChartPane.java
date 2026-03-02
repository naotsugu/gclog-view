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

import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The ChatPane.
 * @author Naotsugu Kobayashi
 */
public class ChartPane extends StackPane {

    /** The logger. */
    private static final System.Logger log = System.getLogger(ChartPane.class.getName());

    public ChartPane() {
        getChildren().add(buildInitLabel());
        setOnDragDetected(this::handleDragDetect);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnScroll(this::handleScroll);
        setOnKeyPressed(this::handleKeyPressed);
        setFocusTraversable(true);
    }


    private void handleDragDetect(MouseEvent e) { }

    private void handleDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard board = e.getDragboard();
        if (board.hasFiles()) {
            open(board.getFiles());
            e.consume();
        }
    }

    private void handleScroll(ScrollEvent e) {

        if (e.getEventType() != ScrollEvent.SCROLL ||
            e.getDeltaY() == 0 ||
            !e.isShortcutDown()) {
            return;
        }

        areaChart().ifPresent(chart -> {

            var xAxis = (NumberAxis) chart.getXAxis();
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
        });
    }

    private void handleKeyPressed(KeyEvent e) {

        if (new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN).match(e)) {
            openWithChooser();
            return;
        }

        areaChart().ifPresent(chart -> {
            var xAxis = (NumberAxis) chart.getXAxis();
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
        });
    }

    private void open(List<File> files) {
        var task = buildAnalyzeTask(files);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(task.progressProperty());
        getChildren().clear();
        getChildren().add(progressBar);

        task.setOnSucceeded(event -> {
            getChildren().clear();
            getChildren().add(task.getValue());
            requestFocus();
        });
        task.setOnFailed(event -> {
            getChildren().clear();
            getChildren().add(new Label("Error processing file."));
            log.log(System.Logger.Level.WARNING, "Error processing file.", task.getException());
        });
        new Thread(task).start();
    }

    private Task<AreaChart<Number, Number>> buildAnalyzeTask(List<File> files) {
        return new Task<>() {
            @Override
            protected AreaChart<Number, Number> call() {
            List<Path> paths = files.stream().map(File::toPath)
                    .filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .toList();
            var gcLog = new GcLog();
            if (paths.size() == 1) {
                updateProgress(-1, 1);
                gcLog.analyze(paths.getFirst());
            } else {
                for (int i = 0; i < paths.size(); i++) {
                    gcLog.analyze(paths.get(i));
                    updateProgress(i + 1, paths.size());
                }
            }
            return buildAreaChart(gcLog);
            }
        };
    }

    private AreaChart<Number, Number> buildAreaChart(GcLog gcLog) {

        var sizeSeries = new XYChart.Series<Number, Number>();
        sizeSeries.setName("Heap size");
        gcLog.acceptSize((t, v) -> sizeSeries.getData().add(new XYChart.Data<>(t, v)));

        var usedSeries = new XYChart.Series<Number, Number>();
        usedSeries.setName("Used heap");
        gcLog.acceptUsed((t, v) -> usedSeries.getData().add(new XYChart.Data<>(t, v)));

        var xAxis = new NumberAxis(gcLog.timeMin(), gcLog.timeMax(),
            niceTickUnit(gcLog.timeMax() - gcLog.timeMin()));
        xAxis.setTickLabelFormatter(new DateLabelFormatter());
        xAxis.setAnimated(false);

        var yAxis = new NumberAxis();
        yAxis.setAnimated(false);

        var areaChart =  new AreaChart<>(xAxis, yAxis);
        areaChart.setCreateSymbols(false);
        areaChart.getData().add(sizeSeries);
        areaChart.getData().add(usedSeries);

        // panning support
        final double[] pressX = { 0 };
        final double[] pressLower = { 0 };
        final double[] pressUpper = { 0 };
        areaChart.setOnMousePressed(e -> {
            if (e.getClickCount() == 2) {
                openRangeDialog(xAxis);
            } else if (e.isPrimaryButtonDown()) {
                pressX[0] = e.getX();
                pressLower[0] = xAxis.getLowerBound();
                pressUpper[0] = xAxis.getUpperBound();
                areaChart.setCursor(Cursor.MOVE);
            }
        });
        areaChart.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                double dragX = e.getX();
                double valueDiff = xAxis.getValueForDisplay(pressX[0]).doubleValue() - xAxis.getValueForDisplay(dragX).doubleValue();
                xAxis.setLowerBound(pressLower[0] + valueDiff);
                xAxis.setUpperBound(pressUpper[0] + valueDiff);
            }
        });
        areaChart.setOnMouseReleased(e -> areaChart.setCursor(Cursor.DEFAULT));

        return areaChart;
    }

    private void openRangeDialog(NumberAxis xAxis) {
        var dialog = new ChartSettingDialog(
            LocalDateTime.ofInstant(Instant.ofEpochMilli((long) xAxis.getLowerBound()), ZoneId.systemDefault()),
            LocalDateTime.ofInstant(Instant.ofEpochMilli((long) xAxis.getUpperBound()), ZoneId.systemDefault()));
        dialog.showAndWait().ifPresent(range -> {
            xAxis.setLowerBound(range.from().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            xAxis.setUpperBound(range.to().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        });
    }

    private void openWithChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        fc.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file == null) return;
        open(List.of(file));
    }

    private Optional<AreaChart> areaChart() {
        return getChildren().stream()
            .filter(AreaChart.class::isInstance)
            .map(AreaChart.class::cast)
            .findFirst();
    }

    private double niceTickUnit(double range) {
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

    private Label buildInitLabel() {
        String shortcut = System.getProperty("os.name").toLowerCase().contains("mac") ? "⌘" : "Ctrl";
        var label = new Label("Drop gc log file here, or open with " + shortcut + " + o");
        label.setFont(Font.font("System", FontWeight.BOLD, 20));
        return label;
    }

}
