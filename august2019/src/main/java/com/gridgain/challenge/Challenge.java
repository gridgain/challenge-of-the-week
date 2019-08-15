/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * Commons Clause Restriction
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, "Sell" means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 *
 * For purposes of the clause above, the "Licensor" is Copyright 2019 GridGain Systems, Inc.,
 * the "License" is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

package com.gridgain.challenge;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import static com.gridgain.challenge.Configuration.getIgniteCfg;
import static com.gridgain.challenge.Configuration.getTradeCacheConfiguration;
import static com.gridgain.challenge.Configuration.getTraderCacheConfiguration;

public class Challenge {

    private static final int INITIATION_EVENT_ID = 1;
    private static final int EXECUTION_EVENT_ID = 2;

    //Please note that you cannot change main method
    public static void main(String[] args) {
        IgniteConfiguration serverNode1Cfg = getIgniteCfg(48500);
        serverNode1Cfg.setIgniteInstanceName("server-node-1");

        IgniteConfiguration serverNode2Cfg = getIgniteCfg(48501);
        serverNode2Cfg.setIgniteInstanceName("server-node-2");

        IgniteConfiguration clientNodeCfg = getIgniteCfg(48502);
        clientNodeCfg.setIgniteInstanceName("client-node");
        clientNodeCfg.setClientMode(true);

        try (Ignite serverNode1 = Ignition.start(serverNode1Cfg);
             Ignite serverNode2 = Ignition.start(serverNode2Cfg);
             Ignite clientNode = Ignition.start(clientNodeCfg)) {

            CacheConfiguration<UUID, Trader> traderCacheCfg = getTraderCacheConfiguration();
            traderCacheCfg.setCacheMode(CacheMode.PARTITIONED);
            IgniteCache<UUID, Trader> traderCache = clientNode.createCache(traderCacheCfg);

            CacheConfiguration<TradeKey, Trade> tradeCacheCfg = getTradeCacheConfiguration();
            tradeCacheCfg.setCacheMode(CacheMode.PARTITIONED);
            IgniteCache<TradeKey, Trade> tradeCache = clientNode.createCache(tradeCacheCfg);

            populateData();

            SqlFieldsQuery qry = new SqlFieldsQuery("select * from Trader tr inner join Trade t on tr.id = t.traderId");
            QueryCursor<?> cur = traderCache.query(qry);

            assert cur.getAll().size() == 100000;
        }
    }

    private static void populateData() {
        IgniteCache<UUID, Trader> traders = Ignition.ignite("client-node").cache("traders");
        IgniteCache<TradeKey, Trade> trades = Ignition.ignite("client-node").cache("trades");

        int incrementalTradeId = 0;
        //populate 1000 traders and 50 trades with 2 versions for each trader
        for (int i = 0; i < 1000; i++) {
            UUID traderId = UUID.randomUUID();
            Trader trader = new Trader();
            trader.name = "Trader #" + i;
            traders.put(traderId, trader);

            for (int j = 0; j < 50; j++) {
                Trade trade1 = new Trade();
                trade1.currencyPair = "USDEUR";
                trade1.amount = new BigDecimal(1000);
                trade1.traderId = traderId;
                trade1.lifecycleEventId = INITIATION_EVENT_ID;
                trades.put(new TradeKey(incrementalTradeId, 1), trade1);

                Trade trade2 = new Trade();
                trade2.currencyPair = "USDEUR";
                trade2.amount = new BigDecimal(1000);
                trade2.traderId = traderId;
                trade2.lifecycleEventId = EXECUTION_EVENT_ID;
                trades.put(new TradeKey(incrementalTradeId, 2), trade1);

                incrementalTradeId++;
            }
        }
    }
}
