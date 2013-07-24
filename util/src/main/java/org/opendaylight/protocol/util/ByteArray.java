/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.codec.binary.Hex;

/**
 *
 * Util class for methods working with byte array.
 *
 */
public final class ByteArray {
	private ByteArray() {
	}

	/**
	 * Returns a new byte array from given byte array, starting at start index
	 * with the size of the length parameter. Byte array given as parameter
	 * stays untouched.
	 *
	 * @param bytes
	 *            original byte array
	 * @param startIndex
	 *            beginning index, inclusive
	 * @param length
	 *            how many bytes should be in the sub-array
	 * @return a new byte array that is a sub-array of the original
	 */
	public static byte[] subByte(final byte[] bytes, int startIndex, int length) {
		if (bytes.length == 0 || length < 0 || length > bytes.length || startIndex < 0 || startIndex > bytes.length || startIndex + length > bytes.length) {
			throw new IllegalArgumentException("Cannot create subByte, invalid arguments: Length: " + length + " startIndex: " + startIndex);
		}
		final byte[] res = new byte[length];
		System.arraycopy(bytes, startIndex, res, 0, length);
		return res;
	}

	/**
	 * Converts byte array to Integer. If there are less bytes in the array as
	 * required (4), the method will push adequate number of zero bytes
	 * prepending given byte array.
	 *
	 * @param bytes
	 *            array to be converted to int
	 * @return int
	 */
	public static int bytesToInt(byte[] bytes) {
		if (bytes.length > Integer.SIZE / 8) {
			throw new IllegalArgumentException("Cannot convert bytes to integer. Byte array too big.");
		}
		byte[] res = new byte[Integer.SIZE / 8];
		if (bytes.length != Integer.SIZE / 8) {
			System.arraycopy(bytes, 0, res, Integer.SIZE / 8 - bytes.length, bytes.length);
		} else {
			res = bytes;
		}
		final ByteBuffer buff = ByteBuffer.wrap(res);
		return buff.getInt();
	}

	/**
	 * Converts byte array to long. If there are less bytes in the array as
	 * required (Long.Size), the method will push adequate number of zero bytes
	 * prepending given byte array.
	 *
	 * @param bytes
	 *            array to be converted to long
	 * @return long
	 */
	public static long bytesToLong(byte[] bytes) {
		if (bytes.length > Long.SIZE / 8) {
			throw new IllegalArgumentException("Cannot convert bytes to long.Byte array too big.");
		}
		byte[] res = new byte[Long.SIZE / 8];
		if (bytes.length != Long.SIZE / 8) {
			System.arraycopy(bytes, 0, res, Long.SIZE / 8 - bytes.length, bytes.length);
		} else {
			res = bytes;
		}
		final ByteBuffer buff = ByteBuffer.wrap(res);
		return buff.getLong();
	}

	/**
	 * Converts byte array to float IEEE 754 format. If there are less bytes in
	 * the array as required (Float.Size), the method will push adequate number
	 * of zero bytes prepending given byte array.
	 *
	 * @param bytes
	 *            array to be converted to float
	 * @return float
	 */
	public static float bytesToFloat(byte[] bytes) {
		if (bytes.length > Float.SIZE / 8) {
			throw new IllegalArgumentException("Cannot convert bytes to float.Byte array too big.");
		}
		byte[] res = new byte[Float.SIZE / 8];
		if (bytes.length != Float.SIZE / 8) {
			System.arraycopy(bytes, 0, res, Float.SIZE / 8 - bytes.length, bytes.length);
		} else {
			res = bytes;
		}
		final ByteBuffer buff = ByteBuffer.wrap(res);
		return buff.getFloat();
	}

	/**
	 * Cuts 'count' number of bytes from the beginning of given byte array.
	 *
	 * @param bytes
	 *            array to be cut, cannot be null
	 * @param count
	 *            how many bytes needed to be cut, needs to be > 0
	 * @return bytes array without first 'count' bytes
	 */
	public static byte[] cutBytes(byte[] bytes, int count) {
		if (bytes.length == 0 || count > bytes.length || count <= 0) {
			throw new IllegalArgumentException("Cannot cut bytes, invalid arguments: Count: " + count + " bytes.length: " + bytes.length);
		}
		return Arrays.copyOfRange(bytes, count, bytes.length);
	}

	/**
	 * Parse byte to bits, from the leftmost bit.
	 *
	 * @param b
	 *            byte to be parsed
	 * @return array of booleans with size of 8
	 */
	public static boolean[] parseBits(byte b) {
		final boolean[] bits = new boolean[Byte.SIZE];
		int j = 0;
		for (int i = Byte.SIZE - 1; i >= 0; i--) {
			bits[j] = ((b & (1 << i)) != 0);
			j++;
		}
		return bits;
	}

