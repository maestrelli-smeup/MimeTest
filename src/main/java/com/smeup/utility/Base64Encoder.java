// Copyright (C) 1999-2002 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved.  Use of this class is limited.
// Please see the LICENSE for more information.
package com.smeup.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Encoder;

/**
 * A class to encode Base64 streams and strings. See RFC 1521 section 5.2 for
 * details of the Base64 algorithm.
 * <p>
 * This class can be used for encoding strings: <blockquote>
 * 
 * <pre>
 * String unencoded = &quot;webmaster:try2gueSS&quot;;
 * String encoded = Base64Encoder.encode(unencoded);
 * </pre>
 * 
 * </blockquote> or for encoding streams: <blockquote>
 * 
 * <pre>
 * OutputStream out = new Base64Encoder(System.out);
 * </pre>
 * 
 * </blockquote>
 * 
 * @author <b>Jason Hunter</b>, Copyright &#169; 2000
 * @version 1.2, 2002/11/01, added encode(byte[]) method to better handle binary
 *          data (thanks to Sean Graham)
 * @version 1.1, 2000/11/17, fixed bug with sign bit for char values
 * @version 1.0, 2000/06/11
 */
public class Base64Encoder extends FilterOutputStream
{
	public static String CHARSET_NAME="8859_1";
	
	private static final char[] CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
	private int iCharCount;
	private int iCarryOver;

	/**
     * Constructs a new Base64 encoder that writes output to the given
     * OutputStream.
     * 
     * @param aOut the output stream
     */
	public Base64Encoder(OutputStream aOut)
	{
		super(aOut);
	}

	/**
     * Writes the given byte to the output stream in an encoded form.
     * 
     * @exception IOException if an I/O error occurs
     */
	public void write(int aByte) throws IOException
	{
		// Take 24-bits from three octets, translate into four encoded chars
		// Break lines at 76 chars
		// If necessary, pad with 0 bits on the right at the end
		// Use = signs as padding at the end to ensure encodedLength % 4 == 0
		// Remove the sign bit,
		// thanks to Christian Schweingruber <chrigu@lorraine.ch>
		if (aByte < 0)
		{
			aByte += 256;
		}
		// First byte use first six bits, save last two bits
		if (iCharCount % 3 == 0)
		{
			int vLookup = aByte >> 2;
			iCarryOver = aByte & 3; // last two bits
			out.write(CHARS[vLookup]);
		}
		// Second byte use previous two bits and first four new bits,
		// save last four bits
		else if (iCharCount % 3 == 1)
		{
			int vLookup = ((iCarryOver << 4) + (aByte >> 4)) & 63;
			iCarryOver = aByte & 15; // last four bits
			out.write(CHARS[vLookup]);
		}
		// Third byte use previous four bits and first two new bits,
		// then use last six new bits
		else if (iCharCount % 3 == 2)
		{
			int cLookup = ((iCarryOver << 2) + (aByte >> 6)) & 63;
			out.write(CHARS[cLookup]);
			cLookup = aByte & 63; // last six bits
			out.write(CHARS[cLookup]);
			iCarryOver = 0;
		}
		iCharCount++;
		// Add newline every 76 output chars (that's 57 input chars)
		if (iCharCount % 57 == 0)
		{
			out.write('\n');
		}
	}

	/**
     * Writes the given byte array to the output stream in an encoded form.
     * 
     * @param b the data to be written
     * @param aOff the start offset of the data
     * @param aLen the length of the data
     * @exception IOException if an I/O error occurs
     */
	public void write(byte[] aBuf, int aOff, int aLen) throws IOException
	{
		// This could of course be optimized
		for (int i = 0; i < aLen; i++)
		{
			write(aBuf[aOff + i]);
		}
	}

	/**
     * Closes the stream, this MUST be called to ensure proper padding is
     * written to the end of the output stream.
     * 
     * @exception IOException if an I/O error occurs
     */
	public void close() throws IOException
	{
		// Handle leftover bytes
		if (iCharCount % 3 == 1)
		{ // one leftover
			int lookup = (iCarryOver << 4) & 63;
			out.write(CHARS[lookup]);
			out.write('=');
			out.write('=');
		}
		else if (iCharCount % 3 == 2)
		{ // two leftovers
			int vLookup = (iCarryOver << 2) & 63;
			out.write(CHARS[vLookup]);
			out.write('=');
		}
		super.close();
	}

	/**
     * Returns the encoded form of the given unencoded string. The encoder uses
     * the ISO-8859-1 (Latin-1) encoding to convert the string to bytes. For
     * greater control over the encoding, encode the string to bytes yourself
     * and use encode(byte[]).
     * 
     * @param aUnencoded the string to encode
     * @return the encoded form of the unencoded string
     */
	public static String encode(String aUnencoded, String aCharset)
	{
		byte[] aBytes = null;
		try
		{
			aBytes = aUnencoded.getBytes(aCharset);
		}
		catch (UnsupportedEncodingException ignored)
		{
		}
		return encode(aBytes, aCharset);
	}

