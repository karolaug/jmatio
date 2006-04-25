package com.jmatio.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jmatio.common.MatDataTypes;

/**
 * MAT-file input stream class. 
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatFileInputStream extends InputStream
{
    private int type;
    private InputStream is;
    private boolean byteSwap;
    
    /**
     * Attach MAT-file input stream to <code>InputStream</code>
     * 
     * @param is - input stream
     * @param type - type of data in the stream
     * @param byteSwap - set to <code>true</code> if data requires swapping for
     *                   correct interpretation
     * @see com.jmatio.common.MatDataTypes
     */
    public MatFileInputStream( InputStream is, int type, boolean byteSwap )
    {
        this.type = type;
        this.is = is;
        this.byteSwap = byteSwap;
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
        //read bytes from stream. buffer size is determined by data type
        byte[] buffer = new byte[ sizeOf() ];
        is.read(buffer);
        
        //swap if necesary
        if ( byteSwap )
        {
            swapBuffer(buffer);
        }
        
        //convert data to double
        DataInputStream _dis = new DataInputStream(new ByteArrayInputStream(buffer) );
        
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (int)( _dis.readByte() & 0xFF);
            case MatDataTypes.miINT8:
                return (int)_dis.readByte();
            case MatDataTypes.miUINT16:
                return (int)( _dis.readShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (int)_dis.readShort();
            case MatDataTypes.miUINT32:
                return (int)( _dis.readInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (int)_dis.readInt();
            case MatDataTypes.miDOUBLE:
                return (int)_dis.readDouble();
            default:
                return (int)_dis.readByte();
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
        //read bytes from stream. buffer size is determined by data type
        byte[] buffer = new byte[ sizeOf() ];
        is.read(buffer);
        //swap if necesary
        if ( byteSwap )
        {
            swapBuffer(buffer);
        }
        
        //convert data to double
        DataInputStream _dis = new DataInputStream(new ByteArrayInputStream(buffer) );
        
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (char)( _dis.readByte() & 0xFF);
            case MatDataTypes.miINT8:
                return (char)_dis.readByte();
            case MatDataTypes.miUINT16:
                return (char)( _dis.readShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (char)_dis.readShort();
            case MatDataTypes.miUINT32:
                return (char)( _dis.readInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (char)_dis.readInt();
            case MatDataTypes.miDOUBLE:
                return (char)_dis.readDouble();
            default:
                return (char)_dis.readByte();
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
        //read bytes from stream. buffer size is determined by data type
        byte[] buffer = new byte[ sizeOf() ];
        is.read(buffer);
        //swap if necesary
        if ( byteSwap )
        {
            swapBuffer(buffer);
        }
        
        //convert data to double
        DataInputStream _dis = new DataInputStream(new ByteArrayInputStream(buffer) );
        
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (double)( _dis.readByte() & 0xFF);
            case MatDataTypes.miINT8:
                return (double)_dis.readByte();
            case MatDataTypes.miUINT16:
                return (double)( _dis.readShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (double)_dis.readShort();
            case MatDataTypes.miUINT32:
                return (double)( _dis.readInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (double)_dis.readInt();
            case MatDataTypes.miDOUBLE:
                return _dis.readDouble();
            default:
                return _dis.readDouble();
        }
    }
    /**
     * Swap byte buffer
     * 
     * @param buffer
     */
    private void swapBuffer(byte[] buffer)
    {
        if ( buffer.length == 1 ) 
        {
            return;
        }
        if ( buffer.length%2 != 0 )
        {
            throw new IllegalArgumentException("Byte array length must by multiplication of 2");
        }
        for(int i = 0; i < buffer.length/2; i++)
        {
            int src = i;
            int dest = buffer.length - 1 - i;
            
            byte tmp = buffer[dest];
            buffer[dest] = buffer[src];
            buffer[src] = tmp;
        }
    }
    /**
     * Get size of (number of bytes) for data in this stream.
     * 
     * @return - size of data for this stream type
     */
    private int sizeOf()
    {
        return MatDataTypes.sizeOf( type );
    }
    
    @Override
    public int read() throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