	/**
	 * Parses array of bytes to BitSet, from left most bit.
	 *
	 * @param bytes
	 *            array of bytes to be parsed
	 * @return BitSet with length = bytes.length * Byte.SIZE
	 */
	public static BitSet bytesToBitSet(byte[] bytes) {
		final BitSet bitSet = new BitSet(bytes.length * Byte.SIZE);
		for (int bytes_iter = 0; bytes_iter < bytes.length; bytes_iter++) {
			final int offset = bytes_iter * Byte.SIZE;
			for (int byte_iter = Byte.SIZE - 1; byte_iter >= 0; byte_iter--) {
				bitSet.set(offset + (Byte.SIZE - byte_iter - 1), (bytes[bytes_iter] & 1 << (byte_iter)) != 0);
			}
		}
		return bitSet;
	}

	/**
	 * Parses BitSet to bytes, from most left bit.
	 *
	 * @param bitSet
	 *            BitSet to be parsed
	 * @param returnedLength
	 *            Length of returned array. Overlapping flags are truncated.
	 * @return parsed array of bytes with length of bitSet.length / Byte.SIZE
	 */
	public static byte[] bitSetToBytes(BitSet bitSet, int returnedLength) {
		final byte[] bytes = new byte[returnedLength];

		for (int bytes_iter = 0; bytes_iter < bytes.length; bytes_iter++) {
			final int offset = bytes_iter * Byte.SIZE;

			for (int byte_iter = Byte.SIZE - 1; byte_iter >= 0; byte_iter--) {
				bytes[bytes_iter] |= (bitSet.get(offset + (Byte.SIZE - byte_iter - 1)) ? 1 << byte_iter : 0);
			}
		}
		return bytes;
	}

	/**
	 * Parses file to array of bytes
	 *
	 * @param name
	 *            path to file to by parsed
	 * @return parsed array of bytes
	 */
	public static byte[] fileToBytes(String name) throws IOException {
		final File file = new File(name);
		int offset = 0;
		int numRead = 0;

		if (file.length() > Integer.MAX_VALUE) {
			throw new IOException("Too large file to load in byte array.");
		}

		final FileInputStream fin = new FileInputStream(file);
		final byte[] byteArray = new byte[(int) file.length()];

		while (offset < byteArray.length && (numRead = fin.read(byteArray, offset, byteArray.length - offset)) >= 0) {
			offset += numRead;
		}

		if (fin != null) {
			fin.close();
		}

		return byteArray;
	}

	/**
	 * Parses integer to array of bytes
	 *
	 * @param num
	 *            integer to be parsed
	 * @return parsed array of bytes with length of Integer.SIZE/Byte.SIZE
	 */
	public static byte[] intToBytes(int num) {
		final ByteBuffer bytesBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		bytesBuffer.putInt(num);

		return bytesBuffer.array();
	}

	/**
	 * Parses integer to array of bytes
	 *
	 * @param num
	 *            integer to be parsed
	 * @return parsed array of bytes with length of Long.SIZE/Byte.SIZE
	 */
	public static byte[] longToBytes(long num) {
		final ByteBuffer bytesBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		bytesBuffer.putLong(num);

		return bytesBuffer.array();
	}

	/**
	 * Copies range of bits from passed byte and align to right.<br/>
	 *
	 * @param src
	 *            source byte to copy from
	 * @param fromBit
	 *            bit from which will copy (inclusive) - numbered from 0
	 * @param length
	 *            of bits to by copied - <1,8>
	 * @return copied value aligned to right
	 */
	public static byte copyBitsRange(byte src, int fromBit, int length) {
		if (fromBit < 0 | fromBit > Byte.SIZE - 1 | length < 1 | length > Byte.SIZE) {
			throw new IllegalArgumentException("fromBit or toBit is out of range.");
		}
		if (fromBit + length > Byte.SIZE) {
			throw new IllegalArgumentException("Out of range.");
		}

		byte retByte = 0;
		int retI = 0;

		for (int i = fromBit + length - 1; i >= fromBit; i--) {

			if ((src & 1 << (Byte.SIZE - i - 1)) != 0) {
				retByte |= 1 << retI;
			}

			retI++;
		}

		return retByte;
	}

	/**
	 * Copies whole source byte array to destination from offset.<br/>
	 * Length of src can't be bigger than dest length minus offset
	 *
	 * @param src
	 *            byte[]
	 * @param dest
	 *            byte[]
	 * @param offset
	 *            int
	 */
	public static void copyWhole(byte[] src, byte[] dest, int offset) {
		if (dest.length - offset < src.length) {
			throw new ArrayIndexOutOfBoundsException("Can't copy whole array.");
		}

		System.arraycopy(src, 0, dest, offset, src.length);
	}

