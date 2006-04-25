package com.jmatio.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.log4j.Logger;

import com.jmatio.common.MatDataTypes;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;

/**
 * MAT-file reader. Reads MAT-file into <code>MLArray</code> objects.
 * 
 * Usage:
 * <pre><code>
 * //read in the file
 * MatFileReader mfr = new MatFileReader( "mat_file.mat" );
 * 
 * //get array of a name "my_array" from file
 * MLArray mlArrayRetrived = mfr.getMLArray( "my_array" );
 * 
 * //or get the collection of all arrays that were stored in the file
 * Map content = mfr.getContent();
 * </pre></code>
 * 
 * @see com.jmatio.io.MatFileFilter
 * @author Wojciech Gradkowski (<a href="mailto:wgradkowski@gmail.com">wgradkowski@gmail.com</a>)
 */
public class MatFileReader
{
    private static final Logger logger = Logger.getLogger( MatFileReader.class.getName() );
    
    /**
     * MAT-file header
     */
    private MatFileHeader matFileHeader;
    /**
     * Container for red <code>MLArray</code>s
     */
    private Map<String, MLArray> data;
    /**
     * When set to true, byte-swapping is required to interpret data correctly
     */
    private boolean byteSwap;
    
    /**
     * Array name filter
     */
    private MatFileFilter filter;
    
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file 
     * from location given as <code>fileName</code>.
     * 
     * @param fileName - a path to file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MatFileReader(String fileName) throws FileNotFoundException, IOException
    {
        this ( fileName, new MatFileFilter() );
    }
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file 
     * from location given as <code>fileName</code>.
     * 
     * Results are filtered by <code>MatFileFilter</code>. Arrays that do not meet
     * filter match condition will not be avalable in results.
     * 
     * @param fileName - MAT-file path
     * @param filter - array name filter
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MatFileReader(String fileName, MatFileFilter filter ) throws FileNotFoundException, IOException
    {
        this( new DataInputStream( new FileInputStream( new File(fileName) ) ), filter );
    }
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file 
     * from <code>file</code>.
     * 
     * @param file - MAT-file path
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MatFileReader(File file) throws FileNotFoundException, IOException
    {
        this ( new DataInputStream( new FileInputStream(file) ), new MatFileFilter() );
        
    }
    /**
     * Creates instance of <code>MatFileReader</code> and reads MAT-file 
     * from <code>InputStream</code>.
     * 
     * @param is - MAT-file data <code>InputStream</code>
     * @param filter - a filter for file contents
     * @throws IOException
     */
    private MatFileReader(InputStream is, MatFileFilter filter) throws IOException
    {
        this.filter = filter;
        data = new LinkedHashMap<String, MLArray>();
        
        readHeader(is);

        while ( is.available() > 0 )
        {
            readData( is );
        }
        
    }
    /**
     * Gets MAT-file header
     * 
     * @return - a <code>MatFileHeader</code> object
     */
    public MatFileHeader getMatFileHeader()
    {
        return matFileHeader;
    }
    /**
     * Returns list of <code>MLArray</code> objects that were inside MAT-file
     * 
     * @return - a <code>ArrayList</code>
     * @deprecated use <code>getContent</code> which returs a Map to provide 
     *             easier access to <code>MLArray</code>s contained in MAT-file
     */
    public ArrayList<MLArray> getData()
    {
        return new ArrayList<MLArray>( data.values() );
    }
    /**
     * Returns the value to which the red file maps the specified array name.
     * 
     * Returns <code>null</code> if the file contains no content for this name.
     * 
     * @param - array name
     * @return - the <code>MLArray</code> to which this file maps the specified name, 
     *           or null if the file contains no content for this name.
     */
    public MLArray getMLArray( String name )
    {
        return data.get( name );
    }
    /**
     * Returns a map of <code>MLArray</code> objects that were inside MAT-file.
     * 
     * MLArrays are mapped with MLArrays' names
     *  
     * @return - a <code>Map</code> of MLArrays mapped with theid names.
     */
    public Map<String, MLArray> getContent()
    {
        return data;
    }
    /**
     * Decompresses (inflates) bytes from input stream.
     * 
     * Stream marker is being set at +<code>numOfBytes</code> positon of the stream.
     * 
     * @param is - <code>InputStream</code>
     * @param numOfBytes - number of bytes to be red
     * @return - byte array of decompressed data
     * @throws IOException
     */
    private byte[] inflate(InputStream is, int numOfBytes) throws IOException
    {
        byte[] compressed = new byte[numOfBytes];
        is.read(compressed);
        
        logger.debug("Decompressing " + numOfBytes + " bytes");
        
        //get new inflater instance
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressed);
        
