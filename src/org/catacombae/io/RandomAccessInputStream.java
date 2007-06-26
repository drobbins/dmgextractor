package org.catacombae.io;

import java.io.*;

/**
 * This class subclasses java.io.InputStream to transform a part of a RandomAccessStream
 * into an ordinary InputStream.
 */
public class RandomAccessInputStream extends InputStream {
    private final SynchronizedRandomAccessStream ras;
    private long streamPos;
    private final long endPos;
    private final byte[] tmp = new byte[1];
    
    /** length == -1 means length == ras.length() */
    public RandomAccessInputStream(SynchronizedRandomAccessStream ras, long offset, long length) {
	try {
	    long rasLength = ras.length();
	    if(length == -1)
		length = rasLength;
	    if(offset > rasLength || offset < 0)
		throw new IllegalArgumentException("offset out of bounds (offset=" + offset + " length=" + length + ")");
	    if(length > rasLength-offset || length < 0)
		throw new IllegalArgumentException("length out of bounds (offset=" + offset + " length=" + length + ")");
	    this.ras = ras;
	    this.streamPos = offset;
	    this.endPos = offset+length;
	} catch(Exception e) { throw new RuntimeException(e); }
    }
    /**
     * Constructs an InputStream that covers the data contained in the underlying
     * RandomAccessStream, from the beginning, to the end. */
    public RandomAccessInputStream(SynchronizedRandomAccessStream ras) {
	this(ras, 0, -1);
    }
    
    public int available() throws IOException {
	long remaining = endPos - streamPos;
	if(remaining > Integer.MAX_VALUE)
	    return Integer.MAX_VALUE;
	else if(remaining < Integer.MIN_VALUE)
	    return Integer.MIN_VALUE;
	else
	    return (int)remaining;
    }
    /** Does not do anything. The underlying SynchronizedRandomAccessStream might be in use by others. */
    public void close() throws IOException {}
    
    /** Not supported, not implemented (not needed). */
    public void mark(int readlimit) { throw new RuntimeException("Not supported"); }
    /** Not supported, not implemented (not needed). */
    public boolean markSupported() { return false; }
    public int read() throws IOException {
	final byte[] tmp = new byte[1];
	int res = read(tmp, 0, 1);
	if(res == 1)
	    return tmp[0] & 0xFF;
	else
	    return -1;
    }
    public int read(byte[] b) throws IOException { return read(b, 0, b.length); }
    public int read(byte[] b, int off, int len) throws IOException {
	int bytesToRead = (int)((streamPos+len > endPos)?endPos-streamPos:len);
	if(bytesToRead == 0)
	    return -1;
	int res = ras.readFrom(streamPos, b, off, bytesToRead);
	if(res > 0) streamPos += res;
	return res;
    }
    /** Not supported, not implemented (not needed). */
    public void reset() throws IOException { throw new RuntimeException("Not supported"); }
    public long skip(long n) throws IOException {
	long res = ras.skipFrom(streamPos, n);
	if(res > 0) streamPos += res;
	return res;	
    }
}