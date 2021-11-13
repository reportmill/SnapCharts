/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snapcharts.data.DataPoint;
import snapcharts.data.DataType;

/**
 * A class to represent a data point.
 */
public class DataSetPoint extends DataPoint {

    // The DataSet this point belongs to
    protected DataSet _dset;

    // The index of point in data set
    protected int  _index;

    /**
     * Constructor for dataset point at index.
     */
    public DataSetPoint(DataSet aDataSet, int anIndex)
    {
        _dset = aDataSet;
        _index = anIndex;
        _x = _dset.getValueX(_index);
        _y = _dset.getValueY(_index);
        _z = _dset.getValueZ(_index);
        _c = _dset.getC(_index);
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
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        DataSetPoint other = anObj instanceof DataSetPoint ? (DataSetPoint) anObj : null;
        if (other == null) return false;

        // Check DataSet, Index
        if (other._dset != _dset) return false;
        if (other._index != _index) return false;
        return true;
    }
}