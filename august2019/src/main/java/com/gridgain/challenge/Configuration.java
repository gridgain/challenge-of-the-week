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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.ignite.cache.CacheKeyConfiguration;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

public class Configuration {

    public static IgniteConfiguration getIgniteCfg(int port) {
        IgniteConfiguration cfg = new IgniteConfiguration();
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
        discoverySpi.setLocalPort(port);
        discoverySpi.setLocalPortRange(20);
        TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(Collections.singletonList("127.0.0.1:48500..48520"));
        discoverySpi.setIpFinder(finder);

        cfg.setDiscoverySpi(discoverySpi);
        return cfg;
    }

    public static CacheConfiguration<UUID, Trader> getTraderCacheConfiguration() {
        CacheConfiguration<UUID, Trader> traderCacheCfg = new CacheConfiguration<>();
        traderCacheCfg.setName("traders");
        traderCacheCfg.setSqlSchema("PUBLIC");
        traderCacheCfg.setQueryEntities(Collections.singleton(
            new QueryEntity(UUID.class, Trader.class)
                .setKeyFieldName("id")
                .addQueryField("id", UUID.class.getName(), null)
                .addQueryField("name", String.class.getName(), null)
        ));
        return traderCacheCfg;
    }

    public static CacheConfiguration<TradeKey, Trade> getTradeCacheConfiguration() {
        CacheConfiguration<TradeKey, Trade> tradeCacheCfg = new CacheConfiguration<>();
        tradeCacheCfg.setName("trades");
        tradeCacheCfg.setSqlSchema("PUBLIC");
        Set<String> keyFields = new HashSet<>();
        keyFields.add("id");
        keyFields.add("version");
        tradeCacheCfg.setQueryEntities(Collections.singleton(
            new QueryEntity(TradeKey.class, Trade.class)
                .setKeyFields(keyFields)
                .addQueryField("id", Long.class.getName(), null)
                .addQueryField("version", Long.class.getName(), null)
                .addQueryField("traderId", UUID.class.getName(), null)
                .addQueryField("currencyPair", String.class.getName(), null)
                .addQueryField("amount", BigDecimal.class.getName(), null)
                .addQueryField("lifecycleEventId", Integer.class.getName(), null)

        ));
        return tradeCacheCfg;
    }
}
