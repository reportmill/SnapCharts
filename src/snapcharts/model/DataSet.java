package snapcharts.model;

import java.util.*;

/**
 * A class to represent a list of data points.
 */
public class DataSet {
    
    // The DataSetList that owns this dataset
    protected DataSetList  _dsetList;

    // The name
    private String  _name;
    
    // The values
    private List <DataPoint>  _points = new ArrayList<>();
    
    // The index in data set
    protected int  _index;
    
    // Whether dataset is disabled
    private boolean  _disabled;
    
    // Cached array of values, ratios, total
    private double  _vals[], _ratios[], _total;

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return _dsetList; }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _dsetList !=null ? _dsetList.getChart() : null; }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aValue)
    {
        _name = aValue;
        clearCache();
    }

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
        while(aValue>getPointCount())
            addPoint(null, null);

        // If too many points, remove
        while(aValue<getPointCount())
            removePoint(getPointCount()-1);
    }

    /**
     * Returns the data points list.
     */
    public List <DataPoint> getPoints()  { return _points; }

    /**
     * Returns the data point at given index.
     */
    public DataPoint getPoint(int anIndex)
    {
        return anIndex<getPointCount()? _points.get(anIndex) : null;
    }

    /**
     * Adds a point.
     */
    public void addPoint(DataPoint aPoint)
    {
        aPoint._dset = this; aPoint._index = getPointCount();
        _points.add(aPoint);
        clearCache();
    }

    /**
     * Removes a point at given index.
     */
    public DataPoint removePoint(int anIndex)
    {
        DataPoint dpnt = _points.remove(anIndex);
        clearCache();
        return dpnt;
    }

    /**
     * Adds a point for name and value.
     */
    public void addPoint(String aName, Double aValue)
    {
        DataPoint dpnt = new DataPoint();
        dpnt._name = aName; dpnt._y = aValue; addPoint(dpnt);
    }

    /**
     * Returns the value at given index.
     */
    public Double getValue(int anIndex)
    {
        DataPoint dp = getPoint(anIndex); return dp!=null? dp.getValue() : null;
    }

    /**
     * Returns the value at given index.
     */
    public double getValueX(int anIndex)
    {
        DataPoint dp = getPoint(anIndex); return dp!=null? dp.getValueX() : 0;
    }

    /**
     * Sets the value at given index.
     */
    public void setValue(Double aValue, int anIndex)
    {
        while(anIndex>=getPointCount()) addPoint(null, null);
        DataPoint dpnt = getPoint(anIndex);
        dpnt.setValue(aValue);
    }

    /**
     * Sets the values.
     */
    public void setValues(Double ... theVals)
    {
        _points.clear();
        for (Double v : theVals) addPoint(null, v);
    }

    /**
     * Returns the total of all values.
     */
    public double getTotal()
    {
        if (_vals==null) getValues();
        return _total;
    }

    /**
     * Returns an array of dataset values.
     */
    public double[] getValues()
    {
        if (_vals!=null) return _vals;
        int count = getPointCount(); _total = 0;
        double vals[] = new double[count];
        for (int i=0;i<count;i++) { double v = getValueX(i); vals[i] = v; _total += v; }
        return _vals = vals;
    }

    /**
     * Returns an array of dataset ratios.
     */
    public double[] getRatios()
    {
        if (_ratios!=null) return _ratios;
        double vals[] = getValues();
        double total = getTotal();
        int count = vals.length;
        double ratios[] = new double[count];
        for (int i=0;i<count;i++) ratios[i] = vals[i]/total;
        return _ratios = ratios;
    }

    /**
     * Returns the index in dataset.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the index in dataset active dataset.
     */
    public int getActiveIndex()  { return _dsetList.getActiveDataSets().indexOf(this); }

    /**
     * Returns whether this dataset is disabled.
     */
    public boolean isDisabled()  { return _disabled; }

    /**
     * Sets whether this dataset is disabled.
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue==isDisabled()) return;
        _disabled = aValue;
        _dsetList.clearCache();

        // if Pie chart, clear other
        boolean isPie = getChart().getType() == ChartType.PIE;
        if ((!aValue) && isPie) {
            for (DataSet s : _dsetList.getDataSets())
                s.setDisabled(s!=this);
        }
    }

    /**
     * Returns whether this dataset is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Returns the minimum value in this dataset.
     */
    public double getMinValue()
    {
        double minVal = Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getValueX()<minVal)
                minVal = dp.getValueX();
        return minVal;
    }

    /**
     * Returns the maximum value in this dataset.
     */
    public double getMaxValue()
    {
        double maxVal = -Float.MAX_VALUE;
        for (DataPoint dp : _points)
            if (dp.getValueX()>maxVal)
                maxVal = dp.getValueX();
        return maxVal;
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0) return false;
        for (DataPoint dp : getPoints())
            if (dp.isValueSet())
                return false;
        return true;
    }

    /**
     * Clears cached values.
     */
    protected void clearCache()
    {
        _vals = _ratios = null;
        if (_dsetList !=null) _dsetList.clearCache();
    }
}