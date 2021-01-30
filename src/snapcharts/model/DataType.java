package snapcharts.model;

/**
 * Constants for format of DataSet.
 */
public enum DataType {

    IY,
    XY,
    CY,
    XYZ,
    UNKNOWN;

    /**
     * Returns a DataType given data arrays.
     */
    public static DataType getDataType(boolean hasX, boolean hasY, boolean hasZ, boolean hasC)
    {
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
