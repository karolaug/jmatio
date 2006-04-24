package com.jmatio.io;

import com.jmatio.common.MatDataTypes;

class MatTag
{

    public int type;
    public int size;
    public int padding;
    public boolean compressed;
    public byte[] data;
    
    
    public MatTag(int type, int size, boolean compressed)
    {
        setPadding();
    }

    protected void setPadding()
    {
        //data not packed in the tag
        if ( !compressed )
        {    
            int b;
            padding = (b = ((size/sizeOf()%(8/sizeOf())))*sizeOf()) != 0 ? 8-b : 0;
        }
        else //data _packed_ in the tag (compressed)
        {
//            int b;
//            padding = (b = ( 4-((size/sizeOf()%(4/sizeOf())))*sizeOf() )) != 0 ? 8-b : 0;
            padding = 4-((size/sizeOf()%(4/sizeOf())))*sizeOf();
        }
        
    }
    public String toString()
    {
        String s;
        
        s = "[tag: " + typeToString(type) + " size: " + size + " padding: " + padding + "]";
        
        return s;
    }
    public int sizeOf()
    {
        return MatDataTypes.sizeOf(type);
    }
    
    public static String typeToString(int type)
    {
        String s;
        switch (type)
        {
            case MatDataTypes.miUNKNOWN:
                s = "unknown";
                break;
            case MatDataTypes.miINT8:
                s = "int8";
                break;
            case MatDataTypes.miUINT8:
                s = "uint8";
                break;
            case MatDataTypes.miINT16:
                s = "int16";
                break;
            case MatDataTypes.miUINT16:
                s = "uint16";
                break;
            case MatDataTypes.miINT32:
                s = "int32";
                break;
            case MatDataTypes.miUINT32:
                s = "uint32";
                break;
            case MatDataTypes.miSINGLE:
                s = "single";
                break;
            case MatDataTypes.miDOUBLE:
                s = "double";
                break;
            case MatDataTypes.miINT64:
                s = "int64";
                break;
            case MatDataTypes.miUINT64:
                s = "uint64";
                break;
            case MatDataTypes.miMATRIX:
                s = "matrix";
                break;
            case MatDataTypes.miCOMPRESSED:
                s = "compressed";
                break;
            case MatDataTypes.miUTF8:
                s = "uft8";
                break;
            case MatDataTypes.miUTF16:
                s = "utf16";
                break;
            case MatDataTypes.miUTF32:
                s = "utf32";
                break;
            default:
                s = "unknown";
        }
        return s;
    }
}
