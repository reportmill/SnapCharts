/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.util.Arrays;

/**
 * This is the cover class for holding the raw data.
 */
public class DataSetImpl extends DataSet {

    // The number of points
    private int  _pointCount;

    // The actual length of data arrays
    private int  _arrayLen;

    // Cached arrays of X/Y/Z data
    private double[] _dataX, _dataY, _dataZ;

    // Cached arrays of C (text) data
    private String[] _dataC;

    // Constants
    private int MAX_POINT_COUNT = 2000000;

    /**
     * Constructor.
     */
    public DataSetImpl()  { }

    /**
     * Constructor for DataType and arrays.
     */
    public DataSetImpl(DataType aDataType, Object ... theValues)
    {
        setDataType(aDataType);

        // Handle XYZZ special
        if (aDataType == DataType.XYZZ) {
            double[] dataX = (double[]) theValues[0];
            double[] dataY = (double[]) theValues[1];
            double[] dataZ = (double[]) theValues[2];
            DataSetUtils.addDataPointsXYZZ(this, dataX, dataY, dataZ);
            return;
        }

        DataChan[] channels = aDataType.getChannelsXY();
        for (int i = 0; i < channels.length; i++) {
            DataChan chan = channels[i];
            Object chanData = theValues[i];
            switch (chan) {
                case X: _dataX = (double[]) chanData; break;
                case Y: _dataY = (double[]) chanData; break;
                case Z: _dataZ = (double[]) chanData; break;
                case C: _dataC = (String[]) chanData; break;
                default: break;
            }
        }

        // Set PointCount, ArrayLen
        _pointCount = _dataX != null ? _dataX.length : _dataY.length;
        _arrayLen = _pointCount;
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _pointCount; }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        // If already set, just return
        if (aValue == _pointCount) return;

        // If silly value, complain and return
        if (aValue < 1 || aValue > MAX_POINT_COUNT) {
            System.err.println("DataSetImpl.setPointCount: Count exceeds arbitrary limit: " + aValue);
            return;
        }

        // Ensure capacity
        ensureCapacity(aValue);

