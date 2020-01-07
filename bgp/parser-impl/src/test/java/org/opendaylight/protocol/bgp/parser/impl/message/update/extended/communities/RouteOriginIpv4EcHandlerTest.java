/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.impl.message.update.extended.communities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.opendaylight.protocol.bgp.parser.BGPDocumentedException;
import org.opendaylight.protocol.bgp.parser.BGPParsingException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.extended.community.ExtendedCommunity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.extended.community.extended.community.RouteOriginIpv4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.extended.community.extended.community.RouteOriginIpv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.extended.community.extended.community.route.origin.ipv4._case.RouteOriginIpv4Builder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class RouteOriginIpv4EcHandlerTest {
    private static final byte[] INPUT = {
        12, 51, 2, 5, 0x15, 0x2d
    };

    @Test
    public void testHandler() throws BGPDocumentedException, BGPParsingException {
        final RouteOriginIpv4EcHandler handler = new RouteOriginIpv4EcHandler();
        final RouteOriginIpv4Case expected = new RouteOriginIpv4CaseBuilder().setRouteOriginIpv4(
                new RouteOriginIpv4Builder()
                    .setGlobalAdministrator(new Ipv4AddressNoZone("12.51.2.5"))
                    .setLocalAdministrator(Uint16.valueOf(5421))
                    .build()).build();

        final ExtendedCommunity exComm = handler.parseExtendedCommunity(Unpooled.copiedBuffer(INPUT));
        assertEquals(expected, exComm);

        final ByteBuf output = Unpooled.buffer(INPUT.length);
        handler.serializeExtendedCommunity(expected, output);
        assertArrayEquals(INPUT, output.array());
    }
}
