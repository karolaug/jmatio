package com.jmatio.test;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLStructure;
import com.jmatio.types.MLUInt64;
import com.jmatio.types.MLUInt8;

/**
 * The test suite for JMatIO
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatIOTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( MatIOTest.class );
    }
    
    //@Test
    public void testBenchmarkDouble() throws Exception
    {
        final String fileName = "bb.mat";
        final String name = "bigdouble";
//        final int SIZE = 1000;    
        //System.out.println(14e6);
//        ByteBuffer.allocateDirect(1000000000);
        
        
//        MLDouble mlDouble = new MLDouble( name, new int[] {SIZE, SIZE} );
//        
//        for ( int i = 0; i < SIZE*SIZE; i++ )
//        {
//            mlDouble.set((double)i, i);
//        }
//        
//        
//        //write array to file
//        ArrayList<MLArray> list = new ArrayList<MLArray>();
//        list.add( mlDouble );
//        
//        //write arrays to file
//        new MatFileWriter( fileName, list );
//        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
//        
//        System.out.println( mlArrayRetrived );
//        System.out.println( mlArrayRetrived.contentToString() );
        
        //test if MLArray objects are equal
//        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }
    
    
    @Test 
    public void testBenchmarkUInt8() throws Exception
    {
        final String fileName = "bigbyte.mat";
        final String name = "bigbyte";
        final int SIZE = 1024;    
        
        
        MLUInt8 mluint8 = new MLUInt8( name, new int[] { SIZE, SIZE } );
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mluint8 );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
        
        final long start = System.nanoTime();
        for ( int i = 0; i < mlArrayRetrived.getSize(); i++ )
        {
            ((MLNumericArray<?>)mlArrayRetrived).get(i);
        }
        final long stop = System.nanoTime();
        System.out.println("--> " + (stop - start)/1e6 +  "[ns]");
               
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mluint8, mlArrayRetrived);
    }
    
    @Test 
    public void testCellFromMatlabCreatedFile() throws IOException
    {
        //array name
        File file = new File("test/cell.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "cel" );
        
        List<MLArray> towrite =  Arrays.asList( mlArray );
        
        MatFileWriter  writer = new MatFileWriter( "cellcopy.mat", towrite );
    
        reader = new MatFileReader("cellcopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "cel" );
        
        //assertEquals( ((MLCell)mlArray).get(0), ((MLCell)mlArrayRetrieved).get(0));
    }

    /**
     * Tests filtered reading
     * 
     * @throws IOException
     */
    @Test 
    public void testFilteredReading() throws IOException
    {
        //1. First create arrays
        //array name
        String name = "doublearr";
        String name2 = "dummy";
        //file name in which array will be storred
        String fileName = "filter.mat";

        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        MLChar mlChar = new MLChar( name2, "I am dummy" );
        
        //2. write arrays to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );
        list.add( mlChar );
        new MatFileWriter( fileName, list );
        
        //3. create new filter instance
        MatFileFilter filter = new MatFileFilter();
        filter.addArrayName( name );
        
        //4. read array form file
        MatFileReader mfr = new MatFileReader( fileName, filter );
        
        //check size of
        Map<String, MLArray> content = mfr.getContent();
        assertEquals("Test if only one array was red", 1, content.size() );
        
    }
    /**
     * Test <code>MatFileFilter</code> options
     */
    @Test 
    public void testMatFileFilter()
    {
        //create new filter instance
        MatFileFilter filter = new MatFileFilter();
        
        //empty filter should match all patterns
        assertEquals("Test if empty filter matches all patterns", true, filter.matches("any") );
    
        //now add something to the filter
        filter.addArrayName("my_array");
        
        //test if filter matches my_array
        assertEquals("Test if filter matches given array name", true, filter.matches("my_array") );
        
        //test if filter returns false if does not match given name
        assertEquals("Test if filter does not match non existent name", false, filter.matches("dummy") );
    
    }
    
    
    /**
     * Test <code>MatFileFilter</code> options
     * @throws IOException 
     */
    @Test 
    public void testMLCell() throws IOException
    {
        //array name
        String name = "doublearr";
        String name2 = "name";
        //file name in which array will be storred
        String fileName = "mlcell.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        MLChar mlChar = new MLChar( name2, "none" );
        
        
        MLCell mlCell = new MLCell("cl", new int[] {2,1} );
        mlCell.set(mlChar, 0);
        mlCell.set(mlDouble, 1);
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlCell );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLCell mlArrayRetrived = (MLCell)mfr.getMLArray( "cl" );
        
        assertEquals(mlDouble, mlArrayRetrived.get(1) );
        assertEquals(mlChar, mlArrayRetrived.get(0) );
        
    
    }
    
    /**
     * Tests <code>MLChar</code> reading and writing.
     * 
     * @throws IOException
     */
    @Test 
    public void testMLCharArray() throws IOException
    {
        //array name
        String name = "chararr";
        //file name in which array will be storred
        String fileName = "mlchar.mat";
        //temp
        String valueS;

        //create MLChar array of a name "chararr" containig one
        //string value "dummy"
        MLChar mlChar = new MLChar(name, "dummy");
        
        //get array name
        valueS = mlChar.getName();
        assertEquals("MLChar name getter", name, valueS);
        
        //get value of the first element
        valueS = mlChar.getString(0);
        assertEquals("MLChar value getter", "dummy", valueS);
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlChar );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlCharRetrived = mfr.getMLArray( name );
        
        assertEquals("Test if value red from file equals value stored", mlChar, mlCharRetrived);
        
        //try to read non existent array
        mlCharRetrived = mfr.getMLArray( "nonexistent" );
        assertEquals("Test if non existent value is null", null, mlCharRetrived);
    }
    
    /**
     * Tests <code>MLDouble</code> reading and writing.
     * 
     * @throws IOException
     */
    @Test 
    public void testMLDoubleArray() throws IOException
    {
        //array name
        String name = "doublearr";
        //file name in which array will be storred
        String fileName = "mldouble.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        //test 2D array coresponding to test vector
        double[][] src2D = new double[][] { { 1.3, 4.0 },
                                            { 2.0, 5.0 },
                                            { 3.0, 6.0 }
                                        };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
        
        //System.out.println( mlDouble.contentToString() );
        //System.out.println( mlArrayRetrived.contentToString() );
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
        
        //test if 2D array match
        for ( int i = 0; i < src2D.length; i++ )
        {
            boolean result = Arrays.equals( src2D[i], ((MLDouble)mlArrayRetrived ).getArray()[i] );
            assertEquals( "2D array match", true, result );
        }
        
        //test new constructor
        MLArray mlDouble2D = new MLDouble(name, src2D );
        //compare it with original
        assertEquals( "Test if double[][] constructor produces the same matrix as normal one", mlDouble2D, mlDouble );
    }

    /**
     * Test <code>MatFileFilter</code> options
     * @throws IOException 
     */
    @Test
    public void testMLStructure() throws IOException
    {
        //array name
        //file name in which array will be storred
        String fileName = "mlstruct.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        
        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( null, src, 3 );
        MLChar mlChar = new MLChar( null, "I am dummy" );
        
        
        MLStructure mlStruct = new MLStructure("str", new int[] {1,1} );
        mlStruct.setField("f1", mlDouble);
        mlStruct.setField("f2", mlChar);
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlStruct );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( "str" );
        
        assertEquals(mlDouble, mlArrayRetrived.getField("f1") );
        assertEquals(mlChar, mlArrayRetrived.getField("f2") );
        
    
    }
    
    /**
     * Tests <code>MLUint8</code> reading and writing.
     * 
     * @throws IOException
     */
    @Test 
    public void testMLUInt8Array() throws IOException
    {
        //array name
        String name = "arr";
        //file name in which array will be storred
        String fileName = "mluint8tst.mat";

        //test column-packed vector
        byte[] src = new byte[] { 1, 2, 3, 4, 5, 6 };
        //test 2D array coresponding to test vector
        byte[][] src2D = new byte[][] { { 1, 4 },
                                        { 2, 5 },
                                        { 3, 6 }
                                        };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLUInt8 mluint8 = new MLUInt8( name, src, 3 );
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mluint8 );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
        
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mluint8, mlArrayRetrived);
        
        //test if 2D array match
        for ( int i = 0; i < src2D.length; i++ )
        {
            boolean result = Arrays.equals( src2D[i], ((MLUInt8)mlArrayRetrived ).getArray()[i] );
            assertEquals( "2D array match", true, result );
        }
        
        //test new constructor
        MLArray mlMLUInt82D = new MLUInt8(name, src2D );
        //compare it with original
        assertEquals( "Test if double[][] constructor produces the same matrix as normal one", mlMLUInt82D, mluint8 );
    }
    
    /**
     * Regression bug
     * 
     * @throws Exception
     */
    @Test 
    public void testDoubleFromMatlabCreatedFile() throws Exception
    {
        //array name
        String name = "arr";
        //file name in which array will be stored
        String fileName = "test/matnativedouble.mat";

        //test column-packed vector
        double[] src = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
        
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }
    
    /**
     * Regression bug.
     
     * <pre><code>
     * Matlab code:
     * >> arr = [1.1, 4.4; 2.2, 5.5; 3.3, 6.6];
     * >> save('matnativedouble2', arr);
     * </code></pre>
     * 
     * @throws IOException
     */
    @Test 
    public void testDoubleFromMatlabCreatedFile2() throws IOException
    {
        //array name
        String name = "arr";
        //file name in which array will be stored
        String fileName = "test/matnativedouble2.mat";

        //test column-packed vector
        double[] src = new double[] { 1.1, 2.2, 3.3, 4.4, 5.5, 6.6 };
        MLDouble mlDouble = new MLDouble( name, src, 3 );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLArray mlArrayRetrived = mfr.getMLArray( name );
        
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", mlDouble, mlArrayRetrived);
    }
    
    @Test 
    public void testSparseFromMatlabCreatedFile() throws IOException
    {
        //array name
        File file = new File("test/sparse.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "spa" );
        
        List<MLArray> towrite =  Arrays.asList( mlArray );
        
        new MatFileWriter( "sparsecopy.mat", towrite );
    
        reader = new MatFileReader("sparsecopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "spa" );
        
        assertEquals(mlArray, mlArrayRetrieved);
        
    }
    
    @Test 
    public void testStructureFromMatlabCreatedFile() throws IOException
    {
        //array name
        File file = new File("test/simplestruct.mat");
        MatFileReader reader = new MatFileReader( file );
        MLArray mlArray = reader.getMLArray( "structure" );
        
        List<MLArray> towrite =  Arrays.asList( mlArray );
        
        new MatFileWriter( "simplestructcopy.mat", towrite );
    
        reader = new MatFileReader("simplestructcopy.mat");
        MLArray mlArrayRetrieved = reader.getMLArray( "structure" );
    }
    
    /**
     * Regression bug: Test writing several arrays into a single file.
     * 
     * @throws IOException
     */
    @Test 
    public void testWritingManyArraysInFile() throws IOException
    {
        final String fileName = "multi.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src2 = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] src3 = new double[] { 3.1415 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble( "m1", src, 3 );
        MLDouble m2= new MLDouble( "m2", src2, 3 );
        MLDouble m3 = new MLDouble( "m3", src3, 1 );
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( m1);
        list.add( m2);
        list.add( m3);
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        
        //test if MLArray objects are equal
        assertEquals("Test if value red from file equals value stored", m1, mfr.getMLArray( "m1" ));
        assertEquals("Test if value red from file equals value stored", m2, mfr.getMLArray( "m2" ));
        assertEquals("Test if value red from file equals value stored", m3, mfr.getMLArray( "m3" ));
    }
    
    
    /**
     * 
     * <pre><code>
     * >> x = NaN;
     * >> save('nan', 'x');
     * </code></pre>
     * @throws IOException
     */
    @Test 
    public void testReadingNaN() throws IOException
    {
        final String fileName = "test/nan.mat";
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        
        assertEquals("Test if value red from file equals NaN", Double.NaN, 
                                    ((MLDouble)mfr.getMLArray( "x" )).get(0,0) );
        
        
    }
    
    @Test 
    public void testUInt8() throws Exception
    {
        String fileName = "test/uint8.mat";
        String arrName = "arr";
        MatFileReader mfr;
        MLArray src;
        
        //read array form file
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     (byte)0, 
                     ((MLUInt8)mfr.getMLArray( arrName )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                (byte)255, 
                ((MLUInt8)mfr.getMLArray( arrName )).get(0,1) );
        
        src = mfr.getMLArray( arrName );
        
        //write
        fileName = "uint8out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) ); 
        new MatFileWriter(fileName, towrite );
    
        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     (byte)0, 
                     ((MLUInt8)mfr.getMLArray( arrName )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                (byte)255, 
                ((MLUInt8)mfr.getMLArray( arrName )).get(0,1) );
    
        
        assertEquals("Test if array retrieved from " + fileName + " equals source array", 
                src, 
                mfr.getMLArray( arrName ) );
    }

    @Test 
    public void testInt8() throws Exception
    {
        String fileName = "test/int8.mat";
        String arrName = "arr";
        MatFileReader mfr;
        MLArray src;
        
        //read array form file
        mfr = new MatFileReader( fileName );
        
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     (byte)-128, 
                     ((MLInt8)mfr.getMLArray( "arr" )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                (byte)127, 
                ((MLInt8)mfr.getMLArray( "arr" )).get(0,1) );
        
        src = mfr.getMLArray( "arr" );
        
        //write
        fileName = "int8out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) ); 
        new MatFileWriter(fileName, towrite );
    
        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     (byte)-128, 
                     ((MLInt8)mfr.getMLArray( arrName )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                (byte)127, 
                ((MLInt8)mfr.getMLArray( arrName )).get(0,1) );
    
        
        assertEquals("Test if array retrieved from " + fileName + " equals source array", 
                src, 
                mfr.getMLArray( arrName ) );

        
    }
    

    @Test 
    public void testInt64() throws Exception
    {
        String fileName = "test/int64.mat";
        String arrName = "arr";
        MatFileReader mfr;
        MLArray src;
        
        Long max = Long.parseLong("9223372036854775807");
        Long min = Long.parseLong("-9223372036854775808");
        
        //read array form file
        mfr = new MatFileReader( fileName );
        
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     min, 
                     ((MLInt64)mfr.getMLArray( "arr" )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                    max, 
                    ((MLInt64)mfr.getMLArray( "arr" )).get(0,1) );
        
        src = mfr.getMLArray( "arr" );
        
        //write
        fileName = "int64out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) ); 
        new MatFileWriter(fileName, towrite );
    
        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     min, 
                     ((MLInt64)mfr.getMLArray( arrName )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                    max, 
                    ((MLInt64)mfr.getMLArray( arrName )).get(0,1) );
    
        
        assertEquals("Test if array retrieved from " + fileName + " equals source array", 
                src, 
                mfr.getMLArray( arrName ) );

    }
    @Test 
    public void testUInt64() throws Exception
    {
        String fileName = "test/uint64.mat";
        String arrName = "arr";
        MatFileReader mfr;
        MLArray src;
        
        Long max = Long.MAX_VALUE;
        Long min = Long.parseLong("0");
        
        
        //read array form file
        mfr = new MatFileReader( fileName );
        
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     min, 
                     ((MLUInt64)mfr.getMLArray( "arr" )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                    max, 
                    ((MLUInt64)mfr.getMLArray( "arr" )).get(0,1) );
        
        src = mfr.getMLArray( "arr" );
        
        //write
        fileName = "uint64out.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add( mfr.getMLArray( arrName ) ); 
        new MatFileWriter(fileName, towrite );
    
        //read again
        mfr = new MatFileReader( fileName );
        assertEquals("Test min. value from file:" + fileName + " array: " + arrName, 
                     min, 
                     ((MLUInt64)mfr.getMLArray( arrName )).get(0,0) );
        
        assertEquals("Test max. value from file:" + fileName + " array: " + arrName, 
                    max, 
                    ((MLUInt64)mfr.getMLArray( arrName )).get(0,1) );
    
        
        assertEquals("Test if array retrieved from " + fileName + " equals source array", 
                src, 
                mfr.getMLArray( arrName ) );

    }
    
    @Test
    public void testWritingMethods() throws IOException
    {
        final String fileName = "nwrite.mat";
        final File f = new File(fileName);
        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };

        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble m1 = new MLDouble( "m1", src, 3 );
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( m1);
        
        MatFileWriter writer = new MatFileWriter();
        
        writer.write(f, list);
        
        assertTrue("Test if file was created", f.exists() );
        
        MLArray array = null;
        
        //try to read it
        MatFileReader reader = new MatFileReader();
        reader.read(f, MatFileReader.MEMORY_MAPPED_FILE );
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);
        
        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );
        
        writer.write(fileName, list);
        
        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.MEMORY_MAPPED_FILE );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);

        
        //try the same with direct buffer allocation
        reader.read(f, MatFileReader.DIRECT_BYTE_BUFFER );
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);
        
        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );
        
        writer.write(fileName, list);
        
        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.DIRECT_BYTE_BUFFER );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);
        
        //try the same with direct buffer allocation
        reader.read(f, MatFileReader.HEAP_BYTE_BUFFER);
        array = reader.getMLArray("m1");
        assertEquals("Test if is correct file", array, m1);
        
        //try to delete the file
        assertTrue("Test if file can be deleted", f.delete() );
        
        writer.write(fileName, list);
        
        assertTrue("Test if file was created", f.exists() );
        reader.read(f, MatFileReader.HEAP_BYTE_BUFFER );
        assertEquals("Test if is correct file", reader.getMLArray("m1"), m1);
        
    }
    
    
}
