package snapcharts.model;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import snap.util.*;

/**
 * A class to manage a list of DataSets.
 */
public class DataSetList extends ChartPart {

    // The list of datasets
    private List <DataSet>  _dsets = new ArrayList<>();

    // The AxisTypes
    private AxisType[]  _axisTypes;

    // The dataset start value
    private int _startValue = 0;

    // A DataSetList to hold subset of enabled datasets
    private DataSetList  _active;
    
    // The cached min X value
    private double _minX = Float.MAX_VALUE;

    // The cached max X value
    private double _maxX = -Float.MAX_VALUE;

    // The cached min Y value
    private double _minY = Float.MAX_VALUE;

    // The cached max Y value
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
            DataSet dset = addDataSetForNameAndValues(null, (Double)null);
            dset.setPointCount(getPointCount());
        }

        // If value smaller than count, remove dataset
        while (aValue<getDataSetCount())
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
        if (index>=0)
            removeDataSet(index);
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
     * Returns a list of datasets for given filter predicate.
     */
    public List<DataSet> getDataSetsFiltered(Predicate<? super DataSet> aPredicate)
    {
        return getDataSets().stream().filter(aPredicate).collect(Collectors.toList());
    }

    /**
     * The AxisTypes currently in use.
     */
    public AxisType[] getAxisTypes()
    {
        // If already set, just return
        if (_axisTypes!=null) return _axisTypes;

        // Get set of unique axes
        Set<AxisType> typesSet = new HashSet<>();
        typesSet.add(AxisType.X);
        for (DataSet dset : getDataSetList().getDataSets())
            typesSet.add(dset.getAxisTypeY());

        // Convert to array, sort, set and return
        AxisType[] types = typesSet.toArray(new AxisType[0]);
        Arrays.sort(types);
        return _axisTypes = types;
    }

    /**
     * Returns the minimum X value for datasets.
     */
    public double getMinX()
    {
        // If value already cached, just return
        if (_minX < Float.MAX_VALUE) return _minX;

        // If no datasets/points, just set to 0
        if (getPointCount()==0) return _minX = 0;

        // Iterate over datasets to get min of all
        double min = Float.MAX_VALUE;
        for (DataSet dset : getDataSets())
            min = Math.min(min, dset.getMinX());
        return _minX = min;
    }

    /**
     * Returns the maximum X value for datasets.
     */
    public double getMaxX()
    {
        // If value already cached, just return
        if (_maxX > -Float.MAX_VALUE) return _maxX;

        // If no datasets/points, just set to 5
        if (getPointCount()==0) return _maxX = 5;

        // Iterate over datasets to get max of all
        double max = -Float.MAX_VALUE;
        for (DataSet dset : getDataSets())
            max = Math.max(max, dset.getMaxX());
        return _maxX = max;
    }

    /**
     * Returns the minimum Y value for datasets.
     */
    public double getMinY(AxisType anAxisTypeY)
    {
        // If value already cached, just return
        //if (_minY < Float.MAX_VALUE) return _minY;

        // If no datasets/points, just set to 0
        if (getPointCount()==0) return _minY = 0;

        // Iterate over datasets to get min of all
        double min = Float.MAX_VALUE;
        for (DataSet dset : getDataSets()) {
            if (dset.getAxisTypeY() == anAxisTypeY)
                min = Math.min(min, dset.getMinY());
        }

        // Bogus
        if (min == Float.MAX_VALUE) {
            for (DataSet dset : getDataSets())
                min = Math.min(min, dset.getMinY());
        }

        // Return
        return _minY = min;
    }

    /**
     * Returns the maximum Y value for datasets.
     */
    public double getMaxY(AxisType anAxisTypeY)
    {
        // If value already cached, just return
        //if (_maxY > -Float.MAX_VALUE) return _maxY;

        // If no datasets, just set to 0
        if (getPointCount()==0) return _maxY = 5;

        // Iterate over datasets to get max of all
        double max = -Float.MAX_VALUE;
        for (DataSet dset : getDataSets()) {
            if (dset.getAxisTypeY() == anAxisTypeY)
                max = Math.max(max, dset.getMaxY());
        }

        // Bogus
        if (max == -Float.MAX_VALUE) {
            for (DataSet dset : getDataSets())
                max = Math.max(max, dset.getMaxY());
        }

        // Return
        return _maxY = max;
    }

    /**
     * Returns the min value for given axis.
     */
    public double getMinForAxis(AxisType anAxisType)
    {
        // Handle X axis
        if (anAxisType == AxisType.X)
            return getMinX();

        // Handle any Y axis
        if (anAxisType.isAnyY())
            return getMinY(anAxisType);

        // Complain
        throw new RuntimeException("DataSetList.getMinForAxis: Unknown axis: " + anAxisType);
    }

    /**
     * Returns the max value for given axis.
     */
    public double getMaxForAxis(AxisType anAxisType)
    {
        // Handle X axis
        if (anAxisType == AxisType.X)
            return getMaxX();

        // Handle any Y axis
        if (anAxisType.isAnyY())
            return getMaxY(anAxisType);

        // Complain
        throw new RuntimeException("DataSetList.getMaxForAxis: Unknown axis: " + anAxisType);
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
     * Returns a DataSetList of active datasets.
     */
    public DataSetList getActiveList()
    {
        // If already cached, just return it
        if (_active!=null) return _active;

        // If all datasets are enabled, return this dataset
        int activeCount = 0; for (DataSet dset : _dsets) if (dset.isEnabled()) activeCount++;
        if (activeCount==getDataSetCount())
            return _active = this;

        // Create new DataSetList and initialize with enabled sets
        DataSetList active = new DataSetList(_chart);
        active._dsets = new ArrayList(activeCount);
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
        _minX = _minY = Float.MAX_VALUE;
        _maxX = _maxY = -Float.MAX_VALUE;
        _axisTypes = null;
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