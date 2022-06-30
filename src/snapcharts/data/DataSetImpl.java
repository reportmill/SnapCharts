/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;

/**
 * This is the cover class for holding the raw data.
 */
public class DataSetImpl extends DataSet {

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
                if (dataArray instanceof NumberArray)
                    ((NumberArray) dataArray).addDouble(0);
                if (dataArray instanceof StringArray)
                    ((StringArray) dataArray).addString(null);
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
