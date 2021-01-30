package snapcharts.model;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the cover class for holding the raw data.
 */
public class RawDataAsPoints extends RawData {

    // The values
    private List<RawPoint>  _points = new ArrayList<>();

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _points.size(); }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        // If silly value, just return
        if (aValue<1 || aValue>1000) return;

        // If not enough points, add
        while (aValue>getPointCount())
            addPoint(new DataPoint(null, null, null, null), getPointCount());

        // If too many points, remove
        while (aValue<getPointCount())
            removePoint(getPointCount()-1);
    }

    /**
     * Returns the data point at given index.
     */
    public RawPoint getPoint(int anIndex)
    {
        return anIndex<getPointCount() ? _points.get(anIndex) : null;
    }

    /**
     * Adds a point for X and Y values.
     */
    @Override
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        RawPoint dpnt = new RawPoint(aPoint.getValueX(), aPoint.getValueY(), aPoint.getValueZ(), aPoint.getC());
        addPoint(dpnt, anIndex);
    }

    /**
     * Adds a point.
     */
    public void addPoint(RawPoint aPoint, int anIndex)
    {
        // Add point
        aPoint._rawData = this;
        aPoint._index = anIndex;
        _points.add(anIndex, aPoint);

        // Notify pointsDidChange
        pointsDidChange();
    }

    /**
     * Removes a point at given index.
     */
    @Override
    public DataPoint removePoint(int anIndex)
    {
        RawPoint raw = _points.remove(anIndex);
        pointsDidChange();
        return new DataPoint(raw._x, raw._y, raw._z, raw._c);
    }

    /**
     * Sets point at given index to new point.
     */
    private void setPoint(RawPoint aPoint, int anIndex)
    {
        removePoint(anIndex);
        addPoint(aPoint, anIndex);
    }

    /**
     * Clears all points.
     */
    public void clearPoints()
    {
        _points.clear();
        pointsDidChange();
    }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getX() : 0;
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getY() : 0;
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getZ() : 0;
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getC() : null;
    }

    /**
     * Sets the C value at given index.
     */
    public void setC(String aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        RawPoint pnt = getPoint(anIndex).copyForC(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueX(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getValueX() : null;
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        RawPoint pnt = getPoint(anIndex).copyForX(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Returns the value at given index.
     */
    public Double getValueY(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getValueY() : null;
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        RawPoint pnt = getPoint(anIndex).copyForY(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public Double getValueZ(int anIndex)
    {
        RawPoint dp = getPoint(anIndex);
        return dp!=null ? dp.getValueZ() : null;
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        // Make sure we have enough points
        if (anIndex>=getPointCount()) setPointCount(anIndex+1);

        // Get old point, copy for new value, swap old point for new
        RawPoint pnt = getPoint(anIndex).copyForZ(aValue);
        setPoint(pnt, anIndex);
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        for (RawPoint dp : _points)
            if (dp.getValueY()!=null)
                return false;
        return true;
    }

    /**
     * A class to represent a raw data point.
     */
    private static class RawPoint {

        // The RawData that holds this DataPoint
        protected RawData  _rawData;

        // The index of point in data set
        protected int  _index;

        // The data point X/Y/Z values
        protected Double  _x, _y, _z;

        // The data point text value
        protected String  _c;

        /**
         * Constructor for XY.
         */
        public RawPoint(Double aX, Double aY, Double aZ, String aC)
        {
            _x = aX; _y = aY; _z = aZ; _c = aC;
        }

        /**
         * Returns the index of this point in dataset.
         */
        public int getIndex()  { return _index; }

        /**
         * Returns the X value.
         */
        public double getX()  { return _x!=null ? _x : _index; }

        /**
         * Returns the Y value.
         */
        public double getY()  { return _y!=null ? _y : 0; }

        /**
         * Returns the Z value.
         */
        public double getZ()  { return _z!=null ? _z : 0; }

        /**
         * Returns the name.
         */
        public String getC()  { return _c; }

        /**
         * Returns X as a Double.
         */
        public Double getValueX()  { return _x; }

        /**
         * Returns Y as a Double.
         */
        public Double getValueY()  { return _y; }

        /**
         * Returns Z as a Double.
         */
        public Double getValueZ()  { return _z; }

        /**
         * Copies this point with new X value.
         */
        public RawPoint copyForX(Double aX)
        {
            return new RawPoint(aX, _y, _z, _c);
        }

        /**
         * Copies this point with new Y value.
         */
        public RawPoint copyForY(Double aY)
        {
            return new RawPoint(_x, aY, _z, _c);
        }

        /**
         * Copies this point with new Z value.
         */
        public RawPoint copyForZ(Double aZ)
        {
            return new RawPoint(_x, _y, aZ, _c);
        }

        /**
         * Copies this point with new C value.
         */
        public RawPoint copyForC(String aStr)
        {
            return new RawPoint(_x, _y, _z, aStr);
        }

        /**
         * Standard equals implementation.
         */
        public boolean equals(Object anObj)
        {
            RawPoint other = anObj instanceof RawPoint ? (RawPoint)anObj : null; if (other==null) return false;
            return other._rawData==_rawData && other._index==_index;
        }
    }
}