	/**
	 * Convert array of bytes to java short.<br/>
	 * Size can't be bigger than size of short in bytes.
	 *
	 * @param bytes
	 *            byte[]
	 * @return array of bytes
	 */
	public static short bytesToShort(byte[] bytes) {
		if (bytes.length > Short.SIZE / Byte.SIZE) {
			throw new IllegalArgumentException("Cannot convert bytes to short. Byte array too big.");
		}
		byte[] res = new byte[Short.SIZE / Byte.SIZE];
		if (bytes.length != Short.SIZE / Byte.SIZE) {
			System.arraycopy(bytes, 0, res, Integer.SIZE / Byte.SIZE - bytes.length, bytes.length);
		} else {
			res = bytes;
		}
		final ByteBuffer buff = ByteBuffer.wrap(res);
		return buff.getShort();
	}

	/**
	 * Convert short java representation to array of bytes.
	 *
	 * @param num
	 *            short
	 * @return short represented as array of bytes
	 */
	public static byte[] shortToBytes(short num) {
		final ByteBuffer bytesBuffer = ByteBuffer.allocate(Short.SIZE / Byte.SIZE);
		bytesBuffer.putShort(num);

		return bytesBuffer.array();
	}

	/**
	 * Convert float java representation to array of bytes.
	 *
	 * @param num
	 *            float
	 * @return float represented as array of bytes
	 */
	public static byte[] floatToBytes(float num) {
		final ByteBuffer bytesBuffer = ByteBuffer.allocate(Float.SIZE / Byte.SIZE);
		bytesBuffer.putFloat(num);

		return bytesBuffer.array();
	}

	/**
	 * Pretty print array of bytes as hex encoded string with 16 bytes per line.
	 * Each byte is separated by space, after first 8 bytes there are 2 spaces
	 * instead of one.
	 */
	public static String bytesToHexString(byte[] array) {
		return bytesToHexString(array, 16, " ", 8, " ");
	}

	/**
	 * Pretty-print an array of bytes as hex-encoded string. Separate them
	 * with specified separator.
	 */
	public static String toHexString(final byte[] array, final String separator) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(Hex.encodeHexString(new byte[] { array[i] }));
			if (i + 1 != array.length)
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Convert array of bytes to hexadecimal String.
	 *
	 * @param array
	 * @param bytesOnLine
	 *            number of bytes that should by displayed in one line
	 * @param byteSeparator
	 *            string that will be placed after each byte
	 * @param wordCount
	 *            number of bytes that make a 'word' (group of bytes)
	 * @param wordSeparator
	 *            string that will be placed after each word
	 * @return Hexadecimal string representation of given byte array
	 */
	public static String bytesToHexString(byte[] array, int bytesOnLine, String byteSeparator, int wordCount, String wordSeparator) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(Hex.encodeHexString(new byte[] { array[i] }));
			if ((i + 1) % bytesOnLine == 0) {
				sb.append("\n");
			} else {
				sb.append(byteSeparator);
				if ((i + 1) % wordCount == 0) {
					sb.append(wordSeparator);
				}
			}

		}
		return sb.toString();
	}

	/**
	 * Decodes bytes to human readable UTF-8 string. If bytes are not valid
	 * UTF-8, they are represented as raw binary.
	 *
	 * @param bytes
	 *            bytes to be decoded to string
	 * @return String representation of passed bytes
	 */
	public static String bytesToHRString(byte[] bytes) {
		try {
			return Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (final CharacterCodingException e) {
			return Arrays.toString(bytes);
		}
	}

	/**
	 * Searches for byte sequence in given array. Returns the index of first occurrence of
	 * this sequence (where it starts).
	 * @param bytes byte array where to search for sequence
	 * @param sequence to be searched in given byte array
	 * @return -1 if the sequence could not be found in given byte array
	 * 			int index of first occurrence of the sequence in bytes
	 */
	public static int findByteSequence(byte[] bytes, byte[] sequence) {
		if (bytes.length < sequence.length)
			throw new IllegalArgumentException("Sequence to be found is longer than the given byte array.");
		if (bytes.length == sequence.length)
			if (Arrays.equals(bytes, sequence))
				return 0;
			else
				return -1;
		int j = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == sequence[j]) {
				j++;
				if (j == sequence.length)
					return i - j + 1;
			} else
				j = 0;
		}
		return -1;
	}

	private static final byte maskBits[] = new byte[] { 0, -128, -64, -32, -16, -8, -4, -2 };
	public static final byte[] maskBytes(final byte[] original,
			final int bits) {
		if (original.length * 8 < bits)
			throw new IllegalArgumentException("Attempted to apply invalid mask (too long)");

		final int needbytes = (bits +7) / 8;
		// We need to have a new copy of the underlying byte array, so that
		// the original bytes stay untouched
		final byte[] bytes = Arrays.copyOf(original, original.length);

		final int needmask = bits % 8;
		if (needmask != 0)
			bytes[needbytes-1] &= maskBits[needmask];

		// zero-out  the rest of the bytes
		for (int i = needbytes ; i < bytes.length; i++) {
			bytes[i] = 0;
		}
		return bytes;
	}
}
