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

import javafx.util.StringConverter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The DateLabelFormatter.
 * @author Naotsugu Kobayashi
 */
public class DateLabelFormatter extends StringConverter<Number> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd\nHH:mm:ss");

    @Override
    public String toString(Number number) {
        return Instant.ofEpochMilli(number.longValue())
                .atZone(ZoneId.of("UTC"))
                .format(formatter);
    }

    @Override
    public Number fromString(String s) {
        return null;
    }
}
