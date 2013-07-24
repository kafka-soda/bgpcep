/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.concepts;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.opendaylight.protocol.util.ByteArray;
import com.google.common.base.Preconditions;

/**
 * This class represents an IPv6 address. This is the well-known 16-byte address
 * used all over the Internet. The difference between this class and the
 * readily-available Inet6Address is the fact it is derived from a well-known
 * concept and it is a pure data holder.
 */
public class IPv6Address implements NetworkAddress<IPv6Address> {
	private static final long serialVersionUID = 8627230318476805811L;
	private final byte[] bytes;

	/**
	 * Instantiate a new IPv6Address based on raw byte representation.
	 *
	 * @param bytes Bytearray holding the byte representation
	 * @throws
	 * @li IllegalArgumentException when length of bytes is not 16
	 */
	public IPv6Address(final byte[] bytes) {
		Preconditions.checkNotNull(bytes);
		Preconditions.checkArgument(bytes.length == 16);
		this.bytes = bytes;
	}

	/**
	 * Instantiate a new IPv6Address based on the contents of a Inet6Address.
	 *
	 * @param address
	 *            IPv6 address in Inet6Address format
	 * @throws
	 * @li IllegalArgumentException when address is not in Inet6Address format
	 */
	public IPv6Address(final InetAddress address) {
		if (!(address instanceof Inet6Address))
			throw new IllegalArgumentException(
					"This class can only handle IPv6 addresses");
		this.bytes = address.getAddress();
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof IPv6Address
				&& Arrays.equals(this.bytes, ((IPv6Address) o).bytes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.bytes);
	}

	@Override
	public byte[] getAddress() {
		return this.bytes;
	}

	@Override
	public int compareTo(final IPv6Address addr) {
		for (int i = 0; i < this.bytes.length; ++i) {
			if (this.bytes[i] != addr.bytes[i]) {
				int i1 = 0, i2 = 0;
				i1 = this.bytes[i] < 0 ? this.bytes[i] + 256 : this.bytes[i];
				i2 = addr.bytes[i] < 0 ? addr.bytes[i] + 256 : addr.bytes[i];
				return i1 - i2;
			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Output string is formated according to InetAddress formatting rules.
	 */
	@Override
	public String toString() {
		try {
			return InetAddress.getByAddress(Arrays.copyOf(this.bytes, 16))
					.getHostAddress();
		} catch (final UnknownHostException e) {
			throw new RuntimeException("Unexpected malformed address", e);
		}
	}

	@Override
	public IPv6Address applyMask(final int bits) {
		if (bits < 0 || bits > 128)
			throw new IllegalArgumentException("Bit length has to be in range 0-128");
		return new IPv6Address(ByteArray.maskBytes(this.bytes, bits));
	}

	@Override
	public IPv6Prefix asPrefix(int length) {
		return new IPv6Prefix(this, length);
	}

	@Override
	public IPv6Prefix asHostPrefix() {
		return asPrefix(128);
	}

	@Override
	public IPv6 getAddressFamily() {
		return IPv6.FAMILY;
	}
}
