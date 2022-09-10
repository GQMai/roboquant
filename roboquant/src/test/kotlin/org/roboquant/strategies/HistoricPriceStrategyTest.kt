/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.strategies

import org.roboquant.TestData
import org.roboquant.common.Asset
import kotlin.test.*

internal class HistoricPriceStrategyTest {

    private class MySubClass : HistoricPriceStrategy(10) {
        var called = false

        override fun generate(asset: Asset, data: DoubleArray): Signal? {
            called = true
            return null
        }
    }

    @Test
    fun test() {
        val c = MySubClass()
        val event = TestData.event()
        val signals = c.generate(event)
        assertTrue(signals.isEmpty())
        assertFalse(c.called)

        repeat(10) { c.generate(event) }
        assertTrue(c.called)
    }

}