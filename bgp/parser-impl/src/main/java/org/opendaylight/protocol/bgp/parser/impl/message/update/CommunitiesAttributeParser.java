/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.impl.message.update;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.protocol.bgp.parser.BGPDocumentedException;
import org.opendaylight.protocol.bgp.parser.BGPError;
import org.opendaylight.protocol.bgp.parser.spi.AttributeParser;
import org.opendaylight.protocol.bgp.parser.spi.AttributeSerializer;
import org.opendaylight.protocol.bgp.parser.spi.AttributeUtil;
import org.opendaylight.protocol.bgp.parser.spi.PeerSpecificParserConstraint;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.protocol.util.ReferenceCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.Communities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.Community;

public final class CommunitiesAttributeParser implements AttributeParser, AttributeSerializer {

    public static final int TYPE = 8;

    private static final int COMMUNITY_LENGTH = 4;

    private static final byte[] NO_EXPORT = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x01 };

    private static final byte[] NO_ADVERTISE = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x02 };

    private static final byte[] NO_EXPORT_SUBCONFED = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x03 };

    private final ReferenceCache refCache;

    public CommunitiesAttributeParser(final ReferenceCache refCache) {
        this.refCache = requireNonNull(refCache);
    }

    @Override
    public void parseAttribute(final ByteBuf buffer, final AttributesBuilder builder,
            final PeerSpecificParserConstraint constraint) throws BGPDocumentedException {
        // FIXME: BGPCEP-359: treat-as-withdraw if buffer.readableBytes() is 0 or not a multiple of COMMUNITY_LENGTH
        // FIXME: optimize allocation once we have done the above check
        final List<Communities> set = new ArrayList<>();
        while (buffer.isReadable()) {
            set.add((Communities) parseCommunity(this.refCache, buffer.readSlice(COMMUNITY_LENGTH)));
        }
        builder.setCommunities(set);
    }

   /**
    * Parse known Community, if unknown, a new one will be created.
    *
    * @param refCache
    * @param buffer byte array to be parsed
    * @return new Community
    * @throws BGPDocumentedException
    */
    private static Community parseCommunity(final ReferenceCache refCache, final ByteBuf buffer)
            throws BGPDocumentedException {
        if (buffer.readableBytes() != COMMUNITY_LENGTH) {
            throw new BGPDocumentedException("Community with wrong length: "
                    + buffer.readableBytes(), BGPError.OPT_ATTR_ERROR);
        }
        final byte[] body = ByteArray.getBytes(buffer, COMMUNITY_LENGTH);
        if (Arrays.equals(body, NO_EXPORT)) {
            return CommunityUtil.NO_EXPORT;
        } else if (Arrays.equals(body, NO_ADVERTISE)) {
            return CommunityUtil.NO_ADVERTISE;
        } else if (Arrays.equals(body, NO_EXPORT_SUBCONFED)) {
            return CommunityUtil.NO_EXPORT_SUBCONFED;
        }
        return CommunityUtil.create(refCache, buffer.readUnsignedShort(), buffer.readUnsignedShort());
    }

    @Override
    public void serializeAttribute(final Attributes pathAttributes, final ByteBuf byteAggregator) {
        final List<Communities> communities = pathAttributes.getCommunities();
        if (communities == null || communities.isEmpty()) {
            return;
        }
        final ByteBuf communitiesBuffer = Unpooled.buffer();
        for (final Community community : communities) {
            communitiesBuffer.writeShort(community.getAsNumber().getValue().shortValue());
            communitiesBuffer.writeShort(community.getSemantics().shortValue());
        }
        AttributeUtil.formatAttribute(AttributeUtil.OPTIONAL | AttributeUtil.TRANSITIVE,
                TYPE, communitiesBuffer, byteAggregator);
    }
}
