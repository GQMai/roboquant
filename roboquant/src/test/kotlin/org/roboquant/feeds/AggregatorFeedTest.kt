package org.roboquant.feeds

import org.roboquant.common.Timeframe
import org.roboquant.common.millis
import org.roboquant.common.minutes
import org.roboquant.common.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregatorFeedTest {

    @Test
    fun basic() {
        val feed = RandomWalkFeed.lastDays(1)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 15.minutes)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertTrue((items2[1].time.epochSecond - items2[0].time.epochSecond) >= (15*60))
        assertEquals(feed.timeframe, aggFeed.timeframe)

        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun basic2() {
        // 5-seconds window with 1 millis resolution
        val timeline = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z").toTimeline(1.millis)
        val feed = RandomWalkFeed(timeline, generateBars = false)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 1.seconds)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertEquals(4, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        assertEquals(feed.timeframe, aggFeed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun combined() {
        // 5-seconds window with 1 millis resolution
        val timeline = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z").toTimeline(1.millis)
        val rw = RandomWalkFeed(timeline, generateBars = false)
        val items1 = rw.toList()

        val aggFeed1 = AggregatorFeed(rw, 1.seconds)
        assertEquals(4, aggFeed1.toList().size)

        val aggFeed2 = AggregatorFeed(rw, 2.seconds)
        assertEquals(2, aggFeed2.toList().size)

        val feed = CombinedFeed(aggFeed1, aggFeed2)
        val items2 = feed.toList()
        assertEquals(6, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        assertEquals(feed.timeframe, feed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }


}