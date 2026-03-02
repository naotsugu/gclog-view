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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The chart setting dialog.
 * @author Naotsugu Kobayashi
 */
public class SettingDialog extends Dialog<SettingDialog.Setting> {

    /**
     * The setting.
     * @param from the form date time
     * @param to the to date time
     * @param zoneId the zone id
     */
    public record Setting(LocalDateTime from, LocalDateTime to, ZoneId zoneId) { }

    private final DateTimePicker lowerPicker;
    private final DateTimePicker upperPicker;
    private final ComboBox<ZoneId> zoneIdComboBox;

    public SettingDialog(LocalDateTime lower, LocalDateTime upper, ZoneId zoneId) {

        setTitle("Chart Setting");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        lowerPicker = new DateTimePicker();
        lowerPicker.setDateTimeValue(lower);
        upperPicker = new DateTimePicker();
        upperPicker.setDateTimeValue(upper);

        zoneIdComboBox = new ComboBox<>();
        zoneIdComboBox.getItems().addAll(ZoneId.of("UTC"), ZoneId.systemDefault());
        zoneIdComboBox.setValue(zoneId);
        zoneIdComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ZoneId zone) {
                return (zone == null) ? "" : zone.getId();
            }
            @Override
            public ZoneId fromString(String string) {
                return (string == null) ? null : ZoneId.of(string);
            }
        });
        zoneIdComboBox.valueProperty().addListener((observable, oldZone, newZone) -> {
            if (oldZone != null && newZone != null) {
                lowerPicker.setDateTimeValue(
                    lowerPicker.getDateTimeValue().atZone(oldZone).withZoneSameInstant(newZone).toLocalDateTime());
                upperPicker.setDateTimeValue(
                    upperPicker.getDateTimeValue().atZone(oldZone).withZoneSameInstant(newZone).toLocalDateTime());
            }
        });


        grid.add(new Label("From:"), 0, 0);
        grid.add(lowerPicker, 1, 0);
        grid.add(new Label("To:"), 0, 1);
        grid.add(upperPicker, 1, 1);
        grid.add(new Label("Zone:"), 0, 2);
        grid.add(zoneIdComboBox, 1, 2);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDateTime from = lowerPicker.getDateTimeValue();
                LocalDateTime to = upperPicker.getDateTimeValue();
                if (from != null && to != null && from.isBefore(to)) {
                    return new Setting(from, to, zoneIdComboBox.getValue());
                }
            }
            return null;
        });
    }
}
