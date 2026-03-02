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

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.time.LocalDateTime;

/**
 * The chart setting dialog.
 * @author Naotsugu Kobayashi
 */
public class SettingDialog extends Dialog<SettingDialog.Range> {

    /**
     * The range.
     * @param from the form date time
     * @param to the to date time
     */
    public record Range(LocalDateTime from, LocalDateTime to) { }

    private final DateTimePicker lowerPicker;
    private final DateTimePicker upperPicker;

    public SettingDialog(LocalDateTime lower, LocalDateTime upper) {

        setTitle("Chart Setting");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        lowerPicker = new DateTimePicker();
        lowerPicker.setDateTimeValue(lower);
        upperPicker = new DateTimePicker();
        upperPicker.setDateTimeValue(upper);

        grid.add(new Label("From:"), 0, 0);
        grid.add(lowerPicker, 1, 0);
        grid.add(new Label("To:"), 0, 1);
        grid.add(upperPicker, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDateTime from = lowerPicker.getDateTimeValue();
                LocalDateTime to = upperPicker.getDateTimeValue();
                if (from != null && to != null && from.isBefore(to)) {
                    return new Range(from, to);
                }
            }
            return null;
        });
    }
}
