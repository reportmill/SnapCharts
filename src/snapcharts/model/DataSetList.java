/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import java.util.*;
import java.util.stream.Stream;

import snap.geom.Insets;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.util.*;
import snapcharts.util.MinMax;

/**
 * A class to manage a list of DataSets.
 */
public class DataSetList extends ChartPart {

    // The list of datasets
    private List<DataSet> _dataSetsList = new ArrayList<>();

    // The dataset start value
    private int _startValue = 0;

    // The DataSets array
    private DataSet[]  _dataSets;

    // The Enabled DataSets array
    private DataSet[]  _enabledDataSets;

    // The AxisTypes
    private AxisType[] _axisTypes;

    // A map of MinMax values for axis types
    private Map<AxisType,MinMax>  _minMaxs = new HashMap<>();

    // Constants for properties
    public static final String StartValue_Prop = "StartValue";
    public static final String DataSet_Prop = "DataSet";

    // Constants for property defaults
    public static final Border DEFAULT_BORDER = Border.createLineBorder(Color.GRAY, 1).copyForInsets(Insets.EMPTY);

    /**
     * Creates a DataSet for given ChartView.
     */
    public DataSetList(Chart aChart)
    {
        _chart = aChart;
    }

    /**
     * Override to return this DataSetList.
     */
    @Override
    public DataSetList getDataSetList()  { return this; }

    /**
     * Returns the array of datasets.
     */
    public DataSet[] getDataSets()
    {
        if (_dataSets != null) return _dataSets;

        return _dataSets = _dataSetsList.toArray(new DataSet[0]);
    }

    /**
     * Returns whether no datasets.
     */
    public boolean isEmpty()
    {
        return _dataSetsList.isEmpty();
    }

    /**
     * Returns the number of datasets.
     */
    public int getDataSetCount()
    {
        return _dataSetsList.size();
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
        return _dataSetsList.get(anIndex);
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
        _dataSetsList.add(anIndex, aDataSet);
        aDataSet._parent = this;
        aDataSet.addPropChangeListener(pc -> dataSetDidPropChange(pc));

        // Reset indexes
        for (int i = 0; i < _dataSetsList.size(); i++)
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
        DataSet dset = _dataSetsList.remove(anIndex);
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
        int index = _dataSetsList.indexOf(aDataSet);
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
     * Returns the enabled datasets.
     */
    public DataSet[] getEnabledDataSets()
    {
        if (_enabledDataSets != null) return _enabledDataSets;

        DataSet[] dataSets = getDataSets();
        DataSet[] dataSets2 = Stream.of(dataSets).filter(i -> i.isEnabled()).toArray(size -> new DataSet[size]);
        return _enabledDataSets = dataSets2;
    }

    /**
     * Returns the number of datasets that are enabled.
     */
    public int getEnabledDataSetCount()
    {
        DataSet[] enabledDataSets = getEnabledDataSets();
        return enabledDataSets.length;
    }

    /**
     * Returns the enabled datasets at given enabled dataset index.
     */
    public DataSet getEnabledDataSet(int anIndex)
    {
        DataSet[] enabledDataSets = getEnabledDataSets();
        return enabledDataSets[anIndex];
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
        DataSet[] dataSets = getEnabledDataSets();
        for (DataSet dset : dataSets)
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
        DataSet[] dataSets = getEnabledDataSets();
        if (dataSets.length == 0 || getPointCount() == 0)
            return new MinMax(0, 5);

        // Handle X
        if (anAxisType == AxisType.X) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (DataSet dset : dataSets) {
                DataStore procData = dset.getProcessedData();
                min = Math.min(min, procData.getMinX());
                max = Math.max(max, procData.getMaxX());
            }
            return new MinMax(min, max);
        }

        // Handle Y
        if (anAxisType.isAnyY()) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (DataSet dset : dataSets) {
                if (anAxisType == dset.getAxisTypeY()) {
                    DataStore procData = dset.getProcessedData();
                    min = Math.min(min, procData.getMinY());
                    max = Math.max(max, procData.getMaxY());
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
        DataSet[] dataSets = getEnabledDataSets(); if (dataSets.length == 0) return 0;
        int pc = Integer.MAX_VALUE;
        for (DataSet dset : dataSets) pc = Math.min(dset.getPointCount(), pc);
        return pc;
    }

    /**
     * Sets the point count.
     */
    public void setPointCount(int aValue)
    {
        DataSet[] dataSets = getDataSets();
        for (DataSet dset : dataSets)
            dset.setPointCount(aValue);
    }

    /**
     * Returns whether slice at point index is empty.
     */
    public boolean isSliceEmpty(int anIndex)
    {
        DataSet[] dataSets = getEnabledDataSets();
        for (DataSet dset : dataSets)
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
        _dataSets = null;
        _enabledDataSets = null;
        _axisTypes = null;
        _minMaxs.clear();
    }

    /**
     * Override to customize default Border.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        if (aPropName == Border_Prop)
            return DEFAULT_BORDER;
        return super.getPropDefault(aPropName);
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
        DataSet[] dataSets = getDataSets();
        for (DataSet dset : dataSets)
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