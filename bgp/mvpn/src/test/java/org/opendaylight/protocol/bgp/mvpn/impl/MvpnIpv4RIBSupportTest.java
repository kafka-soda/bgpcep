/*
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.bgp.mvpn.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.protocol.bgp.rib.spi.AbstractRIBSupportTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Update;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.bgp.rib.rib.loc.rib.tables.routes.MvpnRoutesIpv4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.bgp.rib.rib.loc.rib.tables.routes.MvpnRoutesIpv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.mvpn.destination.MvpnDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.mvpn.destination.MvpnDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationMvpnIpv4AdvertizedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationMvpnIpv4AdvertizedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.destination.mvpn.ipv4.advertized._case.DestinationMvpnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationMvpnIpv4WithdrawnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4.rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationMvpnIpv4WithdrawnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.inter.as.i.pmsi.a.d.grouping.InterAsIPmsiADBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.MvpnChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.mvpn.choice.InterAsIPmsiADCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.routes.MvpnRoutes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.routes.MvpnRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.routes.mvpn.routes.MvpnRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.routes.mvpn.routes.MvpnRouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn.routes.mvpn.routes.MvpnRouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.RdIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

@Ignore
public class MvpnIpv4RIBSupportTest extends AbstractRIBSupportTest<MvpnRoute> {
    private static final MvpnRouteKey ROUTE_KEY;
    private static final MvpnRoute ROUTE;
    private static final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.rev180417.mvpn
            .routes.MvpnRoutes MVPN_ROUTES;
    private static final MvpnChoice MVPN = new InterAsIPmsiADCaseBuilder().setInterAsIPmsiAD(
            new InterAsIPmsiADBuilder()
                    .setSourceAs(new AsNumber(1L))
                    .setRouteDistinguisher(new RouteDistinguisher(new RdIpv4("1.2.3.4:258")))
                    .build()).build();
    private static final PathId PATH_ID = new PathId(0L);
    private static final MvpnDestination MVPN_DESTINATION = new MvpnDestinationBuilder()
            .setMvpnChoice(MVPN)
            .setPathId(PATH_ID)
            .build();
    private static final DestinationMvpnIpv4AdvertizedCase REACH_NLRI = new DestinationMvpnIpv4AdvertizedCaseBuilder()
            .setDestinationMvpn(new DestinationMvpnBuilder()
                    .setMvpnDestination(Collections.singletonList(MVPN_DESTINATION)).build()).build();
    private static final DestinationMvpnIpv4WithdrawnCase UNREACH_NLRI = new DestinationMvpnIpv4WithdrawnCaseBuilder()
            .setDestinationMvpn(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.mvpn.ipv4
                    .rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.destination.mvpn
                    .ipv4.withdrawn._case.DestinationMvpnBuilder()
                    .setMvpnDestination(Collections.singletonList(MVPN_DESTINATION)).build()).build();

    static {
        ROUTE_KEY = new MvpnRouteKey(PATH_ID, "AgwAAQECAwQBAgAAAAE=");
        ROUTE = new MvpnRouteBuilder()
                .setRouteKey(ROUTE_KEY.getRouteKey())
                .setPathId(ROUTE_KEY.getPathId())
                .setAttributes(ATTRIBUTES)
                .setMvpnChoice(MVPN)
                .build();
        MVPN_ROUTES = new MvpnRoutesBuilder().setMvpnRoute(Collections.singletonList(ROUTE)).build();
    }

    private MvpnIpv4RIBSupport ribSupport;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ribSupport = MvpnIpv4RIBSupport.getInstance(this.mappingService);
        setUpTestCustomizer(ribSupport);
        NlriActivator.registerNlriParsers(new ArrayList<>());
    }

    @Test
    public void testDeleteRoutes() {
        final ContainerNode withdraw = createNlriWithDrawnRoute(UNREACH_NLRI);
        this.ribSupport.deleteRoutes(this.tx, getTablePath(), withdraw);
        final InstanceIdentifier<MvpnRoute> instanceIdentifier = this.deletedRoutes.get(0);
        assertEquals(ROUTE_KEY, instanceIdentifier.firstKeyOf(MvpnRoute.class));
    }

    @Test
    public void testPutRoutes() {
        this.ribSupport.putRoutes(this.tx, getTablePath(), createNlriAdvertiseRoute(REACH_NLRI), createAttributes());
        final MvpnRoute route = (MvpnRoute) this.insertedRoutes.get(0).getValue();
        assertEquals(ROUTE, route);
    }


    @Test
    public void testEmptyRoute() {
        final Routes empty = new MvpnRoutesIpv4CaseBuilder().setMvpnRoutes(new MvpnRoutesBuilder()
                .setMvpnRoute(Collections.emptyList()).build()).build();
        final ChoiceNode expected = createRoutes(empty);
        assertEquals(expected, this.ribSupport.emptyRoutes());
    }

    @Test
    public void testBuildMpUnreachNlriUpdate() {
        final Collection<MapEntryNode> routes = createRoutes(MVPN_ROUTES);
        final Update update = this.ribSupport.buildUpdate(Collections.emptyList(), routes, ATTRIBUTES);
        assertEquals(UNREACH_NLRI, update.getAttributes().getAugmentation(Attributes2.class).getMpUnreachNlri()
                .getWithdrawnRoutes().getDestinationType());
        assertNull(update.getAttributes().getAugmentation(Attributes1.class));
    }

    @Test
    public void testBuildMpReachNlriUpdate() {
        final Collection<MapEntryNode> routes = createRoutes(MVPN_ROUTES);
        final Update update = this.ribSupport.buildUpdate(routes, Collections.emptyList(), ATTRIBUTES);
        assertEquals(REACH_NLRI, update.getAttributes().getAugmentation(Attributes1.class).getMpReachNlri()
                .getAdvertizedRoutes().getDestinationType());
        assertNull(update.getAttributes().getAugmentation(Attributes2.class));
    }

    @Test
    public void testCacheableNlriObjects() {
        assertEquals(ImmutableSet.of(), this.ribSupport.cacheableNlriObjects());
    }

    @Test
    public void testCacheableAttributeObjects() {
        assertEquals(ImmutableSet.of(), this.ribSupport.cacheableAttributeObjects());
    }

    @Test
    public void testRouteIdAddPath() {
        assertEquals(ROUTE_KEY, this.ribSupport.createRouteListKey(ROUTE_KEY.getPathId().getValue(),
                ROUTE_KEY.getRouteKey()));
    }

    @Test
    public void testRoutePath() {
        final YangInstanceIdentifier.NodeIdentifierWithPredicates prefixNii = createRouteNIWP(MVPN_ROUTES);
        final YangInstanceIdentifier expected = getRoutePath().node(prefixNii);
        final YangInstanceIdentifier actual = this.ribSupport.routePath(getTablePath().node(Routes.QNAME), prefixNii);
        assertEquals(expected, actual);
    }

    @Test
    public void testRouteAttributesIdentifier() {
        assertEquals(new YangInstanceIdentifier.NodeIdentifier(
                        Attributes.QNAME.withModule(BindingReflections.getQNameModule(MvpnRoutesIpv4Case.class))),
                this.ribSupport.routeAttributesIdentifier());
    }

    @Test
    public void testRoutesCaseClass() {
        assertEquals(MvpnRoutesIpv4Case.class, this.ribSupport.routesCaseClass());
    }

    @Test
    public void testRoutesContainerClass() {
        assertEquals(MvpnRoutes.class, this.ribSupport.routesContainerClass());
    }

    @Test
    public void testRoutesListClass() {
        assertEquals(MvpnRoute.class, this.ribSupport.routesListClass());
    }

    @Test
    public void testChangedRoutes() {
        final Routes emptyCase = new MvpnRoutesIpv4CaseBuilder().build();
        DataTreeCandidateNode tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(),
                createRoutes(emptyCase)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes emptyRoutes
                = new MvpnRoutesIpv4CaseBuilder().setMvpnRoutes(new MvpnRoutesBuilder().build()).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(emptyRoutes)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes routes = new MvpnRoutesIpv4CaseBuilder().setMvpnRoutes(MVPN_ROUTES).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(routes)).getRootNode();
        final Collection<DataTreeCandidateNode> result = this.ribSupport.changedRoutes(tree);
        assertFalse(result.isEmpty());
    }
}