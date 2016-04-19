/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.bgp.mode.spi;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.protocol.bgp.mode.api.RouteEntry;
import org.opendaylight.protocol.bgp.rib.spi.CacheDisconnectedPeers;
import org.opendaylight.protocol.bgp.rib.spi.ExportPolicyPeerTracker;
import org.opendaylight.protocol.bgp.rib.spi.RIBSupport;
import org.opendaylight.protocol.bgp.rib.spi.RibSupportUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.bgp.rib.rib.peer.AdjRibOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.tables.Routes;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractRouteEntry implements RouteEntry {
    protected static final NodeIdentifier ROUTES_IDENTIFIER = new NodeIdentifier(Routes.QNAME);
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRouteEntry.class);

    protected final void fillLocRib(final YangInstanceIdentifier routeTarget, final NormalizedNode<?, ?> value, final DOMDataWriteTransaction tx) {
        if (value != null) {
            LOG.debug("Write route to LocRib {}", value);
            tx.put(LogicalDatastoreType.OPERATIONAL, routeTarget, value);
        } else {
            LOG.debug("Delete route from LocRib {}", routeTarget);
            tx.delete(LogicalDatastoreType.OPERATIONAL, routeTarget);
        }
    }

    protected final boolean writeRoute(final PeerId destPeer, final YangInstanceIdentifier routeTarget, final ContainerNode effAttrib,
        final NormalizedNode<?, ?> value, final RIBSupport ribSup, final DOMDataWriteTransaction tx) {
        if (effAttrib != null && value != null) {
            LOG.debug("Write route {} to peer AdjRibsOut {}", value, destPeer);
            tx.put(LogicalDatastoreType.OPERATIONAL, routeTarget, value);
            tx.put(LogicalDatastoreType.OPERATIONAL, routeTarget.node(ribSup.routeAttributesIdentifier()), effAttrib);
            return true;
        }
        return false;
    }

    protected final boolean filterRoutes(final PeerId rootPeer, final PeerId destPeer, final ExportPolicyPeerTracker peerPT,
        final TablesKey localTK, final CacheDisconnectedPeers discPeers) {
        return !rootPeer.equals(destPeer) && isTableSupported(destPeer, peerPT, localTK) && !discPeers.isPeerDisconnected(destPeer);
    }

    private boolean isTableSupported(final PeerId destPeer, final ExportPolicyPeerTracker peerPT, final TablesKey localTK) {
        if (!peerPT.isTableSupported(destPeer)) {
            LOG.trace("Route rejected, peer {} does not support this table type {}", destPeer, localTK);
            return false;
        }
        return true;
    }

    protected final YangInstanceIdentifier getAdjRibOutYII(final RIBSupport ribSup, final YangInstanceIdentifier rootPath, final PathArgument routeId,
        final TablesKey localTK) {
        return ribSup.routePath(rootPath.node(AdjRibOut.QNAME).node(Tables.QNAME).node(RibSupportUtils.toYangTablesKey(localTK))
            .node(ROUTES_IDENTIFIER), routeId);
    }
}