package snapcharts.model;

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
    I;

    /**
     * Return the char.
     */
    public char getChar()
    {
        return toString().charAt(0);
    }

}
