/*
 * Copyright © 2016 Victor.su<victor.su@gwtsz.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package me.asu.util.timetrace;


import java.util.concurrent.TimeUnit;

public class TimeTrace
{
    private final String name;
    private final TracerDriver driver;
    private final long startTimeNanos = System.nanoTime();

    /**
     * Create and start a timer
     *
     * @param name
     *         name of the event
     * @param driver
     *         driver
     */
    public TimeTrace(String name, TracerDriver driver)
    {
        this.name = name;
        this.driver = driver;
    }

    /**
     * Record the elapsed time
     */
    public void commit()
    {
        long elapsed = System.nanoTime() - startTimeNanos;
        driver.addTrace(name, elapsed, TimeUnit.NANOSECONDS);
    }
}
