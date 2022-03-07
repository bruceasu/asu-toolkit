/*
 * Copyright (c) 2022 Suk Honzeon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.asu.util.timetrace;


import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Default tracer driver
 */
@Slf4j
public class DefaultTracerDriver implements TracerDriver {

    private int value = 0;

    @Override
    public void addTrace(String name, long time, TimeUnit unit) {
        if (log.isTraceEnabled()) {
            log.trace("Trace: {} - {} ms.", name, TimeUnit.MILLISECONDS.convert(time, unit));
        }
    }

    @Override
    public void addCount(String name, int increment) {
        value += increment;

        if (log.isTraceEnabled()) {
            log.trace("Counter {}: {}", name, value);
        }
    }
}