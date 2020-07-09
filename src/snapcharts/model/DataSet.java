package snapcharts.model;
import java.util.*;

import snap.util.*;
import snapcharts.app.ChartView;
import snapcharts.app.Intervals;

/**
 * A class to manage a list of DataSeries.
 */
public class DataSet {

    // The Chart that owns this dataset
    private Chart  _chart;

    // The list of series
    List <DataSeries>  _series = new ArrayList();
    
    // The series start
    int                _seriesStart = 0;

    // The intervals
    Intervals _intervals = new Intervals(0, 4, 100);
    
    // Hold a subset of enabled series in this dataset
    DataSet            _active;
    
    // The cached min/max values
    double             _minVal = Float.MAX_VALUE, _maxVal = -Float.MAX_VALUE;

    // PropertyChangeSupport
    private PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String SeriesStart_Prop = "SeriesStart";

    /**
     * Creates a DataSet for given ChartView.
     */
    public DataSet(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Returns the series.
     */
    public List <DataSeries> getSeries()  { return _series; }

    /**
     * Returns whether series is empty.
     */
    public boolean isEmpty()  { return _series.isEmpty(); }

    /**
     * Returns the number of series.
     */
    public int getSeriesCount()  { return _series.size(); }

    /**
     * Sets the number of series.
     */
    public void setSeriesCount(int aValue)
    {
        // Ignore silly values
        if (aValue<1 || aValue>20) return;

        // If value larger than cound, create empty series
        while (aValue>getSeriesCount()) {
            DataSeries series = addSeriesForNameAndValues((String)null, (Double)null);
            series.setPointCount(getPointCount());
        }

        // If value smaller than count, remove series
        while (aValue<getSeriesCount())
            removeSeries(getSeriesCount()-1);
    }

    /**
     * Returns the individual series at given index.
     */
    public DataSeries getSeries(int anIndex)  { return _series.get(anIndex); }

    /**
     * Adds a new series.
     */
    public void addSeries(DataSeries aSeries)
    {
        aSeries._index = _series.size();
        _series.add(aSeries);
        aSeries._dset = this;
        clearCache();
    }

    /**
     * Removes the series at index.
     */
    public DataSeries removeSeries(int anIndex)
    {
        DataSeries series = _series.remove(anIndex);
        clearCache();
        return series;
    }

    /**
     * Removes the given series.
     */
    public int removeSeries(DataSeries aSeries)
    {
        int index = _series.indexOf(aSeries);
        if (index>=0) removeSeries(index);
        return index;
    }

    /**
     * Clears the series.
     */
    public void clear()
    {
        _series.clear();
        clearCache();
    }

    /**
     * Returns the minimum value for active series.
     */
    public double getMinValue()
    {
        if (_minVal<Float.MAX_VALUE) return _minVal;
        double minVal = Float.MAX_VALUE;
        for (DataSeries s : getSeries()) { double mval = s.getMinValue(); if (mval<minVal) minVal = mval; }
        return _minVal = minVal;
    }

    /**
     * Returns the maximum value for active series.
     */
    public double getMaxValue()
    {
        if (_maxVal>-Float.MAX_VALUE) return _maxVal;
        double maxVal = -Float.MAX_VALUE;
        for (DataSeries s : getSeries()) { double mval = s.getMaxValue(); if (mval>maxVal) maxVal = mval; }
        return _maxVal = maxVal;
    }

    /**
     * Adds a new series for given name and values.
     */
    public DataSeries addSeriesForNameAndValues(String aName, Double ... theVals)
    {
        DataSeries series = new DataSeries(); series.setName(aName);
        addSeries(series);
        series.setValues(theVals);
        return series;
    }

    /**
     * Returns the start of the series.
     */
    public int getSeriesStart()  { return _seriesStart; }

    /**
     * Sets the start of the series.
     */
    public void setSeriesStart(int aValue)
    {
        if (aValue==_seriesStart) return;
        firePropChange(SeriesStart_Prop, _seriesStart, _seriesStart=aValue);
        //_chartView.repaint();
    }

    /**
     * Returns the number of points in each series.
     */
    public int getPointCount()  { return _series.get(0).getPointCount(); }

    /**
     * Sets the point count.
     */
    public void setPointCount(int aValue)
    {
        for (DataSeries series : getSeries())
            series.setPointCount(aValue);
    }

    /**
     * Returns whether slice at point index is empty.
     */
    public boolean isSliceEmpty(int anIndex)
    {
        for (DataSeries ser : getSeries())
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
     * Returns the active dataset.
     */
    public DataSet getActiveSet()
    {
        if (_active!=null) return _active;

        // If all series are enabled, return this dataset
        int ac = 0; for (DataSeries s : _series) if (s.isEnabled()) ac++;
        if (ac==getSeriesCount()) return _active = this;

        // Create new dataset and initialize
        DataSet active = new DataSet(_chart);
        active._series = new ArrayList(ac);
        for (DataSeries s : _series) if (s.isEnabled()) active._series.add(s);
        active._seriesStart = _seriesStart;
        return _active = active;
    }

    /**
     * Returns the active series.
     */
    public List <DataSeries> getActiveSeries()  { return getActiveSet().getSeries(); }

    /**
     * Returns the intervals.
     */
    public Intervals getActiveIntervals(double aHeight)  { return getActiveSet().getIntervals(aHeight); }

    /**
     * Clears cached values.
     */
    protected void clearCache()
    {
        _active = null; _minVal = Float.MAX_VALUE; _maxVal = -Float.MAX_VALUE;

        System.out.println("DataSet.clearCache: Need to do something here");
        //_chartView.getChartArea().clearCache();
        //_chartView.reloadContents(false);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        if (_pcs== PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if (!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPC)  { _pcs.firePropChange(aPC); }

    /**
     * Add DeepChange listener.
     */
    public void addDeepChangeListener(DeepChangeListener aDCL)
    {
        if (_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addDeepChangeListener(aDCL);
    }

    /**
     * Remove DeepChange listener.
     */
    public void removeDeepChangeListener(DeepChangeListener aPCL)  { _pcs.removeDeepChangeListener(aPCL); }
}