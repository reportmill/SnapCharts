package snapcharts.model;
import snap.util.Range;

/**
 * A DataStore implementation that wraps around another DataStore and extends its data to any min/max.
 */
public class DataStoreWrapper extends DataStore {

    // The wrapped DataStore
    private DataStore _dataStore;

    // The raw min/max X
    private double  _rawMinX, _rawMaxX;

    // The wrap min/max X
    private double  _wrapMinX, _wrapMaxX;

    // The raw pointCount
    private int  _rawPointCount;

    // The wrap pointCount
    private int  _wrapPointCount;

    // The start index in wrapped DataStore
    private int  _start;

    // The end index in wrapped DataStore
    private int  _end;

    // The maximum number of wrapped points allowed
    private static int MAX_WRAPPED_POINTS = 10000;

    /**
     * Constructor.
     */
    public DataStoreWrapper(DataStore aDataStore, double aRawMinX, double aRawMaxX, double aWrapMinX, double aWrapMaxX)
    {
        // Get info
        _dataStore = aDataStore;
        _rawPointCount = aDataStore.getPointCount();
        _rawMinX = aRawMinX;
        _rawMaxX = aRawMaxX;
        _wrapMinX = aWrapMinX;
        _wrapMaxX = aWrapMaxX;

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
        // Get index of wrapped DataStore point and get value X
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        double valX = _dataStore.getX(rawIndex);

        // Get cycle index and shift value
        int cycle = floorDiv(_start + anIndex, _rawPointCount);
        valX += cycle * (_rawMaxX - _rawMinX);
        return valX;
    }

    @Override
    public double getY(int anIndex)
    {
        // Get index of wrapped DataStore point and get value Y
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        double valY = _dataStore.getY(rawIndex);
        return valY;
    }

    @Override
    public double getZ(int anIndex)
    {
        // Get index of wrapped DataStore point and get value Y
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        double valZ = _dataStore.getZ(rawIndex);
        return valZ;
    }

    @Override
    public String getC(int anIndex)
    {
        // Get index of wrapped DataStore point and get value Y
        int rawIndex = floorMod(_start + anIndex, _rawPointCount);
        String valC = _dataStore.getC(rawIndex);
        return valC;
    }

    @Override
    public Double getValueX(int anIndex)
    {
        return getX(anIndex);
    }

    @Override
    public Double getValueY(int anIndex)
    {
        return getY(anIndex);
    }

    @Override
    public Double getValueZ(int anIndex)
    {
        return getZ(anIndex);
    }

    @Override
    public void setPointCount(int aValue)
    {
        throw new RuntimeException("DataStoreWrapper: setPointCount not implemented");
    }

    @Override
    public void setC(String aValue, int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: setC not implemented");
    }

    @Override
    public void setValueX(Double aValue, int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: setValueX not implemented");
    }

    @Override
    public void setValueY(Double aValue, int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: setValueY not implemented");
    }

    @Override
    public void setValueZ(Double aValue, int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: setValueZ not implemented");
    }

    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: addPoint not implemented");
    }

    @Override
    public void removePoint(int anIndex)
    {
        throw new RuntimeException("DataStoreWrapper: removePoint not implemented");
    }

    @Override
    public void clearPoints()
    {
        _dataStore.clearPoints();
    }

    @Override
    public boolean isClear()
    {
        return _dataStore.isClear();
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
