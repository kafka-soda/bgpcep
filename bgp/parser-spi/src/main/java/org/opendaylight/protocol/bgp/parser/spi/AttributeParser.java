/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.spi;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.protocol.bgp.parser.BGPDocumentedException;
import org.opendaylight.protocol.bgp.parser.BGPParsingException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.AttributesBuilder;

/**
 * Common interface for attribute parser implementation.
 */
public interface AttributeParser {
    /**
     * Parses attribute from ByteBuf buffer.
     *
     * @param buffer Encoded attribute body in ByteBuf.
     * @param builder Path attributes builder. Guaranteed to contain all valid attributes whose type is numerically
     *        lower than this attribute's type.
     * @param constraint Peer specific constraints, may be null
     */
    void parseAttribute(@Nonnull ByteBuf buffer, @Nonnull AttributesBuilder builder,
            @Nullable PeerSpecificParserConstraint constraint) throws BGPDocumentedException, BGPParsingException;
}
