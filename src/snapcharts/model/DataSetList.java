package snapcharts.model;
import java.util.*;

import snap.util.*;
import snapcharts.app.Intervals;

/**
 * A class to manage a list of DataSets.
 */
public class DataSetList extends ChartPart {

    // The list of datasets
    private List <DataSet>  _dsets = new ArrayList<>();
    
    // The dataset start value
    private int _startValue = 0;

    // The intervals
    private Intervals  _intervals = new Intervals(0, 4, 100);
    
    // A DataSetList to hold subset of enabled datasets
    private DataSetList  _active;
    
    // The cached min values
    private double  _minVal = Float.MAX_VALUE;

    // The cached max values
    private double  _maxVal = -Float.MAX_VALUE;

    // Constants for properties
    public static final String StartValue_Prop = "StartValue";

    /**
     * Creates a DataSet for given ChartView.
     */
    public DataSetList(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the list of datasets.
     */
    public List <DataSet> getDataSets()  { return _dsets; }

    /**
     * Returns whether no datasets.
     */
    public boolean isEmpty()  { return _dsets.isEmpty(); }

    /**
     * Returns the number of datasets.
     */
    public int getDataSetCount()  { return _dsets.size(); }

    /**
     * Sets the number of datasets.
     */
    public void setDataSetCount(int aValue)
    {
        // Ignore silly values
        if (aValue<1 || aValue>20) return;

        // If value larger than cound, create empty dataset
        while (aValue> getDataSetCount()) {
            DataSet dset = addDataSetForNameAndValues((String)null, (Double)null);
            dset.setPointCount(getPointCount());
        }

        // If value smaller than count, remove dataset
        while (aValue< getDataSetCount())
            removeDataSet(getDataSetCount()-1);
    }

    /**
     * Returns the individual dataset at given index.
     */
    public DataSet getDataSet(int anIndex)  { return _dsets.get(anIndex); }

    /**
     * Adds a new dataset.
     */
    public void addDataSet(DataSet aDataSet)
    {
        addDataSet(aDataSet, getDataSetCount());
    }

    /**
     * Adds a new dataset.
     */
    public void addDataSet(DataSet aDataSet, int anIndex)
    {
        // Add DataSet at index
        _dsets.add(anIndex, aDataSet);
        aDataSet._dsetList = this;
        aDataSet.addPropChangeListener(pc -> dataSetDidPropChange(pc));

        // Reset index
        for (int i=0; i<_dsets.size(); i++)
            getDataSet(i)._index = i;
        clearCachedValues();
    }

    /**
     * Removes the dataset at index.
     */
    public DataSet removeDataSet(int anIndex)
    {
        DataSet dset = _dsets.remove(anIndex);
        clearCachedValues();
        return dset;
    }

    /**
     * Removes the given dataset.
     */
    public int removeDataSet(DataSet aDataSet)
    {
        int index = _dsets.indexOf(aDataSet);
        if (index>=0) removeDataSet(index);
        return index;
    }

    /**
     * Clears the datasets.
     */
    public void clear()
    {
        _dsets.clear();
        clearCachedValues();
    }

    /**
     * Returns the minimum value for active datasets.
     */
    public double getMinValue()
    {
        if (_minVal<Float.MAX_VALUE) return _minVal;
        double minVal = Float.MAX_VALUE;
        for (DataSet s : getDataSets()) { double mval = s.getMinValue(); if (mval<minVal) minVal = mval; }
        return _minVal = minVal;
    }

    /**
     * Returns the maximum value for active datasets.
     */
    public double getMaxValue()
    {
        if (_maxVal>-Float.MAX_VALUE) return _maxVal;
        double maxVal = -Float.MAX_VALUE;
        for (DataSet s : getDataSets()) { double mval = s.getMaxValue(); if (mval>maxVal) maxVal = mval; }
        return _maxVal = maxVal;
    }

    /**
     * Adds a new dataset for given name and values.
     */
    public DataSet addDataSetForNameAndValues(String aName, Double ... theVals)
    {
        DataSet dset = new DataSet();
        dset.setName(aName);
        addDataSet(dset);
        dset.setValues(theVals);
        return dset;
    }

    /**
     * Returns the start value of the dataset.
     */
    public int getStartValue()  { return _startValue; }

    /**
     * Sets the start of the dataset.
     */
    public void setStartValue(int aValue)
    {
        if (aValue== _startValue) return;
        firePropChange(StartValue_Prop, _startValue, _startValue =aValue);
    }

    /**
     * Returns the number of points in datasets.
     */
    public int getPointCount()  { return _dsets.get(0).getPointCount(); }

    /**
     * Sets the point count.
     */
    public void setPointCount(int aValue)
    {
        for (DataSet dset : getDataSets())
            dset.setPointCount(aValue);
    }

    /**
     * Returns whether slice at point index is empty.
     */
    public boolean isSliceEmpty(int anIndex)
    {
        for (DataSet ser : getDataSets())
            if (ser.getPoint(anIndex).isValueSet())
                return false;
        return true;
    }

    /**
     * Returns the intervals.
     */
    public Intervals getIntervals(double aHeight)
    {
        // Get chart min value, max value and height
        double minVal = getMinValue();
        double maxVal = getMaxValue();
        double height = aHeight; //_chartView._chartArea.getHeight() - _chartView._chartArea.getInsetsAll().getHeight();
        if (!_chart.isShowPartialY() && minVal*maxVal>0) {
            if (minVal>0) minVal = 0; else maxVal = 0; }

        // If intervals are cached for current min, max and height, return them
        double seedMax = _intervals.getSeedValueMax(), seedMin = _intervals.getSeedValueMin();
        double seedHeight = _intervals.getSeedHeight();
        if (MathUtils.equals(seedMax, maxVal) && MathUtils.equals(seedMin, minVal) && MathUtils.equals(seedHeight, height))
            return _intervals;

        // Create new intervals and return
        _intervals = new Intervals(minVal, maxVal, height);
        return _intervals;
    }

    /**
     * Returns a DataSetList of active datasets.
     */
    public DataSetList getActiveDataSetList()
    {
        if (_active!=null) return _active;

        // If all datasets are enabled, return this dataset
        int ac = 0; for (DataSet s : _dsets) if (s.isEnabled()) ac++;
        if (ac== getDataSetCount()) return _active = this;

        // Create new dataset and initialize
        DataSetList active = new DataSetList(_chart);
        active._dsets = new ArrayList(ac);
        for (DataSet s : _dsets) if (s.isEnabled()) active._dsets.add(s);
        active._startValue = _startValue;
        return _active = active;
    }

    /**
     * Returns the active datasets.
     */
    public List <DataSet> getActiveDataSets()  { return getActiveDataSetList().getDataSets(); }

    /**
     * Returns the intervals.
     */
    public Intervals getActiveIntervals(double aHeight)  { return getActiveDataSetList().getIntervals(aHeight); }

    /**
     * Called when a DataSet changes a property.
     */
    private void dataSetDidPropChange(PropChange aPC)
    {
        clearCachedValues();

        // Handle Disabled: if Pie chart, clear other
        String prop = aPC.getPropName();
        if (prop==DataSet.Disabled_Prop) {
            DataSet dset = (DataSet)aPC.getSource();
            boolean isPie = getChart().getType() == ChartType.PIE;
            if (isPie && !dset.isDisabled()) {
                for (DataSet ds : getDataSets())
                    ds.setDisabled(ds != dset);
            }
        }

        // Fire PropChange
        firePropChange(aPC);
    }

    /**
     * Clears cached values.
     */
    private void clearCachedValues()
    {
        _active = null;
        _minVal = Float.MAX_VALUE;
        _maxVal = -Float.MAX_VALUE;
    }
}