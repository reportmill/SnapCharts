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
     * Returns the name.
     */
    public String getName()  { return toString(); }

    /**
     * Returns the data class.
     */
    public Class<?> getDataArrayClass()
    {
        return switch (this) {
            case I -> int[].class;
            case C -> String[].class;
            default -> double[].class;
        };
    }
}
