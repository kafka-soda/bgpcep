/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.subobject;

import com.google.common.base.Objects.ToStringHelper;

public class EROProtectionType2Subobject extends EROProtectionSubobject {
    private final boolean secondaryLsp;

    private final boolean protectionLsp;

    private final boolean notification;

    private final boolean operational;

    private final byte lspFlags;

    private final byte linkFlags;

    private final boolean inPlace;

    private final boolean required;

    private final byte segFlags;

    /**
     * @param secondaryLsp
     * @param protectionLsp
     * @param notification
     * @param operational
     * @param lspFlags
     * @param linkFlags
     * @param inPlace
     * @param required
     * @param segFlags
     */
    public EROProtectionType2Subobject(boolean secondaryLsp, boolean protectionLsp, boolean notification, boolean operational, byte lspFlags, byte linkFlags,
	    boolean inPlace, boolean required, byte segFlags, boolean loose) {
	super(loose);
	this.secondaryLsp = secondaryLsp;
	this.protectionLsp = protectionLsp;
	this.notification = notification;
	this.operational = operational;
	this.lspFlags = lspFlags;
	this.linkFlags = linkFlags;
	this.inPlace = inPlace;
	this.required = required;
	this.segFlags = segFlags;
    }

    /**
     * @return the secondaryLsp
     */
    public boolean isSecondaryLsp() {
	return this.secondaryLsp;
    }

    /**
     * @return the protectionLsp
     */
    public boolean isProtectionLsp() {
	return this.protectionLsp;
    }

    /**
     * @return the notification
     */
    public boolean isNotification() {
	return this.notification;
    }

    /**
     * @return the operational
     */
    public boolean isOperational() {
	return this.operational;
    }

    /**
     * @return the lspFlags
     */
    public byte getLspFlags() {
	return this.lspFlags;
    }

    /**
     * @return the linkFlags
     */
    public byte getLinkFlags() {
	return this.linkFlags;
    }

    /**
     * @return the inPlace
     */
    public boolean isInPlace() {
	return this.inPlace;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
	return this.required;
    }

    /**
     * @return the segFlags
     */
    public byte getSegFlags() {
	return this.segFlags;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (this.inPlace ? 1231 : 1237);
	result = prime * result + this.linkFlags;
	result = prime * result + this.lspFlags;
	result = prime * result + (this.notification ? 1231 : 1237);
	result = prime * result + (this.operational ? 1231 : 1237);
	result = prime * result + (this.protectionLsp ? 1231 : 1237);
	result = prime * result + (this.required ? 1231 : 1237);
	result = prime * result + (this.secondaryLsp ? 1231 : 1237);
	result = prime * result + this.segFlags;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (this.getClass() != obj.getClass())
	    return false;
	final EROProtectionType2Subobject other = (EROProtectionType2Subobject) obj;
	if (this.inPlace != other.inPlace)
	    return false;
	if (this.linkFlags != other.linkFlags)
	    return false;
	if (this.lspFlags != other.lspFlags)
	    return false;
	if (this.notification != other.notification)
	    return false;
	if (this.operational != other.operational)
	    return false;
	if (this.protectionLsp != other.protectionLsp)
	    return false;
	if (this.required != other.required)
	    return false;
	if (this.secondaryLsp != other.secondaryLsp)
	    return false;
	if (this.segFlags != other.segFlags)
	    return false;
	return true;
    }

    @Override
	protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper) {
		toStringHelper.add("secondaryLsp", this.secondaryLsp);
		toStringHelper.add("protectionLsp", this.protectionLsp);
		toStringHelper.add("notification", this.notification);
		toStringHelper.add("operational", this.operational);
		toStringHelper.add("lspFlags", this.lspFlags);
		toStringHelper.add("linkFlags", this.linkFlags);
		toStringHelper.add("inPlace", this.inPlace);
		toStringHelper.add("required", this.required);
		toStringHelper.add("segFlags", this.segFlags);
		return super.addToStringAttributes(toStringHelper);
	}
}
