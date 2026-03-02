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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The ChatPane.
 * @author Naotsugu Kobayashi
 */
public class ChartPane extends StackPane {

    /** The logger. */
    private static final System.Logger log = System.getLogger(ChartPane.class.getName());

    public ChartPane() {
        getChildren().add(buildInitLabel());
        setOnDragDetected(e -> { });
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnKeyPressed(this::handleKeyPressed);
        setFocusTraversable(true);
    }

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

    private void handleKeyPressed(KeyEvent e) {
        if (new KeyCharacterCombination("o", KeyCombination.SHORTCUT_DOWN).match(e)) {
            openWithChooser();
            e.consume();
        }
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
            task.getValue().requestFocus();
        });
        task.setOnFailed(event -> {
            getChildren().clear();
            getChildren().add(new Label("Error processing file."));
            log.log(System.Logger.Level.WARNING, "Error processing file.", task.getException());
        });
        new Thread(task).start();
    }

    private Task<HeapChart> buildAnalyzeTask(List<File> files) {
        return new Task<>() {
            @Override
            protected HeapChart call() {
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
            return new HeapChart(gcLog);
            }
        };
    }

    private void openWithChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file...");
        fc.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
        File file = fc.showOpenDialog(getScene().getWindow());
        if (file == null) return;
        open(List.of(file));
    }

    private Label buildInitLabel() {
        String shortcut = System.getProperty("os.name").toLowerCase().contains("mac") ? "⌘" : "Ctrl";
        var label = new Label("Drop gc log file here, or open with " + shortcut + " + o");
        label.setFont(Font.font("System", FontWeight.BOLD, 20));
        return label;
    }

}
