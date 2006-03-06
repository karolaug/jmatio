package com.jmatio.types;

public abstract class MLNumericArray<T extends Number> extends MLArray implements GenericArrayCreator<T>
{
    private T[] real;
    private T[] imaginary;
    
    public MLNumericArray(String name, int[] dims, int type, int attributes)
    {
        super(name, dims, type, attributes);
        
        real = createArray(getM(), getN());
        imaginary = createArray(getM(), getN());
    }
    public T getReal(int m, int n)
    {
        return real[getIndex(m,n)];
    }
    public void setReal(T value, int m, int n)
    {
        real[getIndex(m,n)] = value;
    }
    public void setReal(T value, int index)
    {
        real[index] = value;
    }
    public void setReal( T[] vector )
    {
        if ( vector.length != getSize() )
        {
            throw new IllegalArgumentException("Matrix dimensions do not match. " + getSize() + " not " + vector.length);
        }
        System.arraycopy(vector, 0, real, 0, vector.length);
    }
    public void setImaginary(T value, int m, int n)
    {
        imaginary[getIndex(m,n)] = value;
    }
    public void setImaginary(T value, int index)
    {
        imaginary[index] = value;
    }
    public T getImaginary(int m, int n)
    {
        return imaginary[getIndex(m,n)];
    }
    public T[] exportReal()
    {
        return real.clone();
    }
    public T[] exportImaginary()
    {
        return imaginary.clone();
    }
    public void set(T value, int m, int n)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(value, m, n);
    }
    public void set(T value, int index)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(value, index);
    }
    public void set(T[] vector)
    {
        if ( isComplex() )
        {
            throw new IllegalStateException("Cannot use this method for Complex matrices");
        }
        setReal(vector);
    }
    public String contentToString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " = \n");
        
        for ( int m = 0; m < getM(); m++ )
        {
           sb.append("\t");
           for ( int n = 0; n < getN(); n++ )
           {
               sb.append( getReal(m,n) );
               if ( isComplex() )
               {
                   sb.append("+" + getImaginary(m,n) );
               }
               sb.append("\t");
           }
           sb.append("\n");
        }
        return sb.toString();
    }
}
