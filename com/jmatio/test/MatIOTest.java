package com.jmatio.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;

public class MatIOTest
{
    public static void main(String[] args)
    {
        try
        {
            Logger logger = Logger.getLogger("main");
            logger.info("\n>START");

            ArrayList<MLArray> data = (new MatFileReader("C:\\\\spm" +
                    ".mat")).getData();
            
            logger.debug("\nReading result: \n" + data);
        
            ArrayList<MLArray> toWrite = new ArrayList<MLArray>();
            toWrite.add( data.get(0) );
            
            logger.debug("\nWriting what was red");

            new MatFileWriter("C:\\\\out.mat", toWrite);

            logger.debug("\nwriting done");
            
            ArrayList<MLArray> data2 = (new MatFileReader("C:\\\\out.mat")).getData();
            logger.debug("\nReading 2 result: \n" + data2);
            
            logger.info("\n>DONE");
        
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
