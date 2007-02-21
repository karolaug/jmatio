package com.jmatio.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import sun.reflect.Reflection;

import com.jmatio.common.MatDataTypes;
import com.jmatio.types.ByteStorageSupport;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLNumericArray;

/**
 * MAT-file input stream class. 
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
class MatFileInputStream
{
    private int type;
    private ByteBuffer buf;
    
    /**
     * Attach MAT-file input stream to <code>InputStream</code>
     * 
     * @param is - input stream
     * @param type - type of data in the stream
     * @see com.jmatio.common.MatDataTypes
     */
    public MatFileInputStream( ByteBuffer buf, int type )
    {
        this.type = type;
        this.buf = buf;
    }
    
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>int</code>.
     * 
     * @return
     * @throws IOException
     */
    public int readInt() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (int)( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return (int) buf.get();
            case MatDataTypes.miUINT16:
                return (int)( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (int) buf.getShort();
            case MatDataTypes.miUINT32:
                return (int)( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (int) buf.getInt();
            case MatDataTypes.miUINT64:
                return (int) buf.getLong();
            case MatDataTypes.miDOUBLE:
                return (int) buf.getDouble();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>char</code>.
     * 
     * @return - char
     * @throws IOException
     */
    public char readChar() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (char)( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return (char) buf.get();
            case MatDataTypes.miUINT16:
                return (char)( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (char) buf.getShort();
            case MatDataTypes.miUINT32:
                return (char)( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (char) buf.getInt();
            case MatDataTypes.miDOUBLE:
                return (char) buf.getDouble();
            case MatDataTypes.miUTF8:
                return (char) buf.get();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>double</code>.
     * 
     * @return - double
     * @throws IOException
     */
    public double readDouble() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (double)( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return (double) buf.get();
            case MatDataTypes.miUINT16:
                return (double)( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (double) buf.getShort();
            case MatDataTypes.miUINT32:
                return (double)( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (double) buf.getInt();
            case MatDataTypes.miDOUBLE:
                return (double) buf.getDouble();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }

    public byte readByte()
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (byte)( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return (byte) buf.get();
            case MatDataTypes.miUINT16:
                return (byte)( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (byte) buf.getShort();
            case MatDataTypes.miUINT32:
                return (byte)( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (byte) buf.getInt();
            case MatDataTypes.miDOUBLE:
                return (byte) buf.getDouble();
            case MatDataTypes.miUTF8:
                return (byte) buf.get();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }

    public ByteBuffer readToByteBuffer(ByteBuffer dest, int elements, ByteStorageSupport storage) throws IOException
    {
        
        int bytesAllocated = storage.getBytesAllocated();
        int size = elements * storage.getBytesAllocated();
        
        if ( MatDataTypes.sizeOf(type) == bytesAllocated )
        {
            int bufMaxSize = 1024;
            int bufSize = buf.remaining() < bufMaxSize ? buf.remaining() : bufMaxSize;
            int bufPos = buf.position();
            
            byte[] tmp = new byte[ bufSize ];
            
            while ( dest.remaining() > 0 )
            {
                int length = dest.remaining() > tmp.length ? tmp.length : dest.remaining();
                buf.get( tmp, 0, length );
                dest.put( tmp, 0, length );
            }
            buf.position( bufPos + size );
        }
        else
        {
            Class clazz = storage.getStorageClazz();
            while ( dest.remaining() > 0 )
            {
                if ( clazz.equals( Double.class) )
                {
                    dest.putDouble( readDouble() );
                    continue;
                }
                if ( clazz.equals( Byte.class) )
                {
                    dest.put( readByte() );
                    continue;
                }
                if ( clazz.equals( Integer.class) )
                {
                    dest.putInt( readInt() );
                    continue;
                }
                throw new RuntimeException("Not supported buffer reader for " + clazz );
            }
        }
        dest.rewind();
        return dest;
    }
    

}
