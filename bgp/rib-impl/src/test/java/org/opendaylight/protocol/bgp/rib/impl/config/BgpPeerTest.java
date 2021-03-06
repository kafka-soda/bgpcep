/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.rib.impl.config;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.protocol.concepts.KeyMapping;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.multiprotocol.rev151009.bgp.common.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.multiprotocol.rev151009.bgp.common.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.multiprotocol.rev151009.bgp.common.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.AddPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.AddPathsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.AfiSafis;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.RouteReflector;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.RouteReflectorBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.Timers;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.TimersBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.Transport;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group.add.paths.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbors.Neighbor;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbors.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.CommunityType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.PeerType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev151009.RrClusterIdTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.openconfig.extensions.rev180329.NeighborAddPathsConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.openconfig.extensions.rev180329.NeighborTransportConfigBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

public class BgpPeerTest extends AbstractConfig {
    static final Uint8 SHORT = Uint8.ZERO;
    static final IpAddress NEIGHBOR_ADDRESS = new IpAddress(new Ipv4Address("127.0.0.1"));
    static final String MD5_PASSWORD = "123";
    static final PortNumber PORT = new PortNumber(Uint16.valueOf(179));
    static final AfiSafi AFI_SAFI_IPV4 = new AfiSafiBuilder().setAfiSafiName(IPV4UNICAST.class)
            .addAugmentation(new NeighborAddPathsConfigBuilder().setReceive(true).setSendMax(Uint8.ZERO).build())
            .build();
    static final Map<AfiSafiKey, AfiSafi> AFI_SAFI = Collections.singletonMap(AFI_SAFI_IPV4.key(), AFI_SAFI_IPV4);
    private static final BigDecimal DEFAULT_TIMERS = BigDecimal.valueOf(30);
    private BgpPeer bgpPeer;

    static Neighbor createNeighborExpected(final IpAddress neighborAddress) {
        return new NeighborBuilder()
                .setAfiSafis(createAfiSafi())
                .setConfig(createConfig())
                .setNeighborAddress(neighborAddress)
                .setRouteReflector(createRR())
                .setTimers(createTimers())
                .setTransport(createTransport())
                .setAddPaths(createAddPath())
                .build();
    }

