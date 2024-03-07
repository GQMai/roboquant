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

package org.roboquant

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.assets
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.io.File
import java.time.Instant

/**
 * Test data used in unit tests
 */
internal object TestData {

    fun usStock() = Asset("XYZ")

    fun internalAccount(): InternalAccount {
        val asset1 = Asset("AAA")
        val asset2 = Asset("AAB")
        val account = InternalAccount(Currency.USD)
        account.cash.deposit(100_000.USD)
        account.setPosition(Position(asset1, Size(100), 10.0))
        account.setPosition(Position(asset2, Size(100), 10.0))

        val order = MarketOrder(asset1, 100)
        // val state = MutableOrderState(order, OrderStatus.COMPLETED, Instant.now(), Instant.now())
        account.initializeOrders(listOf(order))
        account.updateOrder(order, Instant.now(), OrderStatus.COMPLETED)
        return account
    }

    fun usAccount(): Account {
        val asset1 = Asset("AAA")
        val asset2 = Asset("AAB")
        val account = InternalAccount(Currency.USD)
        account.cash.deposit(100_000.USD)
        account.setPosition(Position(asset1, Size(100), 10.0))
        account.setPosition(Position(asset2, Size(100), 10.0))
        account.buyingPower = 100_000.USD

        val order = MarketOrder(asset1, 100)
        account.initializeOrders(listOf(order))
        return account.toAccount()
    }

    fun euStock() = Asset("ABC", currencyCode = "EUR", exchangeCode = "AEB")

    fun feed(): HistoricFeed {
        return HistoricTestFeed(90..110, 110 downTo 80, 80..125, priceBar = true, asset = usStock())
    }

    fun dataDir(): String {
        if (File("./roboquant/src/test/resources/data/").isDirectory)
            return "./roboquant/src/test/resources/data/"
        else if (File("../roboquant/src/test/resources/data/").isDirectory)
            return "../roboquant/src/test/resources/data/"
        throw ConfigurationException("cannot find data directory for testing")
    }

    fun euMarketOrder() = MarketOrder(euStock(), 10)

    fun usMarketOrder() = MarketOrder(usStock(), 10)

    fun priceAction(asset: Asset = usStock()) = TradePrice(asset, 10.0)

    private fun priceBar(asset: Asset = usStock()) = PriceBar(asset, 10.0, 11.0, 9.0, 10.0, 1000.0)

    fun time(): Instant = Instant.parse("2020-01-03T12:00:00Z")

    fun event(time: Instant = time()) = Event(time, listOf(priceAction()))

    fun event2(time: Instant = time()) = Event(time, listOf(priceBar()))

    fun metricInput(time: Instant = time()): Pair<Account, Event> {
        val account = usAccount()
        val asset1 = account.positions.assets.first()
        val moment = Event(time, listOf(TradePrice(asset1, 11.0)))
        return Pair(account, moment)
    }

    fun getMetrics(): Map<String, Double> {
        return mapOf("key1" to 12.0, "key2" to 13.0)
    }

    fun events(n: Int = 100, asset: Asset = usStock()): List<Event> {
        val start = time()
        val result = mutableListOf<Event>()
        repeat(n) {
            val action = TradePrice(asset, it + 100.0)
            val event = Event(start + it.days, listOf(action))
            result.add(event)
        }
        return result
    }

    val feed = RandomWalkFeed.lastYears(1, 2)

}
