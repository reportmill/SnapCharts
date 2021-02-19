package snapcharts.model;

/**
 * Constants for format of DataSet.
 */
public enum DataType {

    /** Indexed Y data */
    IY,

    /** XY data */
    XY,

    /** Text + Y data */
    CY,

    /** XYZ data */
    XYZ,

    /** XYZZ data - X/Y are row/col values, Z is matrix of values */
    XYZZ,

    /** Unknown */
    UNKNOWN;

    /**
     * Returns the number of channels of data.
     */
    public int getChannelCount()
    {
        if (this==UNKNOWN) return 0;
        return toString().length();
    }

    /**
     * Returns the channel at given index.
     */
    public DataChan getChannel(int anIndex)
    {
        char c = toString().charAt(anIndex);
        switch (c) {
            case 'X': return DataChan.X;
            case 'Y': return DataChan.Y;
            case 'Z': return DataChan.Z;
            case 'C': return DataChan.C;
            case 'I': return DataChan.I;
            default: throw new RuntimeException("DataType.getChannel: Unknown char: " + c);
        }
    }

    /**
     * Returns whether given channel is in DataType.
     */
    public boolean hasChannel(DataChan aChan)
    {
        char c = aChan.getChar();
        return toString().indexOf(c) >= 0;
    }

    /**
     * Returns a DataType given data arrays.
     */
    public static DataType getDataType(boolean hasX, boolean hasY, boolean hasZ, boolean hasZZ, boolean hasC)
    {
        if (hasX && hasY && hasZZ)
            return XYZZ;
        if (hasX && hasY && hasZ)
            return XYZ;
        if (hasX && hasY)
            return XY;
        if (hasY && hasC)
            return CY;
        if (hasY)
            return IY;
        return UNKNOWN;
    }
}