        byte[] result = new byte[128];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( baos );
        int i;
        try
        {
            do
            {
                i = decompresser.inflate(result);
                dos.write(result);
            }
            while ( i > 0 );
            
            logger.debug("Decompressed buffer length: " + baos.size() + " bytes");
        }
        catch ( DataFormatException e )
        {
            throw new MatlabIOException("Cound not decompress data: " + e );
        }
        finally
        {
            decompresser.end();
        }
        
        return baos.toByteArray();
        
    }
    
    private void readData(InputStream is) throws IOException
    {
        //read data
        ISMatTag tag = new ISMatTag(is);
        switch ( tag.type )
        {
            case MatDataTypes.miCOMPRESSED:
                //inflate and recur
                readData( new DataInputStream( new ByteArrayInputStream(inflate(is, tag.size) ) ) );
                break;
            case MatDataTypes.miMATRIX:
                byte[] buffer = new byte[tag.size];
                is.read( buffer );
                //read in the matrix
                MLArray element = readMatrix( new DataInputStream( new ByteArrayInputStream(buffer) ), true );
                if ( element != null )
                {
                    data.put( element.getName(), element );
                }
                break;
            default:
                throw new MatlabIOException("Incorrect data tag: " + tag);
                    
        }
    }
    /**
     * Reads miMATRIX from from input stream.
     * 
     * If reading was not finished (which is normal for filtered results)
     * returns null.
     * 
     * @param is - Input stream
     * @param isRoot - when <code>true</code> informs that if this is a top level matrix 
     * @return - <code>MLArray</code> or <code>null</code> if  matrix does not match <code>filter</code>
     * @throws IOException
     */
    private MLArray readMatrix(InputStream is, boolean isRoot ) throws IOException
    {
        //result
        MLArray mlArray;
        ISMatTag tag;
        
        //read flags
        int[] flags = readFlags(is);
        int attributes = flags[0];
        int nzmax = flags[1];
        int type = attributes & 0xff;
        
        //read Array dimension
        int[] dims = readDimension(is);
        
        //read arrayName
        String name = readName(is);
        
        if ( isRoot && !filter.matches(name) )
        {
            return null;
        }
        

        //read data >> consider changing it to stategy pattern
        switch ( type )
        {
            case MLArray.mxSTRUCT_CLASS:
                
                MLStructure struct = new MLStructure(name, dims, type, attributes);
                
                //field name lenght - this subelement always uses the compressed data element format
                tag = new ISMatTag(is);
                int maxlen = readInt32(is); //maximum field length

                //////  read fields data as Int8
                tag = new ISMatTag(is);
                //calculate number of fields
                int numOfFields = tag.size/maxlen;
                
                //padding afted field names
                int padding = (tag.size%8) != 0 ? 8-(tag.size%8) : 0;

                String[] fieldNames = new String[numOfFields];
                for ( int i = 0; i < numOfFields; i++ )
                {
                    byte[] buffer = new byte[maxlen];
                    is.read(buffer);
                    fieldNames[i] = zeroEndByteArrayToString(buffer);
                }
                is.skip(padding);
                
                //read fields
                for ( int index = 0; index < struct.getM()*struct.getN(); index++ )
                {
                    for ( int i = 0; i < numOfFields; i++ )
                    {
                        //read matrix data and copy it to inputStream, then recure
                        tag = new ISMatTag(is);
                        byte[] buffer = new byte[tag.size];
                        is.read(buffer);
                        MLArray fieldValue = readMatrix(new DataInputStream( new ByteArrayInputStream(buffer) ), false);
                        struct.setField(fieldNames[i], fieldValue, index);
                    }
                }
                mlArray = struct;
                break;
            case MLArray.mxCELL_CLASS:
                MLCell cell = new MLCell(name, dims, type, attributes);
                
                for ( int i = 0; i < cell.getM()*cell.getN(); i++ )
                {
                    tag = new ISMatTag(is);
                    byte[] buffer = new byte[tag.size];
                    is.read(buffer);
                    MLArray cellmatrix = readMatrix(new DataInputStream( new ByteArrayInputStream(buffer) ), false);
                    cell.set(cellmatrix, i);
                }
                mlArray = cell;
                break;
            case MLArray.mxDOUBLE_CLASS:
                
                double[] ad;
                MLDouble mldouble = new MLDouble(name, dims, type, attributes);
                
                //read real
                tag = new ISMatTag(is);
                ad = tag.readToDoubleArray();
                for ( int i = 0; i < ad.length; i++ )
                {
                    mldouble.setReal( ad[i], i );
                }
                //read complex
                if ( mldouble.isComplex() )
                {
                    tag = new ISMatTag(is);
                    ad = tag.readToDoubleArray();
                    for ( int i = 0; i < ad.length; i++ )
                    {
                        mldouble.setReal( ad[i], i );
                    }
                }
                mlArray = mldouble;
                break;
            case MLArray.mxCHAR_CLASS:
                MLChar mlchar = new MLChar(name, dims, type, attributes);
                
                //read real
                tag = new ISMatTag(is);
                char[] ac = tag.readToCharArray();
                for ( int i = 0; i < ac.length; i++ )
                {
                    mlchar.setChar( new Character(ac[i]), i );
                }
                mlArray = mlchar;
                break;
            case MLArray.mxSPARSE_CLASS:
                MLSparse sparse = new MLSparse(name, dims, attributes, nzmax);
                
                //read ir (row indices)
                tag = new ISMatTag(is);
                int[] ir = tag.readToIntArray();
                //read jc (column indices)
                tag = new ISMatTag(is);
                int[] jc = tag.readToIntArray();
                
                //read pr (reall part)
                tag = new ISMatTag(is);
                double[] ad1 = tag.readToDoubleArray();
                int n = 0;
                for ( int i = 0; i < ir.length; i++)
                {
                    if ( i < sparse.getN() )
                    {
                        n = jc[i];
                    }
                    sparse.setReal(ad1[i], ir[i], n);
                }
                
                //read pi (imaginary part)
                if ( sparse.isComplex() )
                {
                    tag = new ISMatTag(is);
                    double[] ad2 = tag.readToDoubleArray();
                    
                    //TODO: check if I should throw MatlabIOException
                    int n1 = 0;
                    for ( int i = 0; i < ir.length; i++)
                    {
                        if ( i < sparse.getN() )
                        {
                            n1 = jc[i];
                        }
                        sparse.setImaginary(ad2[i], ir[i], n1);
                    }
                }
                mlArray = sparse;
                break;
            default:
                throw new MatlabIOException("Incorrect matlab array class: " + MLArray.typeToString(type) );
               
        }
        
        logger.debug(mlArray + "\n" + mlArray.contentToString() );
        
        return mlArray;
    }
    private String zeroEndByteArrayToString(byte[] bytes) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( baos );
        
        for ( int i = 0; i < bytes.length && bytes[i] != 0; i++ )
        {
            dos.writeByte(bytes[i]);
        }
        return baos.toString();
        
    }
    private int[] readFlags(InputStream is) throws IOException
    {
        ISMatTag tag = new ISMatTag(is);
        
        int[] flags = tag.readToIntArray();
        
        return flags;
    }
    
    private int[] readDimension(InputStream is ) throws IOException
    {
        
        ISMatTag tag = new ISMatTag(is);
        int[] dims = tag.readToIntArray();
        return dims;
        
    }
    private String readName(InputStream is) throws IOException
    {
        String s;
        
        ISMatTag tag = new ISMatTag(is);
        char[] ac = tag.readToCharArray();
        s = new String(ac);

        return s;
    }
    /**
     * 
     * @param is
     * @return
     * @throws IOException
     */
    private int readInt32(InputStream is) throws IOException
    {
        byte[] buffer = new byte[4];
        is.read(buffer);
        if ( byteSwap )
        {
            return buffer[0] & 0xff | buffer[1] << 8 & 0xff00 | buffer[2] << 16 & 0xff0000 | buffer[3] << 24;
        }
        
        return buffer[0] << 24 | buffer[1] << 16 & 0xff0000 | buffer[2] << 8 & 0xff00 | buffer[3] & 0xff;
    }
    
    private void readHeader(InputStream is) throws IOException
    {
        //header values
        String description;
        int version;
        byte[] endianIndicator = new byte[2];
        
        //desctiptive text 116 bytes
        byte[] descriptionBuffer = new byte[116];
        is.read(descriptionBuffer);
        
        description = zeroEndByteArrayToString(descriptionBuffer);
        
        byte[] offsetBuffer = new byte[4];
        //subsyst data offset 4 bytes
        is.read(offsetBuffer);
        
        //subsyst data offset 4 bytes
        is.read(offsetBuffer);
        
        byte[] bversion = new byte[2];
        //vesion 2 bytes
        is.read(bversion);
        
        //endian indicator 2 bytes
        is.read(endianIndicator);
        
        //program reading the MAT-file must perform byte swapping to interpret the data
        //in the MAT-file correctly
        if ( (char)endianIndicator[0] == 'I' && (char)endianIndicator[1] == 'M')
        {
            byteSwap = true;
            version = bversion[1] & 0xff | bversion[0] << 8;
        }
        else
        {
            byteSwap = false;
            version = bversion[0] & 0xff | bversion[1] << 8;
        }
        
        matFileHeader = new MatFileHeader(description, version, endianIndicator);
        logger.debug(matFileHeader);
    }
    private class ISMatTag extends MatTag
    {
        public InputStream is;
        
        public ISMatTag(InputStream is) throws IOException
        {
            //must call parent constructor
            super(0,0, false);
            
            this.is = is;
            
            int tmp = readInt32(is);
            
            //data not packed in the tag
            if ( tmp >> 16 == 0 )
            {    
                type = tmp;
                size = readInt32(is);
                compressed = false;
            }
            else //data _packed_ in the tag (compressed)
            {
                size = tmp >> 16; // 2 more significant bytes
                type = tmp & 0xffff; // 2 less significant bytes;
                compressed = true;
            }
            setPadding();
        }
        public double[] readToDoubleArray() throws IOException
        {
            //allocate memory for array elements
            int elements = size/sizeOf();
            double[] ad = new double[elements];
            
            MatFileInputStream mfis = new MatFileInputStream( is, type, byteSwap );

            for ( int i = 0; i < elements; i++ )
            {
                ad[i] = mfis.readDouble();
            }
            
            //skip padding
            if ( padding > 0 )
            {
                is.skip(padding);
            }
            return ad;
        }
        public int[] readToIntArray() throws IOException
        {
            //allocate memory for array elements
            int elements = size/sizeOf();
            int[] ai = new int[elements];
            
            MatFileInputStream mfis = new MatFileInputStream( is, type, byteSwap );

            for ( int i = 0; i < elements; i++ )
            {
                ai[i] = mfis.readInt();
            }
            
            //skip padding
            if ( padding > 0 )
            {
                is.skip(padding);
            }
            return ai;
        }
        public char[] readToCharArray() throws IOException
        {
            //allocate memory for array elements
            int elements = size/sizeOf();
            char[] ac = new char[elements];
            
            MatFileInputStream mfis = new MatFileInputStream( is, type, byteSwap );

            for ( int i = 0; i < elements; i++ )
            {
                ac[i] = mfis.readChar();
            }
            
            //skip padding
            if ( padding > 0 )
            {
                is.skip(padding);
            }
            return ac;
        }
    }
}
