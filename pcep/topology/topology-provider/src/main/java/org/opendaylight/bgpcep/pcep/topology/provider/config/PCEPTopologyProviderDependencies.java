/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bgpcep.pcep.topology.provider.config;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.bgpcep.pcep.server.PceServerProvider;
import org.opendaylight.bgpcep.pcep.topology.provider.TopologySessionListenerFactory;
import org.opendaylight.bgpcep.pcep.topology.spi.stats.TopologySessionStatsRegistry;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.protocol.pcep.PCEPDispatcher;

/**
 * Provides required dependencies for PCEPTopologyProviderProvider instantiation.
 */
@Beta
@NonNullByDefault
public interface PCEPTopologyProviderDependencies {
    /**
     * PCEP Dispatcher.
     *
     * @return PCEPDispatcher
     */
    PCEPDispatcher getPCEPDispatcher();

    /**
     * Rpc Provider Registry.
     *
     * @return RpcProviderRegistry
     */
    RpcProviderService getRpcProviderRegistry();

    /**
     * DataBroker.
     *
     * @return DataBroker
     */
    DataBroker getDataBroker();

    /**
     * Topology Session Listener Factory.
     *
     * @return TopologySessionListenerFactory
     */
    TopologySessionListenerFactory getTopologySessionListenerFactory();

    /**
     * Topology Session State Registry.
     *
     * @return TopologySessionStateRegistry
     */
    TopologySessionStatsRegistry getStateRegistry();

    /**
     * PCE Server Provider.
     *
     * @return PceServerProvider
     */
    PceServerProvider getPceServerProvider();
}
