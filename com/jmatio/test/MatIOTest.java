package com.jmatio.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;

/**
 * The test suite for JMatIO
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
public class MatIOTest
{
    
    /**
     * Tests <code>MLChar</code> reading and writing.
     * 
     * @throws IOException
     */
    @Test public void MLCharArrayTest() throws IOException
    {
        MLChar mlChar = new MLChar("chararr", "dummy");
        
        String valueS;
        
        //get array name
        valueS = mlChar.getName();
        assertEquals("MLChar name getter", "chararr", valueS);
        
        //get value of the first element
        valueS = mlChar.getString(0);
        assertEquals("MLChar value getter", "dummy", valueS);
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlChar );
        
        //write arrays to file
        new MatFileWriter("mlchar.mat", list);
        
        //read array form file
        MatFileReader mfr = new MatFileReader("mlchar.mat");
        MLArray mlCharRetrived = mfr.getMLArray( "chararr" );
        
        assertEquals("Test if value red from file equals value stored", mlChar, mlCharRetrived);
        
        //try to read non existent array
        mlCharRetrived = mfr.getMLArray( "nonexistent" );
        assertEquals("Test if non existent value is null", null, mlCharRetrived);
    }
    
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( MatIOTest.class );
    }
    
}
