/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.Range;

/**
 * A DataSet implementation that wraps around another DataSet and extends its data to any min/max.
 */
public class DataSetWrapper extends DataSet {

    // The wrapped DataSet
    private DataSet  _dataSet;

    // The raw min/max X
    private double  _rawMinX, _rawMaxX;

    // The raw pointCount
    private int  _rawPointCount;

    // The wrap pointCount
    private int  _wrapPointCount;

    // The start index in wrapped DataSet
    private int  _start;

    // The end index in wrapped DataSet
    private int  _end;

    // The maximum number of wrapped points allowed
    private static int MAX_WRAPPED_POINTS = 10000;

    /**
     * Constructor.
     */
    public DataSetWrapper(DataSet aDataSet, double aRawMinX, double aRawMaxX, double aWrapMinX, double aWrapMaxX)
    {
        // Get DataSet info
        _dataSet = aDataSet;
        _dataArrays = _dataSet.getDataArrays();
        _dataX = _dataSet.getDataArrayX();
        _dataY = _dataSet.getDataArrayY();
        _dataZ = _dataSet.getDataArrayZ();
        _dataC = _dataSet.getDataArrayC();

        // Get info
        _rawPointCount = aDataSet.getPointCount();
        _rawMinX = aRawMinX;
        _rawMaxX = aRawMaxX;

        // Get cycle range (make sure cycles * points doesn't exceed MAX_WRAPPED_POINTS)
        Range cycleRange = getCycleRange(aRawMinX, aRawMaxX, aWrapMinX, aWrapMaxX);
        while (cycleRange.getLength() > 2 && cycleRange.length * _rawPointCount > MAX_WRAPPED_POINTS)
            cycleRange = new Range(cycleRange.getStart() + 1, cycleRange.getEnd() - 1);

        // Set point start/end indexes
        _start = cycleRange.start * _rawPointCount;
        _end = cycleRange.end * _rawPointCount;

        // Calc/set total number of wrapped points
        _wrapPointCount = _end - _start;
    }

    /**
     * Override to return point count for wrapped data.
     */
    @Override
    public int getPointCount()
    {
        return _wrapPointCount;
    }

    @Override
    public double getX(int anIndex)
    {
        // Get index of wrapped DataSet point and get value X
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        double valX = _dataSet.getX(rawIndex);

        // Get cycle index and shift value
        int cycle = floorDiv(_start + anIndex, _rawPointCount);
        valX += cycle * (_rawMaxX - _rawMinX);
        return valX;
    }

    @Override
    public double getY(int anIndex)
    {
        // Get index of wrapped DataSet point and get value Y
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        double valY = _dataSet.getY(rawIndex);
        return valY;
    }

    @Override
    public void setPointCount(int aValue)
    {
        throw new RuntimeException("DataSetWrapper: setPointCount not implemented");
    }

    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        throw new RuntimeException("DataSetWrapper: addPoint not implemented");
    }

    @Override
    public void removePoint(int anIndex)
    {
        throw new RuntimeException("DataSetWrapper: removePoint not implemented");
    }

    @Override
    public void setPoint(DataPoint aPoint, int anIndex)
    {
        throw new RuntimeException("DataSetWrapper: setPoint not implemented");
    }

    @Override
    public void clearPoints()
    {
        _dataSet.clearPoints();
    }

    @Override
    public boolean isClear()
    {
        return _dataSet.isClear();
    }

    /**
     * Returns the cycle range of a given min/max inside of another min/max.
     */
    private static Range getCycleRange(double aMin1, double aMax1, double aMin2, double aMax2)
    {
        double range1 = aMax1 - aMin1;

        // Calculate cycle start
        int cycleStart = 0;
        for (double min1 = aMin1; min1 > aMin2; min1 -= range1)
            cycleStart--;
        for (double min1 = aMin1; min1 + range1 < aMin2; min1 += range1)
            cycleStart++;

        // Calculate cycle end
        int cycleEnd = 1;
        for (double max1 = aMax1; max1 < aMax2; max1 += range1)
            cycleEnd++;
        for (double max1 = aMax1; max1 - range1 > aMax2; max1 -= range1)
            cycleEnd--;

        // Return cycle range
        return new Range(cycleStart, cycleEnd);
    }

    /**
     * This should just be Math.floorMod, but TeaVM doesn't yet support this.
     */
    private static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    /**
     * This should just be Math.floorDiv, but TeaVM doesn't yet support this.
     */
    private static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) < 0 && (r * y != x))
            r--;
        return r;
    }
}
