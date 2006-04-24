package com.jmatio.io;

import java.util.HashSet;
import java.util.Set;

public class MatFileFilter
{
    private Set<String> filter;
    
    public MatFileFilter()
    {
        filter = new HashSet<String>();
    }
    public void addArrayName( String name )
    {
        filter.add( name );
    }
    public boolean matches( String name )
    {
        if ( filter.size() == 0 )
        {
            return true;
        }
        return filter.contains( name );
    }
}
