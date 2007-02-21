package com.jmatio.types;

public interface GenericArrayCreator<T>
{
    T[] createArray(int m, int n);
    int getBytesAllocated();
    T buldFromBytes( byte[] bytes );
    byte[] getByteArray ( T value );
}
