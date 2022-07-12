/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;

/**
 * Constants for a channel of data in a DataType (X, Y, Z, C, I).
 */
public enum DataChan {

    /** X channel. */
    X,

    /** Y channel. */
    Y,

    /** Z channel. */
    Z,

    /** Text channel. */
    C,

    /** Index channel. */
    I,

    /** Theta channel. */
    T,

    /** Radius channel. */
    R;

    /**
     * Return the char.
     */
    public char getChar()
    {
        return toString().charAt(0);
    }

    /**
     * Returns the name.
     */
    public String getName()  { return toString(); }

    /**
     * Returns the data class.
     */
    public Class getDataArrayClass()
    {
        switch (this) {
            case I: return int[].class;
            case C: return String[].class;
            default: return double[].class;
        }
    }
}
