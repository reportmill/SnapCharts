package snapcharts.model;
import java.util.*;

import snap.util.*;

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
    private double _minY = Float.MAX_VALUE;

    // The cached max values
    private double _maxY = -Float.MAX_VALUE;

    // Constants for properties
    public static final String StartValue_Prop = "StartValue";
    public static final String DataSet_Prop = "DataSet";

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

        // Reset indexes
        for (int i=0; i<_dsets.size(); i++)
            getDataSet(i)._index = i;
        clearCachedValues();

        // FirePropChange
        firePropChange(DataSet_Prop, null, aDataSet, anIndex);
    }

    /**
     * Removes the dataset at index.
     */
    public DataSet removeDataSet(int anIndex)
    {
        DataSet dset = _dsets.remove(anIndex);
        clearCachedValues();
        if (dset!=null)
            firePropChange(DataSet_Prop, dset, null, anIndex);
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
     * Returns the minimum Y value for active datasets.
     */
    public double getMinY()
    {
        // If value already cached, just return
        if (_minY < Float.MAX_VALUE) return _minY;

        // If no datasets/points, just set to 0
        if (getPointCount()==0) return _minY = 0;

        // Iterate over datasets to get min of all
        double min = Float.MAX_VALUE;
        for (DataSet dset : getDataSets())
            min = Math.min(min, dset.getMinY());
        return _minY = min;
    }

    /**
     * Returns the maximum Y value for active datasets.
     */
    public double getMaxY()
    {
        // If value already cached, just return
        if (_maxY > -Float.MAX_VALUE) return _maxY;

        // If no datasets, just set to 0
        if (getPointCount()==0) return _maxY = 5;

        // Iterate over datasets to get max of all
        double max = -Float.MAX_VALUE;
        for (DataSet dset : getDataSets())
            max = Math.max(max, dset.getMaxY());
        return _maxY = max;
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
    public int getPointCount()
    {
        int pc = Integer.MAX_VALUE; if (getDataSetCount()==0) return 0;
        for (DataSet dset : getDataSets()) pc = Math.min(dset.getPointCount(), pc);
        return pc;
    }

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
        for (DataSet dset : getDataSets())
            if (dset.getPoint(anIndex).getValueY()!=null)
                return false;
        return true;
    }

    /**
     * Returns the intervals for Y axis.
     */
    public Intervals getIntervalsY(double aHeight)
    {
        // Get chart min value, max value and height
        double minY = getMinY();
        double maxY = getMaxY();

        // If ShowPartialY, reset min or max
        if (!_chart.isShowPartialY() && minY*maxY>0) {
            if (minY>0) minY = 0;
            else maxY = 0;
        }

        // If intervals are cached for current min, max and height, return them
        double seedMax = _intervals.getSeedValueMax();
        double seedMin = _intervals.getSeedValueMin();
        double seedHeight = _intervals.getSeedHeight();
        if (MathUtils.equals(seedMax, maxY) && MathUtils.equals(seedMin, minY) && MathUtils.equals(seedHeight, aHeight))
            return _intervals;

        // Create new intervals and return
        _intervals = new Intervals(minY, maxY, aHeight);
        return _intervals;
    }

    /**
     * Returns a DataSetList of active datasets.
     */
    public DataSetList getActiveList()
    {
        // If already cached, just return it
        if (_active!=null) return _active;

        // If all datasets are enabled, return this dataset
        int ac = 0; for (DataSet dset : _dsets) if (dset.isEnabled()) ac++;
        if (ac== getDataSetCount())
            return _active = this;

        // Create new dataset and initialize
        DataSetList active = new DataSetList(_chart);
        active._dsets = new ArrayList(ac);
        for (DataSet dset : _dsets)
            if (dset.isEnabled())
                active._dsets.add(dset);
        active._startValue = _startValue;
        return _active = active;
    }

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
        _minY = Float.MAX_VALUE;
        _maxY = -Float.MAX_VALUE;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive DataSets
        for (DataSet dset : getDataSets())
            e.add(anArchiver.toXML(dset));

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive DataSets
        List<XMLElement> dsetXMLs = anElement.getElements(DataSet.class.getSimpleName());
        for (XMLElement dsetXML : dsetXMLs) {
            DataSet dset = (DataSet)anArchiver.fromXML(dsetXML, this);
            if (dset!=null)
                addDataSet(dset);
        }

        // Return this part
        return this;
    }
}