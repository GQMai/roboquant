/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.metrics

import org.roboquant.TestData
import org.roboquant.common.months
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.journals.MetricsJournal
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.TestStrategy
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class ReturnsMetricTest {

    @Test
    fun basic() {
        val metric = ReturnsMetric()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isEmpty())
    }

    @Test
    fun basic2() {
        val metric = ReturnsMetric2(minSize = 250)
        val feed = RandomWalkFeed.lastYears(2)
        val j = MetricsJournal(metric)
        org.roboquant.run(feed, EMAStrategy(), j)
        assertContains(j.getMetricNames(), "returns.sharperatio")
    }

    @Test
    fun test2() {
        val feed = HistoricTestFeed(100..300)
        val strategy = TestStrategy()
        val metric = ReturnsMetric(period = 1.months)
        val logger = MetricsJournal(metric)
        org.roboquant.run(feed, strategy, logger)

        val sharpRatio = logger.getMetric("returns.sharperatio").last().value
        assertTrue(!sharpRatio.isNaN())

        val mean = logger.getMetric("returns.mean").last().value
        assertTrue(!mean.isNaN())

        val std = logger.getMetric("returns.std").last().value
        assertTrue(!std.isNaN())
    }

}
