/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl.message;

import org.opendaylight.protocol.pcep.PCEPMessage;
import org.opendaylight.protocol.pcep.impl.PCEPMessageParser;
import org.opendaylight.protocol.pcep.impl.PCEPObjectFactory;
import org.opendaylight.protocol.pcep.message.PCEPOpenMessage;

/**
 * Parser for {@link org.opendaylight.protocol.pcep.message.PCEPOpenMessage PCEPOpenMessage}
 */
public class PCEPOpenMessageParser implements PCEPMessageParser {

    @Override
    public byte[] put(PCEPMessage msg) {
	if (!(msg instanceof PCEPOpenMessage))
	    throw new IllegalArgumentException("Wrong instance of PCEPMessage. Passed instance " + msg.getClass() + ". Nedded PCEPOpenMessage.");

	return PCEPObjectFactory.put(((PCEPOpenMessage) msg).getAllObjects());
    }

}
