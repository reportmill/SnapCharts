/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.ArrayUtils;

/**
 * Constants for format of DataSet.
 */
public enum DataType {

    /** Indexed Y data */
    IY,

    /** Text + Y data */
    CY,

    /** XY data */
    XY,

    /** XYZ data */
    XYZ,

    /** XYZZ data - X/Y are row/col values, Z is matrix of values */
    XYZZ,

    /** TR data */
    TR,

    /** TRZ data */
    TRZ,

    /** TRZZ data - X/Y are row/col values, Z is matrix of values */
    TRZZ,

    /** Unknown */
    UNKNOWN;

    // The chanels (cached)
    private DataChan[]  _channels;

    /**
     * Returns the number of channels of data.
     */
    public int getChannelCount()
    {
        if (this==UNKNOWN) return 0;
        if (this==XYZZ) return 3;
        if (this==TRZZ) return 3;
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
            case 'T': return DataChan.T;
            case 'R': return DataChan.R;
            default: throw new RuntimeException("DataType.getChannel: Unknown char: " + c);
        }
    }

    /**
     * Returns the channels.
     */
    public DataChan[] getChannels()
    {
        // If already set, just return
        if (_channels != null) return _channels;

        // Create channels array
        int chanCount = getChannelCount();
        DataChan[] chans = new DataChan[chanCount];
        for (int i=0; i<chanCount; i++)
            chans[i] = getChannel(i);

        // Set/return channels
        return _channels = chans;
    }

    /**
     * Returns whether given channel is in DataType.
     */
    public boolean hasChannel(DataChan aChan)
    {
        return ArrayUtils.contains(getChannels(), aChan);
    }

    /**
     * Returns whether DataType has Z channel.
     */
    public boolean hasZ()  { return hasChannel(DataChan.Z); }

    /**
     * Returns whether DataType is polar data type.
     */
    public boolean isPolar()
    {
        return this == TR || this == TRZ || this == TRZZ;
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
