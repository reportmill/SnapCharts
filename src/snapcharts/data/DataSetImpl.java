/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;

/**
 * This is the cover class for holding the raw data.
 */
public class DataSetImpl extends DataSet {

    // The DataArrays
    protected DataArray[]  _dataArrays;

    // The number of points
    private int  _pointCount;

    // Cached DataArrays for common channels X/Y/Z
    protected DataArrays.Number  _dataX, _dataY, _dataZ;

    // Cached DataArrays for common channel C
    protected DataArrays.String  _dataC;

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

        setDataArraysFromArrays(theValues);
    }

    /**
     * Sets DataArrays from data.
     */
    @Override
    public void setDataArraysFromArrays(Object ... theArrays)
    {
        // Get DataArrays
        _dataArrays = DataArray.newDataArraysForArrays(theArrays);

        // Set known arrays
        DataType dataType = getDataType();
        DataChan[] channels = dataType.getChannelsXY();
        int channelCount = channels.length;
        if (channelCount > _dataArrays.length) {
            _dataArrays = DataArray.newDataArraysForDataType(dataType);
            System.out.println("DataSetImpl.setDataArraysFromArrays: Missing data");
        }

        // Iterate over channels and set XYZ DataArrays
        for (int i = 0; i < channelCount; i++) {
            DataChan chan = channels[i];
            DataArray dataArray = _dataArrays[i];
            switch (chan) {
                case X: _dataX = (DataArrays.Number) dataArray; break;
                case Y: _dataY = (DataArrays.Number) dataArray; break;
                case Z: _dataZ = (DataArrays.Number) dataArray; break;
                case C: _dataC = (DataArrays.String) dataArray; break;
                default: break;
            }
        }

        // Set PointCount, ArrayLen
        _pointCount = _dataX != null ? _dataX.getLength() : _dataY.getLength();
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

        // Add points
        while (aValue < _pointCount) {
            for (DataArray dataArray : _dataArrays) {
                Class componentType = dataArray.getComponentType();
                if (componentType == float.class)
                    dataArray.addFloat(0);
                else if (componentType == double.class)
                    dataArray.addDouble(0);
                else if (componentType == String.class)
                    dataArray.addString(null);
            }
            _pointCount++;
        }
    }

    /**
     * Adds a point for X and Y values.
     */
    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        // Get DataType
        DataType dataType = getDataType();
        DataChan[] dataChans = dataType.getChannelsXY();

        // Set channel values
        for (DataChan chan : dataChans) {
            switch (chan) {
                case X: _dataX.addDouble(aPoint.getX(), anIndex); break;
                case Y: _dataY.addDouble(aPoint.getY(), anIndex); break;
                case Z: _dataZ.addDouble(aPoint.getZ(), anIndex); break;
                case C: _dataC.addString(aPoint.getC(), anIndex); break;
                default: break;
            }
        }

        // Update point
        _pointCount++;
    }

    /**
     * Removes a point at given index.
     */
    @Override
    public void removePoint(int anIndex)
    {
        // Iterate over DataArrays and remove index
        for (DataArray dataArray : _dataArrays)
            dataArray.removeIndex(anIndex);

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
                case X: _dataX.setDouble(aPoint.getX(), anIndex); break;
                case Y: _dataY.setDouble(aPoint.getY(), anIndex); break;
                case Z: _dataZ.setDouble(aPoint.getZ(), anIndex); break;
                case C: _dataC.setString(aPoint.getC(), anIndex); break;
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
        // Iterate over DataArrays and reset length
        for (DataArray dataArray : _dataArrays)
            dataArray.setLength(0);

        _pointCount = 0;
        pointsDidChange();
    }

    /**
     * Returns an array of dataset X values.
     */
    @Override
    public DataArrays.Number getDataArrayX()  { return _dataX; }

    /**
     * Returns an array of dataset Y values.
     */
    @Override
    public DataArrays.Number getDataArrayY()  { return _dataY; }

    /**
     * Returns an array of dataset Z values.
     */
    @Override
    public DataArrays.Number getDataArrayZ()  { return _dataZ; }

    /**
     * Returns an array of dataset C values.
     */
    @Override
    public DataArrays.String getDataArrayC()  { return _dataC; }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        return _dataX != null ? _dataX.getDouble(anIndex) : anIndex;
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        return _dataY != null ? _dataY.getDouble(anIndex) : 0;
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        return _dataZ != null ? _dataZ.getDouble(anIndex) : 0;
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        return _dataC != null ? _dataC.getString(anIndex) : null;
    }

    /**
     * Sets the C value at given index.
     */
    public void setC(String aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex >= getPointCount())
            setPointCount(anIndex + 1);

        // Set new value
        _dataC.setString(aValue, anIndex);
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueX(int anIndex)
    {
        return _dataX != null ? _dataX.getDouble(anIndex) : null;
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex >= getPointCount())
            setPointCount(anIndex + 1);

        // Set new value
        _dataX.setDouble(aValue, anIndex);
        pointsDidChange();
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueY(int anIndex)
    {
        return _dataY != null ? _dataY.getDouble(anIndex) : null;
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex >= getPointCount())
            setPointCount(anIndex + 1);

        // Set new value
        _dataY.setDouble(aValue, anIndex);
        pointsDidChange();
    }

    /**
     * Returns the Z value at given index.
     */
    public Double getValueZ(int anIndex)
    {
        return _dataZ != null ? _dataZ.getDouble(anIndex) : null;
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex >= getPointCount())
            setPointCount(anIndex + 1);

        // Set new value
        _dataZ.setDouble(aValue, anIndex);
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
