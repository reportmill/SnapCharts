package snapcharts.model;
import snap.util.Range;

/**
 * A RawData implementation that wraps around another RawData and wraps extends its data to any min/max.
 */
public class RawDataWrapper extends RawData {

    // The wrapped RawData
    private RawData  _rawData;

    // The raw min/max X
    private double  _rawMinX, _rawMaxX;

    // The wrap min/max X
    private double  _wrapMinX, _wrapMaxX;

    // The raw pointCount
    private int  _rawPointCount;

    // The wrap pointCount
    private int  _wrapPointCount;

    // The start index in wrapped RawData
    private int  _start;

    // The end index in wrapped RawData
    private int  _end;

    /**
     * Constructor.
     */
    public RawDataWrapper(RawData aRawData, double aRawMinX, double aRawMaxX, double aWrapMinX, double aWrapMaxX)
    {
        _rawData = aRawData;
        _rawPointCount = aRawData.getPointCount();
        _rawMinX = aRawMinX;
        _rawMaxX = aRawMaxX;
        _wrapMinX = aWrapMinX;
        _wrapMaxX = aWrapMaxX;

        // Get cycle range
        Range cycleRange = getCycleRange(aRawMinX, aRawMaxX, aWrapMinX, aWrapMaxX);
        _start = cycleRange.start * _rawPointCount;
        _end = cycleRange.end * _rawPointCount;
        _wrapPointCount = _end - _start;
    }

    /**
     * Override to forward to RawData.
     */
    @Override
    public int getPointCount()
    {
        return _wrapPointCount;
    }

    @Override
    public void setPointCount(int aValue)
    {

    }

    @Override
    public double getX(int anIndex)
    {
        // Get index of wrapped RawData point and get value X
        int rawIndex = Math.floorMod(_start + anIndex, _rawPointCount);
        double valX = _rawData.getX(rawIndex);

        // Get cycle index and shift value
        int cycle = (_start + anIndex) / _rawPointCount;
        valX += cycle * (_rawMaxX - _rawMinX);
        return valX;
    }

    @Override
    public double getY(int anIndex)
    {
        // Get index of wrapped RawData point and get value Y
        int rawIndex = Math.floorMod(_start + anIndex, _rawPointCount);
        double valY = _rawData.getY(rawIndex);
        return valY;
    }

    @Override
    public double getZ(int anIndex)
    {
        return 0;
    }

    @Override
    public String getC(int anIndex)
    {
        return null;
    }

    @Override
    public void setC(String aValue, int anIndex)
    {

    }

    @Override
    public Double getValueX(int anIndex)
    {
        return null;
    }

    @Override
    public void setValueX(Double aValue, int anIndex)
    {

    }

    @Override
    public Double getValueY(int anIndex)
    {
        return null;
    }

    @Override
    public void setValueY(Double aValue, int anIndex)
    {

    }

    @Override
    public Double getValueZ(int anIndex)
    {
        return null;
    }

    @Override
    public void setValueZ(Double aValue, int anIndex)
    {

    }

    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {

    }

    @Override
    public void removePoint(int anIndex)
    {

    }

    @Override
    public void clearPoints()
    {
        _rawData.clearPoints();
    }

    @Override
    public boolean isClear()
    {
        return _rawData.isClear();
    }

    /**
     * Returns the cycle range of a given min/max inside of another min/max.
     */
    private static Range getCycleRange(double aMin1, double aMax1, double aMin2, double aMax2)
    {
        double range1 = aMax1 - aMin1;

        // Caculate cycle start
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
}
