/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.SnapUtils;

/**
 * A class to represent a data point.
 */
public class DataPoint implements Cloneable {

    // The X/Y/Z, if cached
    protected Double  _x, _y, _z;

    // The C, if cached
    protected String  _c;

    // The DataSet if this point exists in one
    protected DataSet  _dataSet;

    // The row index in DataSet if point exists in one
    protected int  _index;

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
     * Constructor for DataSet and index.
     */
    public DataPoint(DataSet aDataSet, int anIndex)
    {
        _dataSet = aDataSet;
        _index = anIndex;
        _x = aDataSet.getValueX(anIndex);
        _y = aDataSet.getValueY(anIndex);
        _z = aDataSet.getValueZ(anIndex);
        _c = aDataSet.getC(anIndex);
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
     * Returns the DataSet for this point, if point came from DataSet.
     */
    public DataSet getDataSet()  { return _dataSet; }

    /**
     * Returns the DataSet index for this point, if point came from DataSet.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the column index.
     */
    public int getColIndex()
    {
        if (_dataSet == null || _dataSet.getDataType() != DataType.XYZZ)
            return 0;
        int index = getIndex();
        int colCount = _dataSet.getColCount(); if (colCount == 0) return index;
        return index % colCount;
    }

    /**
     * Returns the row index.
     */
    public int getRowIndex()
    {
        int index = getIndex();
        if (_dataSet == null || _dataSet.getDataType() != DataType.XYZZ)
            return index;
        int colCount = _dataSet.getColCount(); if (colCount == 0) return index;
        return index / colCount;
    }

    /**
     * Returns a copy of this point with new value for given channel.
     */
    public DataPoint copyForChannelValue(DataChan aChan, Object aValue)
    {
        // Do normal clone
        DataPoint clone = clone();

        // Change clone channel value
        switch (aChan) {
            case X: clone._x = SnapUtils.getDouble(aValue); break;
            case Y: clone._y = SnapUtils.getDouble(aValue); break;
            case Z: clone._z = SnapUtils.getDouble(aValue); break;
            case C: clone._c = SnapUtils.stringValue(aValue); break;
            default: System.err.println("DataPoint.copyForChannelValue: Unsupported channel: " + aChan);
        }

        // Return
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    public DataPoint clone()
    {
        DataPoint clone;
        try { clone = (DataPoint) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }
        return clone;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        DataPoint other = anObj instanceof DataPoint ? (DataPoint) anObj : null;
        if (other == null) return false;

        // Check DataSet, Index
        if (other._dataSet != _dataSet) return false;
        if (other._index != _index) return false;
        return true;
    }
}