    static Transport createTransport() {
        return new TransportBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp
                .rev151009.bgp.neighbor.group.transport.ConfigBuilder()
                    .setMtuDiscovery(false)
                    .setPassiveMode(false)
                    .addAugmentation(new NeighborTransportConfigBuilder().setRemotePort(PORT).build())
                    .build())
                .build();
    }

    static Timers createTimers() {
        return new TimersBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009
                .bgp.neighbor.group.timers.ConfigBuilder()
                .setHoldTime(DEFAULT_TIMERS)
                .setMinimumAdvertisementInterval(DEFAULT_TIMERS)
                .setKeepaliveInterval(DEFAULT_TIMERS)
                .setConnectRetry(DEFAULT_TIMERS).build()).build();
    }

    private static RouteReflector createRR() {
        return new RouteReflectorBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp
                .rev151009.bgp.neighbor.group.route.reflector.ConfigBuilder()
                .setRouteReflectorClusterId(RrClusterIdTypeBuilder.getDefaultInstance("127.0.0.1"))
                .setRouteReflectorClient(false).build()).build();
    }

    static Config createConfig() {
        return new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev151009.bgp.neighbor.group
                .ConfigBuilder()
                .setPeerAs(AS)
                .setLocalAs(LOCAL_AS)
                .setPeerType(PeerType.INTERNAL)
                .setAuthPassword(MD5_PASSWORD)
                .setRouteFlapDamping(false)
                .setSendCommunity(CommunityType.NONE).build();
    }

    static AfiSafis createAfiSafi() {
        return new AfiSafisBuilder().setAfiSafi(AFI_SAFI).build();
    }

    static AddPaths createAddPath() {
        return new AddPathsBuilder().setConfig(new ConfigBuilder().setReceive(true).setSendMax(SHORT).build()).build();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.bgpPeer = new BgpPeer(mock(RpcProviderService.class));
        doNothing().when(this.serviceRegistration).unregister();
    }

    @Test
    public void testBgpPeer() {
        final Neighbor neighbor = new NeighborBuilder().setAfiSafis(createAfiSafi()).setConfig(createConfig())
                .setNeighborAddress(NEIGHBOR_ADDRESS).setRouteReflector(createRR()).setTimers(createTimers())
                .setTransport(createTransport()).setAddPaths(createAddPath()).build();

        this.bgpPeer.start(this.rib, neighbor, null, this.peerGroupLoader, this.tableTypeRegistry);
        verify(this.rib).createPeerDOMChain(any());
        verify(this.rib, times(2)).getLocalAs();
        verify(this.rib).getLocalTables();

        this.bgpPeer.instantiateServiceInstance();
        verify(this.bgpPeerRegistry).addPeer(any(), any(), any());
        verify(this.dispatcher).createReconnectingClient(any(InetSocketAddress.class),
                any(), anyInt(), any(KeyMapping.class));

        try {
            this.bgpPeer.start(this.rib, neighbor, null, this.peerGroupLoader, this.tableTypeRegistry);
            fail("Expected Exception");
        } catch (final IllegalStateException expected) {
            assertEquals("Previous peer instance was not closed.", expected.getMessage());
        }
        this.bgpPeer.setServiceRegistration(this.serviceRegistration);
        this.bgpPeer.closeServiceInstance();
        verify(this.bgpPeerRegistry).removePeer(any());
        verify(this.future).cancel(true);
        this.bgpPeer.close();
        verify(this.serviceRegistration).unregister();

        this.bgpPeer.restart(this.rib, null, this.peerGroupLoader, this.tableTypeRegistry);
        verify(this.rib, times(2)).createPeerDOMChain(any());
        verify(this.rib, times(4)).getLocalAs();
        verify(this.rib, times(2)).getLocalTables();
        this.bgpPeer.instantiateServiceInstance();
        verify(this.bgpPeerRegistry, times(2)).addPeer(any(), any(), any());
        verify(this.dispatcher, times(2)).createReconnectingClient(any(InetSocketAddress.class),
                any(), anyInt(), any(KeyMapping.class));

        final Neighbor neighborExpected = createNeighborExpected(NEIGHBOR_ADDRESS);
        assertTrue(this.bgpPeer.containsEqualConfiguration(neighborExpected));
        assertFalse(this.bgpPeer.containsEqualConfiguration(createNeighborExpected(
                new IpAddress(new Ipv4Address("127.0.0.2")))));

        this.bgpPeer.closeServiceInstance();
        verify(this.bgpPeerRegistry, times(2)).removePeer(any());
        verify(this.future, times(2)).cancel(true);

        this.bgpPeer.instantiateServiceInstance();
        verify(this.bgpPeerRegistry, times(3)).addPeer(any(), any(), any());
        verify(this.dispatcher, times(3)).createReconnectingClient(any(InetSocketAddress.class),
                any(), anyInt(), any(KeyMapping.class));

        this.bgpPeer.closeServiceInstance();
        verify(this.bgpPeerRegistry, times(3)).removePeer(any());
        verify(this.future, times(3)).cancel(true);
        verify(this.rib, times(3)).createPeerDOMChain(any());

        this.bgpPeer.restart(this.rib, null, this.peerGroupLoader, this.tableTypeRegistry);
        verify(this.rib, times(4)).createPeerDOMChain(any());
        verify(this.rib, times(6)).getLocalAs();
        verify(this.rib, times(3)).getLocalTables();
        this.bgpPeer.instantiateServiceInstance();
        verify(this.bgpPeerRegistry, times(4)).addPeer(any(), any(), any());
        verify(this.dispatcher, times(4)).createReconnectingClient(any(InetSocketAddress.class),
                any(), anyInt(), any(KeyMapping.class));
        this.bgpPeer.closeServiceInstance();
        verify(this.bgpPeerRegistry, times(4)).removePeer(any());
        verify(this.future, times(4)).cancel(true);
        this.bgpPeer.close();
        verify(this.serviceRegistration).unregister();

        final Neighbor neighborDiffConfig = new NeighborBuilder().setNeighborAddress(NEIGHBOR_ADDRESS)
                .setAfiSafis(createAfiSafi()).build();
        this.bgpPeer.start(this.rib, neighborDiffConfig, null, this.peerGroupLoader, this.tableTypeRegistry);
        assertTrue(this.bgpPeer.containsEqualConfiguration(neighborDiffConfig));
        this.bgpPeer.close();
    }
}
