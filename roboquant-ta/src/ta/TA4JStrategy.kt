package org.roboquant.ta

import org.roboquant.RunPhase
import org.roboquant.common.Asset
import org.roboquant.common.toUTC
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Rule
import org.ta4j.core.rules.BooleanRule

private fun noOp(series: BarSeries) = BooleanRule.FALSE

/**
 * Strategy that allows to use indicators and rules from the TA4J library to define a custom strategy.
 *
 * @property getBuyingRule function that returns a buying rule based on the provided BarSaries
 * @property getSellingRule function that returns a selling rule based on the provided BarSaries
 * @property maxBarCount maximum number of pricebars to track
 * @constructor Create new TA4J strategy
 */
class TA4JStrategy(
    var getBuyingRule: (BarSeries) -> Rule = ::noOp,
    var getSellingRule: (BarSeries) -> Rule = ::noOp,
    private val maxBarCount:Int = -1,
) : Strategy {

    private val data = mutableMapOf<Asset, Triple<Rule, Rule, BarSeries>>()

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val time = event.time.toUTC()
        for ((asset, price) in event.prices) {
            if (price is PriceBar) {
                val (buyingRule, sellingRule, series) = data.getOrPut(asset) {
                    val series = BaseBarSeriesBuilder().withName(asset.symbol).build()
                    if (maxBarCount >= 0) series.maximumBarCount = maxBarCount
                    val rule1 = getBuyingRule(series)
                    val rule2 = getSellingRule(series)
                    Triple(rule1, rule2, series)
                }

                series.addBar(time, price.open, price.high, price.low, price.close, price.volume)
                if (buyingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.BUY))
                if (sellingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.SELL))
            }
        }
        return result
    }

    override fun start(runPhase: RunPhase) {
        reset()
    }

    override fun reset() {
        super.reset()
        data.clear()
    }

}