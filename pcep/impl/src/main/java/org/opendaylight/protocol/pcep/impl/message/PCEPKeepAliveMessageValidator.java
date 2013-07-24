/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl.message;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.protocol.pcep.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.PCEPMessage;
import org.opendaylight.protocol.pcep.PCEPObject;
import org.opendaylight.protocol.pcep.impl.PCEPMessageValidator;
import org.opendaylight.protocol.pcep.message.PCEPKeepAliveMessage;

/**
 * PCEPKeepAliveMessage validator. Validates message integrity.
 */
public class PCEPKeepAliveMessageValidator extends PCEPMessageValidator {

	@Override
	public List<PCEPMessage> validate(List<PCEPObject> objects) throws PCEPDeserializerException {
		if (objects != null && !objects.isEmpty())
			throw new PCEPDeserializerException("KeepAlive message has content.");

		return new ArrayList<PCEPMessage>() {
			private static final long serialVersionUID = 1L;

			{
				this.add(new PCEPKeepAliveMessage());
			}
		};
	}
}
