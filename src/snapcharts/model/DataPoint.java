package snapcharts.model;
import java.util.List;
import snap.util.SnapUtils;

/**
 * A class to represent a data point.
 */
public class DataPoint {
    
    // The DataSet this point belongs to
    protected DataSet  _dset;
    
    // The data point name
    protected String  _name;
    
    // The data point x value (usually the index)
    protected Double  _x;
    
    // The data point y value
    protected Double  _y;
    
    // The index
    protected int  _index;
    
    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dset; }

    /**
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()  { return _dset.getDataSetList(); }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return getDataSetList().getChart(); }

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
        _x = (double)(_dset._dsetList.getStartValue() + _index);
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
     * Returns the index of this point in dataset.
     */
    public int getIndex()  { return _index; }

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
     * Return data point value.
     */
    public Double getValue()  { return _y; }

    /**
     * Sets data point value.
     */
    public void setValue(Double aValue)  { setY(aValue); }

    /**
     * Return data point value or zero (if null).
     */
    public double getValueX()  { return _y!=null ? _y : 0; }

    /**
     * Clears cached values.
     */
    protected void clearCache()
    {
        if(_dset !=null) _dset.clearCache();
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        DataPoint other = anObj instanceof DataPoint ? (DataPoint)anObj : null; if(other==null) return false;
        return other._dset == _dset && other._index==_index;
    }
}