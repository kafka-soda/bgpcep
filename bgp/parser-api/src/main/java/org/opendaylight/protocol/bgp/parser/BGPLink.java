/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser;

import org.opendaylight.protocol.bgp.concepts.BGPObject;

import org.opendaylight.protocol.bgp.linkstate.LinkIdentifier;

/**
 * Interface exposing the attributes of a BGPLink.
 */
public interface BGPLink extends BGPObject {
	@Override
	public BGPLinkState currentState();

	/**
	 * Returns object Link Identifier describing this link.
	 * 
	 * @return Link Identifier
	 */
	public LinkIdentifier getLinkIdentifier();
}
