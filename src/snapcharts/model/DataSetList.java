package snapcharts.model;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import snap.util.*;
import snapcharts.util.MinMax;

/**
 * A class to manage a list of DataSets.
 */
public class DataSetList extends ChartPart {

    // The list of datasets
    private List<DataSet> _dsets = new ArrayList<>();

    // The dataset start value
    private int _startValue = 0;

    // The AxisTypes
    private AxisType[] _axisTypes;

    // A map of MinMax values for axis types
    private Map<AxisType,MinMax>  _minMaxs = new HashMap<>();

    // Constants for properties
    public static final String StartValue_Prop = "StartValue";
    public static final String DataSet_Prop = "DataSet";

    /**
     * Creates a DataSet for given ChartView.
     */
    public DataSetList(Chart aChart)
    {
        _chart = aChart;
    }

    /**
     * Returns the list of datasets.
     */
    public List<DataSet> getDataSets()
    {
        return _dsets;
    }

    /**
     * Returns whether no datasets.
     */
    public boolean isEmpty()
    {
        return _dsets.isEmpty();
    }

    /**
     * Returns the number of datasets.
     */
    public int getDataSetCount()
    {
        return _dsets.size();
    }

    /**
     * Sets the number of datasets.
     */
    public void setDataSetCount(int aValue)
    {
        // Ignore silly values
        if (aValue < 1 || aValue > 20) return;

        // If value larger than cound, create empty dataset
        while (aValue > getDataSetCount()) {
            DataSet dset = addDataSetForNameAndValues(null, (Double) null);
            dset.setPointCount(getPointCount());
        }

        // If value smaller than count, remove dataset
        while (aValue < getDataSetCount())
            removeDataSet(getDataSetCount() - 1);
    }

    /**
     * Returns the individual dataset at given index.
     */
    public DataSet getDataSet(int anIndex)
    {
        return _dsets.get(anIndex);
    }

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
        for (int i = 0; i < _dsets.size(); i++)
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
        if (dset != null)
            firePropChange(DataSet_Prop, dset, null, anIndex);
        return dset;
    }

    /**
     * Removes the given dataset.
     */
    public int removeDataSet(DataSet aDataSet)
    {
        int index = _dsets.indexOf(aDataSet);
        if (index >= 0)
            removeDataSet(index);
        return index;
    }

    /**
     * Clears the datasets.
     */
    public void clear()
    {
        while (getDataSetCount()!=0)
            removeDataSet(getDataSetCount()-1);
        clearCachedValues();
    }

    /**
     * Returns the number of datasets that are enabled.
     */
    public int getDataSetCountEnabled()
    {
        int count = 0;
        for (DataSet dset : getDataSets()) if (dset.isEnabled()) count++;
        return count;
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
     * Returns the MinMax for given axis.
     */
    public MinMax getMinMaxForAxis(AxisType anAxisType)
    {
        MinMax minMax = _minMaxs.get(anAxisType);
        if (minMax!=null)
            return minMax;

        minMax = getMinMaxForAxisImpl(anAxisType);
        _minMaxs.put(anAxisType, minMax);
        return minMax;
    }

    /**
     * Returns the MinMax for given axis.
     */
    private MinMax getMinMaxForAxisImpl(AxisType anAxisType)
    {
        // If empty, just return silly range
        if (getDataSetCount()==0 || getPointCount()==0)
            return new MinMax(0, 5);

        // Handle X
        if (anAxisType == AxisType.X) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (DataSet dset : getDataSets()) {
                min = Math.min(min, dset.getMinX());
                max = Math.max(max, dset.getMaxX());
            }
            return new MinMax(min, max);
        }

        // Handle Y
        if (anAxisType.isAnyY()) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (DataSet dset : getDataSets()) {
                if (anAxisType == dset.getAxisTypeY()) {
                    min = Math.min(min, dset.getMinY());
                    max = Math.max(max, dset.getMaxY());
                }
            }
            if (min==Double.MAX_VALUE)
                return new MinMax(0, 5);
            return new MinMax(min, max);
        }

        // Complain
        throw new RuntimeException("DataSetList.getMinForAxis: Unknown axis: " + anAxisType);
    }

    /**
     * Returns the min value for given axis.
     */
    public double getMinForAxis(AxisType anAxisType)
    {
        return getMinMaxForAxis(anAxisType).getMin();
    }

    /**
     * Returns the max value for given axis.
     */
    public double getMaxForAxis(AxisType anAxisType)
    {
        return getMinMaxForAxis(anAxisType).getMax();
    }

    /**
     * Adds a new dataset for given name and values.
     */
    public DataSet addDataSetForNameAndValues(String aName, Double ... theVals)
    {
        // Create new DataSet, set Name and add Y values
        DataSet dset = new DataSet();
        dset.setName(aName);
        dset.setDataType(DataType.IY);
        for (Double val : theVals)
            dset.addPointXYZC(null, val, null, null);

        // Add DataSet and return
        addDataSet(dset);
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
        _axisTypes = null;
        _minMaxs.clear();
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