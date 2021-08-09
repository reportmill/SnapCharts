/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * A class to represent a data point.
 */
public class DataPoint {
    
    // The DataSet this point belongs to
    protected DataSet  _dset;

    // The index of point in data set
    protected int  _index;

    // The X/Y/Z, if cached
    private Double  _x, _y, _z;

    // The C, if cached
    private String  _c;

    /**
     * Constructor for dataset point at index.
     */
    public DataPoint(DataSet aDataSet, int anIndex)
    {
        _dset = aDataSet;
        _index = anIndex;
    }

    /**
     * Constructor for cached values.
     */
    public DataPoint(Double aX, Double aY, Double aZ, String aC)
    {
        _x = aX; _y = aY; _z = aZ; _c = aC;
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dset; }

    /**
     * Returns the index of this point in dataset.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the X value.
     */
    public double getX()
    {
        if (_x!=null)
            return _x;
        return _dset!=null ? _dset.getX(_index) : 0;
    }

    /**
     * Returns the Y value.
     */
    public double getY()
    {
        if (_y!=null)
            return _y;
        return _dset!=null ? _dset.getY(_index) : 0;
    }

    /**
     * Returns the Z value.
     */
    public double getZ()
    {
        if (_z!=null)
            return _z;
        return _dset!=null ? _dset.getZ(_index) : 0;
    }

    /**
     * Returns the name.
     */
    public String getC()
    {
        if (_c!=null || _dset==null)
            return _c;
        return _dset.getC(_index);
    }

    /**
     * Returns X as a Double.
     */
    public Double getValueX()
    {
        if (_x!=null || _dset==null)
            return _x;
        return _dset.getValueX(_index);
    }

    /**
     * Returns Y as a Double.
     */
    public Double getValueY()
    {
        if (_y!=null || _dset==null)
            return _y;
        return _dset.getValueY(_index);
    }

    /**
     * Returns Z as a Double.
     */
    public Double getValueZ()
    {
        if (_z!=null || _dset==null)
            return _z;
        return _dset.getValueZ(_index);
    }

    /**
     * Returns the column index.
     */
    public int getColIndex()
    {
        if (_dset.getDataType() != DataType.XYZZ)
            return 0;
        int index = getIndex();
        int colCount = _dset.getColCount(); if (colCount == 0) return index;
        return index % colCount;
    }

    /**
     * Returns the row index.
     */
    public int getRowIndex()
    {
        int index = getIndex();
        if (_dset.getDataType() != DataType.XYZZ)
            return index;
        int colCount = _dset.getColCount(); if (colCount == 0) return index;
        return index / colCount;
    }

    /**
     * Caches values.
     */
    public void cacheValues()
    {
        _x = getValueX();
        _y = getValueY();
        _z = getValueZ();
        _c = getC();
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        DataPoint other = anObj instanceof DataPoint ? (DataPoint)anObj : null;
        if (other == null) return false;

        // Check DataSet, Index
        if (other._dset != _dset) return false;
        if (other._index != _index) return false;
        return true;
    }
}