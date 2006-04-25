package com.jmatio.io;

import com.jmatio.common.MatDataTypes;

/**
 * 
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
class MatTag
{
    public int type;
    public int size;
    public int padding;
    public boolean compressed;
    
    /**
     * @param type
     * @param size
     * @param compressed
     */
    public MatTag(int type, int size, boolean compressed)
    {
        this.type = type;
        this.size = size;
        setPadding();
    }

    /**
     * Calculate padding
     */
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
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s;
        
        s = "[tag: " + MatDataTypes.typeToString(type) + " size: " + size + " padding: " + padding + "]";
        
        return s;
    }
    /**
     * Get size of single data in this tag.
     * 
     * @return - number of bytes for single data
     */
    public int sizeOf()
    {
        return MatDataTypes.sizeOf(type);
    }
    
}
