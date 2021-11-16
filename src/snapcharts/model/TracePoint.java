/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snapcharts.data.DataPoint;
import snapcharts.data.DataType;

/**
 * A class to represent a data point.
 */
public class TracePoint extends DataPoint {

    // The Trace this point belongs to
    protected Trace  _trace;

    // The index of point in data set
    protected int  _index;

    /**
     * Constructor.
     */
    public TracePoint(Trace aTrace, int anIndex)
    {
        _trace = aTrace;
        _index = anIndex;
        _x = _trace.getValueX(_index);
        _y = _trace.getValueY(_index);
        _z = _trace.getValueZ(_index);
        _c = _trace.getC(_index);
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _trace; }

    /**
     * Returns the index of this point in trace.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the column index.
     */
    public int getColIndex()
    {
        if (_trace.getDataType() != DataType.XYZZ)
            return 0;
        int index = getIndex();
        int colCount = _trace.getColCount(); if (colCount == 0) return index;
        return index % colCount;
    }

    /**
     * Returns the row index.
     */
    public int getRowIndex()
    {
        int index = getIndex();
        if (_trace.getDataType() != DataType.XYZZ)
            return index;
        int colCount = _trace.getColCount(); if (colCount == 0) return index;
        return index / colCount;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        TracePoint other = anObj instanceof TracePoint ? (TracePoint) anObj : null;
        if (other == null) return false;

        // Check Trace, Index
        if (other._trace != _trace) return false;
        if (other._index != _index) return false;
        return true;
    }
}