	/**
     * Returns the encoded form of the given unencoded string.
     * 
     * @param unencoded the string to encode
     * @return the encoded form of the unencoded string
     */
	public static String encode(byte[] aBytes, String aCharset)
	{
		ByteArrayOutputStream vOut = new ByteArrayOutputStream((int) (aBytes.length * 1.37));
		Base64Encoder vEncodedOut = new Base64Encoder(vOut);
		try
		{
			vEncodedOut.write(aBytes);
			vEncodedOut.close();
			return vOut.toString(aCharset);
		}
		catch (IOException ignored)
		{
			return null;
		}
	}

	public static byte[] encodeFile_old(File aInputFile) throws IOException
	{
		byte[] vRet= new byte[] {};
		Base64Encoder vEncoder = null;
		BufferedInputStream vIn = null;
		try
		{
			OutputStream vOutput = new OutputStream()
		    {
		        private StringBuilder vStringBuilder = new StringBuilder();
		        @Override
		        public void write(int aB) throws IOException {
		            this.vStringBuilder.append((char) aB );
		        }
		        public String toString(){
		            return this.vStringBuilder.toString();
		        }
		    };
			vEncoder = new Base64Encoder(vOutput);
			File vFile= aInputFile;
			vIn = new BufferedInputStream(new FileInputStream(vFile));
			byte[] vBuf = new byte[4 * 1024]; // 4K buffer
			int vBytesRead;
			while ((vBytesRead = vIn.read(vBuf)) != -1)
			{
				vEncoder.write(vBuf, 0, vBytesRead);
			}
			System.out.println(vOutput.toString());
			vRet= vOutput.toString().getBytes("UTF-8");
		}
		finally
		{
			if (vIn != null) vIn.close();
			if (vEncoder != null) vEncoder.close();
		}
		return vRet;
	}

	public static byte[] encodeFile(File aInputFile) throws IOException
	{
		byte[] vRet= new byte[] {};
		BASE64Encoder vEncoder = null;
		BufferedInputStream vIn = null;
		try
		{
			OutputStream vOutput = new OutputStream()
		    {
		        private StringBuilder vStringBuilder = new StringBuilder();
		        @Override
		        public void write(int aB) throws IOException {
		            this.vStringBuilder.append((char) aB );
		        }
		        public String toString(){
		            return this.vStringBuilder.toString();
		        }
		    };
			vEncoder = new BASE64Encoder();
			File vFile= aInputFile;
			vIn = new BufferedInputStream(new FileInputStream(vFile));
			vEncoder.encodeBuffer(vIn, vOutput);
			vRet= vOutput.toString().getBytes("UTF-8");
		}
		finally
		{
			if (vIn != null) vIn.close();
			//if (vEncoder != null) vEncoder;
		}
		return vRet;
	}
	
	public static void encodeFile(File aInputFile, File aOutputFile) throws IOException
	{
		Base64Encoder vEncoder = null;
		BufferedInputStream vIn = null;
		try
		{
//			OutputStream vOutput = new OutputStream()
//		    {
//		        private StringBuilder vStringBuilder = new StringBuilder();
//		        @Override
//		        public void write(int aB) throws IOException {
//		            this.vStringBuilder.append((char) aB );
//		        }
//		        public String toString(){
//		            return this.vStringBuilder.toString();
//		        }
//		    };
//			vEncoder = new Base64Encoder(vOutput);
//			File vFile= aInputFile;
//			vIn = new BufferedInputStream(new FileInputStream(vFile));
//			byte[] vBuf = new byte[4 * 1024]; // 4K buffer
//			int vBytesRead;
//			while ((vBytesRead = vIn.read(vBuf)) != -1)
//			{
//				vEncoder.write(vBuf, 0, vBytesRead);
//			}
//			System.out.println(vOutput.toString());
//			File vOutFile= aOutputFile;
			byte[] vArray= encodeFile(aInputFile);
			BufferedOutputStream vWriter= new BufferedOutputStream(new FileOutputStream(aOutputFile));
			vWriter.write(vArray);
			vWriter.flush();
			vWriter.close();
		}
		finally
		{
			if (vIn != null) vIn.close();
			if (vEncoder != null) vEncoder.close();
		}
	}

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println("Usage: Smeup.smec_s.utility.Base64Encoder fileToEncode fileEncoded");
//			Smeup.smec_s.utility.LogManager.error("Usage: Smeup.smec_s.utility.Base64Utility fileToEncode");
			System.exit(-1);
		}
		try
		{
			encodeFile(new File(args[0]), new File(args[1]));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}
}
