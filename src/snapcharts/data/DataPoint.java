/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;

/**
 * A class to represent a data point.
 */
public class DataPoint {
    
    // The X/Y/Z, if cached
    protected Double  _x, _y, _z;

    // The C, if cached
    protected String  _c;

    /**
     * Constructor.
     */
    public DataPoint()  { }

    /**
     * Constructor for cached values.
     */
    public DataPoint(Double aX, Double aY, Double aZ, String aC)
    {
        _x = aX; _y = aY; _z = aZ; _c = aC;
    }

    /**
     * Returns the X value.
     */
    public double getX()
    {
        return _x != null ? _x : 0;
    }

    /**
     * Returns the Y value.
     */
    public double getY()
    {
        return _y != null ? _y : 0;
    }

    /**
     * Returns the Z value.
     */
    public double getZ()
    {
        return _z != null ? _z : 0;
    }

    /**
     * Returns the name.
     */
    public String getC()
    {
        return _c;
    }

    /**
     * Returns X as a Double.
     */
    public Double getValueX()
    {
        return _x;
    }

    /**
     * Returns Y as a Double.
     */
    public Double getValueY()
    {
        return _y;
    }

    /**
     * Returns Z as a Double.
     */
    public Double getValueZ()
    {
        return _z;
    }

    /**
     * Returns the column index.
     */
    /*public int getColIndex()
    {
        if (_dset.getDataType() != DataType.XYZZ)
            return 0;
        int index = getIndex();
        int colCount = _dset.getColCount(); if (colCount == 0) return index;
        return index % colCount;
    }*/

    /**
     * Returns the row index.
     */
    /*public int getRowIndex()
    {
        int index = getIndex();
        if (_dset.getDataType() != DataType.XYZZ)
            return index;
        int colCount = _dset.getColCount(); if (colCount == 0) return index;
        return index / colCount;
    }*/

    /**
     * Caches values.
     */
    /*public void cacheValues()
    {
        _x = getValueX();
        _y = getValueY();
        _z = getValueZ();
        _c = getC();
    }*/

    /**
     * Standard equals implementation.
     */
    /*public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        DataPoint other = anObj instanceof DataPoint ? (DataPoint)anObj : null;
        if (other == null) return false;

        // Check DataSet, Index
        if (other._dset != _dset) return false;
        if (other._index != _index) return false;
        return true;
    }*/
}