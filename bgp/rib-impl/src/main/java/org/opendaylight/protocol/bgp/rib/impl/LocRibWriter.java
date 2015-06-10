/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.rib.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.controller.md.sal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.controller.md.sal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMTransactionChain;
import org.opendaylight.protocol.bgp.rib.impl.spi.RIBSupportContext;
import org.opendaylight.protocol.bgp.rib.impl.spi.RIBSupportContextRegistry;
import org.opendaylight.protocol.bgp.rib.spi.RIBSupport;
import org.opendaylight.protocol.bgp.rib.spi.RibSupportUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.AsNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.PeerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.bgp.rib.rib.LocRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.bgp.rib.rib.Peer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.bgp.rib.rib.peer.AdjRibOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.bgp.rib.rib.peer.EffectiveRibIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.tables.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.tables.Routes;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
final class LocRibWriter implements AutoCloseable, DOMDataTreeChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocRibWriter.class);

    private static final LeafNode<Boolean> ATTRIBUTES_UPTODATE_TRUE = ImmutableNodes.leafNode(QName.create(Attributes.QNAME, "uptodate"), Boolean.TRUE);
    private static final NodeIdentifier ROUTES_IDENTIFIER = new NodeIdentifier(Routes.QNAME);
    private static final NodeIdentifier EFFRIBIN_NID = new NodeIdentifier(EffectiveRibIn.QNAME);
    private static final NodeIdentifier TABLES_NID = new NodeIdentifier(Tables.QNAME);

    private final Map<PathArgument, AbstractRouteEntry> routeEntries = new HashMap<>();
    private final YangInstanceIdentifier locRibTarget;
    private final DOMTransactionChain chain;
    private final ExportPolicyPeerTracker peerPolicyTracker;
    private final NodeIdentifier attributesIdentifier;
    private final Long ourAs;
    private final RIBSupport ribSupport;
    private final NodeIdentifierWithPredicates tableKey;
    private final RIBSupportContextRegistry registry;
    private final ListenerRegistration<LocRibWriter> reg;

    LocRibWriter(final RIBSupportContextRegistry registry, final DOMTransactionChain chain, final YangInstanceIdentifier target, final Long ourAs,
        final DOMDataTreeChangeService service, final PolicyDatabase pd, final TablesKey tablesKey) {
        this.chain = Preconditions.checkNotNull(chain);
        this.tableKey = RibSupportUtils.toYangTablesKey(tablesKey);
        this.locRibTarget = YangInstanceIdentifier.create(target.node(LocRib.QNAME).node(Tables.QNAME).node(this.tableKey).getPathArguments());
        this.ourAs = Preconditions.checkNotNull(ourAs);
        this.registry = registry;
        this.ribSupport = this.registry.getRIBSupportContext(tablesKey).getRibSupport();
        this.attributesIdentifier = this.ribSupport.routeAttributesIdentifier();
        this.peerPolicyTracker = new ExportPolicyPeerTracker(pd);

        final DOMDataWriteTransaction tx = this.chain.newWriteOnlyTransaction();
        tx.merge(LogicalDatastoreType.OPERATIONAL, this.locRibTarget.node(Routes.QNAME), this.ribSupport.emptyRoutes());
        tx.merge(LogicalDatastoreType.OPERATIONAL, this.locRibTarget.node(Attributes.QNAME).node(ATTRIBUTES_UPTODATE_TRUE.getNodeType()), ATTRIBUTES_UPTODATE_TRUE);
        tx.submit();

        final YangInstanceIdentifier tableId = target.node(Peer.QNAME).node(Peer.QNAME);

        this.reg = service.registerDataTreeChangeListener(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, tableId), this);
    }

    public static LocRibWriter create(@Nonnull final RIBSupportContextRegistry registry, @Nonnull final TablesKey tablesKey, @Nonnull final DOMTransactionChain chain, @Nonnull final YangInstanceIdentifier target,
        @Nonnull final AsNumber ourAs, @Nonnull final DOMDataTreeChangeService service, @Nonnull final PolicyDatabase pd) {
        return new LocRibWriter(registry, chain, target, ourAs.getValue(), service, pd, tablesKey);
    }

    @Override
    public void close() {
        this.reg.close();
        // FIXME: wipe the local rib
        // FIXME: wait for the chain to close? unfortunately RIBImpl is the listener, so that may require some work
        this.chain.close();
    }

    private AbstractRouteEntry createEntry(final PathArgument routeId) {
        final AbstractRouteEntry ret = this.ribSupport.isComplexRoute() ? new ComplexRouteEntry() : new SimpleRouteEntry();

        this.routeEntries.put(routeId, ret);
        LOG.trace("Created new entry for {}", routeId);
        return ret;
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
        final DOMDataWriteTransaction tx = this.chain.newWriteOnlyTransaction();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Received data change to LocRib {}", Arrays.toString(changes.toArray()));
        }

        /*
         * We use two-stage processing here in hopes that we avoid duplicate
         * calculations when multiple peers have changed a particular entry.
         */
        final Map<RouteUpdateKey, AbstractRouteEntry> toUpdate = new HashMap<>();

        update(tx, changes, toUpdate);

        // Now walk all updated entries
        walkThrough(tx, toUpdate);

        tx.submit();
    }

    private void update(final DOMDataWriteTransaction tx, final Collection<DataTreeCandidate> changes,
        final Map<RouteUpdateKey, AbstractRouteEntry> toUpdate) {

        for (final DataTreeCandidate tc : changes) {
            // call out peer-role has changed
            final YangInstanceIdentifier rootPath = tc.getRootPath();
            final DataTreeCandidateNode rootNode = tc.getRootNode();
            final DataTreeCandidateNode roleChange =  rootNode.getModifiedChild(AbstractPeerRoleTracker.PEER_ROLE_NID);
            if (roleChange != null) {
                this.peerPolicyTracker.onDataTreeChanged(roleChange, IdentifierUtils.peerPath(rootPath));
            }
            // filter out any change outside EffRibsIn
            final DataTreeCandidateNode ribIn = rootNode.getModifiedChild(EFFRIBIN_NID);
            if (ribIn == null) {
                LOG.debug("Skipping change {}", tc.getRootNode());
                continue;
            }
            final DataTreeCandidateNode table = ribIn.getModifiedChild(TABLES_NID).getModifiedChild(this.tableKey);
            if (table == null) {
                LOG.debug("Skipping change {}", tc.getRootNode());
                continue;
            }
            final NodeIdentifierWithPredicates peerKey = IdentifierUtils.peerKey(rootPath);
            final PeerId peerId = IdentifierUtils.peerId(peerKey);
            final UnsignedInteger routerId = RouterIds.routerIdForPeerId(peerId);
            for (final DataTreeCandidateNode child : table.getChildNodes()) {
                if ((Attributes.QNAME).equals(child.getIdentifier().getNodeType())) {
                    // putting uptodate attribute in
                    LOG.trace("Uptodate found for {}", child.getDataAfter());
                    tx.put(LogicalDatastoreType.OPERATIONAL, this.locRibTarget.node(child.getIdentifier()), child.getDataAfter().get());
                    continue;
                }
                for (final DataTreeCandidateNode route : this.ribSupport.changedRoutes(child)) {
                    final PathArgument routeId = route.getIdentifier();
                    AbstractRouteEntry entry = this.routeEntries.get(routeId);

                    final Optional<NormalizedNode<?, ?>> maybeData = route.getDataAfter();
                    if (maybeData.isPresent()) {
                        if (entry == null) {
                            entry = createEntry(routeId);
                        }

                        entry.addRoute(routerId, this.attributesIdentifier, maybeData.get());
                    } else if (entry != null && entry.removeRoute(routerId)) {
                        this.routeEntries.remove(routeId);
                        entry = null;
                        LOG.trace("Removed route from {}", routerId);
                    }
                    LOG.debug("Updated route {} entry {}", routeId, entry);
                    toUpdate.put(new RouteUpdateKey(peerId, routeId), entry);
                }
            }
        }
    }

    private void walkThrough(final DOMDataWriteTransaction tx, final Map<RouteUpdateKey, AbstractRouteEntry> toUpdate) {
        for (final Entry<RouteUpdateKey, AbstractRouteEntry> e : toUpdate.entrySet()) {
            LOG.trace("Walking through {}", e);
            final AbstractRouteEntry entry = e.getValue();
            final RouteUpdateKey key = e.getKey();
            final NormalizedNode<?, ?> value;

            if (entry != null) {
                if (!entry.selectBest(this.ourAs)) {
                    // Best path has not changed, no need to do anything else. Proceed to next route.
                    LOG.trace("Continuing");
                    continue;
                }
                value = entry.createValue(key.getRouteId());
                LOG.trace("Selected best value {}", value);
            } else {
                value = null;
            }

            final YangInstanceIdentifier writePath = this.ribSupport.routePath(this.locRibTarget.node(ROUTES_IDENTIFIER), key.getRouteId());
            if (value != null) {
                LOG.debug("Write route to LocRib {}", value);
                tx.put(LogicalDatastoreType.OPERATIONAL, writePath, value);
            } else {
                LOG.debug("Delete route from LocRib {}", entry);
                tx.delete(LogicalDatastoreType.OPERATIONAL, writePath);
            }
            fillAdjRibsOut(tx, entry, value, key);
        }
    }

    private void fillAdjRibsOut(final DOMDataWriteTransaction tx, final AbstractRouteEntry entry, final NormalizedNode<?, ?> value, final RouteUpdateKey key) {
        /*
         * We need to keep track of routers and populate adj-ribs-out, too. If we do not, we need to
         * expose from which client a particular route was learned from in the local RIB, and have
         * the listener perform filtering.
         *
         * We walk the policy set in order to minimize the amount of work we do for multiple peers:
         * if we have two eBGP peers, for example, there is no reason why we should perform the translation
         * multiple times.
         */
        for (final PeerRole role : PeerRole.values()) {
            final PeerExportGroup peerGroup = this.peerPolicyTracker.getPeerGroup(role);
            if (peerGroup != null) {
                final ContainerNode attributes = entry == null ? null : entry.attributes();
                final PeerId peerId = key.getPeerId();
                final ContainerNode effectiveAttributes = peerGroup.effectiveAttributes(peerId, attributes);
                for (final Entry<PeerId, YangInstanceIdentifier> pid : peerGroup.getPeers()) {
                    // This points to adj-rib-out for a particular peer/table combination
                    final RIBSupportContext ribCtx = this.registry.getRIBSupportContext(this.tableKey);
                    // FIXME: the table should be created for a peer only once
                    ribCtx.clearTable(tx, pid.getValue().node(AdjRibOut.QNAME).node(Tables.QNAME).node(this.tableKey));
                    final YangInstanceIdentifier routeTarget = this.ribSupport.routePath(pid.getValue().node(AdjRibOut.QNAME).node(Tables.QNAME).node(this.tableKey).node(Routes.QNAME), key.getRouteId());
                    if (effectiveAttributes != null && value != null && !peerId.equals(pid.getKey())) {
                        LOG.debug("Write route to AdjRibsOut {}", value);
                        tx.put(LogicalDatastoreType.OPERATIONAL, routeTarget, value);
                        tx.put(LogicalDatastoreType.OPERATIONAL, routeTarget.node(this.attributesIdentifier), effectiveAttributes);
                    } else {
                        LOG.trace("Removing {} from transaction", routeTarget);
                        tx.delete(LogicalDatastoreType.OPERATIONAL, routeTarget);
                    }
                }
            }
        }
    }
}
