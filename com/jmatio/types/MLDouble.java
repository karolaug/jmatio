package com.jmatio.types;

public class MLDouble extends MLNumericArray<Double>
{

    public MLDouble(String name, int[] dims)
    {
        this(name, dims, MLArray.mxDOUBLE_CLASS, 0);
    }
    public MLDouble(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
    }
    public Double[] createArray(int m, int n)
    {
        return new Double[m*n];
    }




}
