package snapcharts.model;

/**
 * A class to represent a data point.
 */
public class DataPoint {
    
    // The DataSet this point belongs to
    protected DataSet  _dset;

    // The index of point in data set
    protected int  _index;

    // The data point x value
    protected Double  _x;
    
    // The data point y value
    protected Double  _y;

    // The data point text value
    protected String  _c;

    /**
     * Constructor for XY.
     */
    public DataPoint(Double aX, Double aY)  { _x = aX; _y = aY; }

    /**
     * Constructor for CY.
     */
    public DataPoint(String aStr, Double aY)  { _c = aStr; _y = aY; }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dset; }

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
     * Copies this point with new X value.
     */
    public DataPoint copyForX(Double aX)
    {
        DataPoint copy = new DataPoint(aX, _y); copy._c = _c;
        return copy;
    }

    /**
     * Copies this point with new Y value.
     */
    public DataPoint copyForY(Double aY)
    {
        DataPoint copy = new DataPoint(_x, aY); copy._c = _c;
        return copy;
    }

    /**
     * Copies this point with new C value.
     */
    public DataPoint copyForC(String aStr)
    {
        DataPoint copy = new DataPoint(_x, _y); copy._c = aStr;
        return copy;
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        DataPoint other = anObj instanceof DataPoint ? (DataPoint)anObj : null; if (other==null) return false;
        return other._dset == _dset && other._index==_index;
    }
}