/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.util;

import org.opendaylight.protocol.bgp.concepts.BaseBGPObjectState;

import org.opendaylight.protocol.concepts.IPv4Address;
import org.opendaylight.protocol.concepts.Prefix;
import org.opendaylight.protocol.bgp.linkstate.NetworkRouteState;

/**
 * Implementation of {@link AbstractBGPPrefix}.
 */
public final class BGPIPv4RouteImpl extends AbstractBGPRoute<IPv4Address> {
	private static final long serialVersionUID = 1L;

	public BGPIPv4RouteImpl(final Prefix<IPv4Address> prefix, final BaseBGPObjectState base,
			final NetworkRouteState<IPv4Address> prefixState) {
		super(prefix, base, prefixState);
	}
}