        // Set PointCount
        _pointCount = aValue;
    }

    /**
     * Sets the array lengths.
     */
    private void ensureCapacity(int aMinCapacity)
    {
        // If set, just return
        if (aMinCapacity < _arrayLen) return;

        // Get DataType
        DataType dataType = getDataType();
        DataChan[] dataChans = dataType.getChannelsXY();

        // New length should be double old len (stick with factors of two)
        int newLen = 32;
        while (newLen < aMinCapacity) newLen *= 2;

        // Iterate over channels and ensure length
        for (DataChan chan : dataChans) {
            switch (chan) {
                case X: _dataX = _dataX != null ? Arrays.copyOf(_dataX, newLen) : new double[newLen]; break;
                case Y: _dataY = _dataY != null ? Arrays.copyOf(_dataY, newLen) : new double[newLen]; break;
                case Z: _dataZ = _dataZ != null ? Arrays.copyOf(_dataZ, newLen) : new double[newLen]; break;
                case C: _dataC = _dataC != null ? Arrays.copyOf(_dataC, newLen) : new String[newLen]; break;
                default: break;
            }
        }

        // Update array length
        _arrayLen = newLen;
    }

    /**
     * Adds a point for X and Y values.
     */
    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        // Make sure there is enough room
        int pointCount = getPointCount();
        ensureCapacity(pointCount + 1);

        // Get DataType
        DataType dataType = getDataType();
        DataChan[] dataChans = dataType.getChannelsXY();

        // If not adding to end, scoot trailing values down
        if (anIndex < pointCount) {
            int tailLen = pointCount - anIndex;
            for (DataChan chan : dataChans) {
                switch (chan) {
                    case X: System.arraycopy(_dataX, anIndex, _dataX, anIndex + 1, tailLen); break;
                    case Y: System.arraycopy(_dataY, anIndex, _dataY, anIndex + 1, tailLen); break;
                    case Z: System.arraycopy(_dataZ, anIndex, _dataZ, anIndex + 1, tailLen); break;
                    case C: System.arraycopy(_dataC, anIndex, _dataC, anIndex + 1, tailLen); break;
                    default: break;
                }
            }
        }

        // Update point
        _pointCount++;

        // Set channel values
        setPoint(aPoint, anIndex);
    }

    /**
     * Removes a point at given index.
     */
    @Override
    public void removePoint(int anIndex)
    {
        // Get DataType
        DataType dataType = getDataType();
        DataChan[] dataChans = dataType.getChannelsXY();
        int pointCount = getPointCount();

        // If not adding to end, scoot trailing values down
        if (anIndex < pointCount) {
            int nextIndex = anIndex + 1;
            int tailLen = pointCount - nextIndex;
            for (DataChan chan : dataChans) {
                switch (chan) {
                    case X: System.arraycopy(_dataX, nextIndex, _dataX, anIndex, tailLen); break;
                    case Y: System.arraycopy(_dataY, nextIndex, _dataY, anIndex, tailLen); break;
                    case Z: System.arraycopy(_dataZ, nextIndex, _dataZ, anIndex, tailLen); break;
                    case C: System.arraycopy(_dataC, nextIndex, _dataC, anIndex, tailLen); break;
                    default: break;
                }
            }
        }

        // Update point
        _pointCount--;

        // Notify pointsDidChange
        pointsDidChange();
    }

    /**
     * Sets a point for X and Y values.
     */
    @Override
    public void setPoint(DataPoint aPoint, int anIndex)
    {
        // Get DataType
        DataType dataType = getDataType();
        DataChan[] dataChans = dataType.getChannelsXY();

        // Set channel values
        for (DataChan chan : dataChans) {
            switch (chan) {
                case X: _dataX[anIndex] = aPoint.getX(); break;
                case Y: _dataY[anIndex] = aPoint.getY(); break;
                case Z: _dataZ[anIndex] = aPoint.getZ(); break;
                case C: _dataC[anIndex] = aPoint.getC(); break;
                default: break;
            }
        }

        // Notify pointsDidChange
        pointsDidChange();
    }

    /**
     * Clears all points.
     */
    public void clearPoints()
    {
        _pointCount = 0;
        pointsDidChange();
    }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        return _dataX != null ? _dataX[anIndex] : anIndex;
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        return _dataY != null ? _dataY[anIndex] : 0;
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        return _dataZ != null ? _dataZ[anIndex] : 0;
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        return _dataC != null ? _dataC[anIndex] : null;
    }

    /**
     * Sets the C value at given index.
     */
    public void setC(String aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Set new value
        _dataC[anIndex] = aValue;
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueX(int anIndex)
    {
        return _dataX !=null ? _dataX[anIndex] : null;
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Set new value
        _dataX[anIndex] = aValue;
        pointsDidChange();
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueY(int anIndex)
    {
        return _dataY != null ? _dataY[anIndex] : null;
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Set new value
        _dataY[anIndex] = aValue;
        pointsDidChange();
    }

    /**
     * Returns the Z value at given index.
     */
    public Double getValueZ(int anIndex)
    {
        return _dataZ != null ? _dataZ[anIndex] : null;
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Set new value
        _dataZ[anIndex] = aValue;
        pointsDidChange();
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        return _pointCount == 0;
    }

    /**
     * Override to copy arrays.
     */
    @Override
    public DataSet clone()
    {
        DataSetImpl clone = (DataSetImpl) super.clone();
        clone._dataX = _dataX != null ? _dataX.clone() : null;
        clone._dataY = _dataY != null ? _dataY.clone() : null;
        clone._dataZ = _dataZ != null ? _dataZ.clone() : null;
        clone._dataC = _dataC != null ? _dataC.clone() : null;
        return clone;
    }
}
