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

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;

import java.util.ArrayList;
import java.util.List;

public class PtJVMEventChannel implements JVMEventChannel {

    private final List<JVMEventChannelListener> listeners = new ArrayList<>();

    @Override
    public void registerListener(JVMEventChannelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void publish(ChannelName channel, JVMEvent event) {
        listeners.forEach(listener -> listener.receive(event));
    }

    @Override
    public void close() {
        listeners.clear();
    }

}
