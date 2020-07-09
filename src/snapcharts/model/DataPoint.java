package snapcharts.model;
import java.util.List;
import snap.util.SnapUtils;

/**
 * A class to represent a data point.
 */
public class DataPoint {
    
    // The series this point belongs to
    DataSeries   _series;
    
    // The data point name
    String       _name;
    
    // The data point x value (usually the index)
    Double       _x;
    
    // The data point y value
    Double       _y;
    
    // The index
    int          _index;
    
    /**
     * Returns the series.
     */
    public DataSeries getSeries()  { return _series; }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _series.getDataSet(); }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return getDataSet().getChart(); }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the X value.
     */
    public double getX()
    {
        if(_x!=null) return _x;
        _x = (double)(_series._dset.getSeriesStart() + _index);
        return _x;
    }

    /**
     * Returns the Y value.
     */
    public double getY()  { return _y!=null? _y : 0; }

    /**
     * Sets the Y value.
     */
    public void setY(Double aValue)
    {
        if(aValue==_y) return;
        _y = aValue;
        clearCache();
    }

    /**
     * Returns the index of this point in series.
     */
    public int getIndex()  { return _index; }

    /**
     * Return series name.
     */
    public String getSeriesName()  { return _series.getName(); }

    /**
     * Return index of data point series.
     */
    public int getSeriesIndex()  { return _series.getIndex(); }

    /**
     * Return index of data point series in dataset active series.
     */
    public int getSeriesActiveIndex()  { return _series.getActiveIndex(); }

    /**
     * Return data point key - either the name or the x value.
     */
    public Object getKey()
    {
        // If name, just return it
        if(_name!=null) return _name;

        // If categories, return that
        Chart chart = getChart();
        List <String> cats = chart.getAxisX().getCategories();
        if(cats!=null && getIndex()<cats.size())
            return cats.get(getIndex());

        // Otherwise return x val (as int, if whole number)
        double kval = getX();
        return kval==(int)kval ? (int)kval : kval;
    }

    /**
     * Return data point key as a string.
     */
    public String getKeyString()
    {
        Object key = getKey();
        return SnapUtils.stringValue(key);
    }

    /**
     * Returns whether value is set.
     */
    public boolean isValueSet()  { return _y!=null; }

    /**
     * Return series value.
     */
    public Double getValue()  { return _y; }

    /**
     * Sets series value.
     */
    public void setValue(Double aValue)  { setY(aValue); }

    /**
     * Return series value or zero (if null).
     */
    public double getValueX()  { return _y!=null? _y : 0; }

    /**
     * Clears cached values.
     */
    protected void clearCache()
    {
        if(_series!=null) _series.clearCache();
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        DataPoint other = anObj instanceof DataPoint? (DataPoint)anObj : null; if(other==null) return false;
        return other._series==_series && other._index==_index;
    }
}