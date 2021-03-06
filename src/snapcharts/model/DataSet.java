package snapcharts.model;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.util.DataUtils;
import snapcharts.util.MinMax;
import snapcharts.util.RawDataUtils;

import java.util.*;

/**
 * A class to represent a list of data points.
 */
public class DataSet extends ChartPart {
    
    // The DataSetList that owns this dataset
    protected DataSetList  _dsetList;

    // The index in data set
    protected int  _index;

    // The Y Axis type
    private AxisType  _axisTypeY = AxisType.Y;

    // Whether dataset is disabled
    private boolean  _disabled;

    // Whether to show symbols
    private boolean  _showSymbols;

    // The expression to apply to X values
    private String  _exprX;

    // The expression to apply to Y values
    private String  _exprY;

    // The expression to apply to Z values
    private String  _exprZ;

    // The RawData
    private RawData  _rawData = RawData.newRawData();

    // The Processed Data
    private RawData  _procData;

    // RawData in polar form
    private RawData  _polarRawData;

    // RawData in polar XY form
    private RawData  _polarXYRawData;

    // Constants for properties
    public static final String DataType_Prop = "DataType";
    public static final String Disabled_Prop = "Disabled";
    public static final String Point_Prop = "Points";
    public static final String ShowSymbols_Prop = "ShowSymbols";
    public static final String AxisTypeY_Prop = "AxisTypeY";
    public static final String ExprX_Prop = "ExpressionX";
    public static final String ExprY_Prop = "ExpressionY";
    public static final String ExprZ_Prop = "ExpressionZ";

    /**
     * Constructor.
     */
    public DataSet()
    {
        super();
    }

    /**
     * Returns the chart.
     */
    @Override
    public Chart getChart()  { return _dsetList !=null ? _dsetList.getChart() : null; }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return _dsetList; }

    /**
     * Returns the index in dataset.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns the DataType.
     */
    public DataType getDataType()
    {
        return _rawData.getDataType();
    }

    /**
     * Sets the DataType.
     */
    public void setDataType(DataType aDataType)
    {
        if (aDataType==getDataType()) return;
        DataType old = getDataType();
        _rawData.setDataType(aDataType);
        clearCachedData();
        firePropChange(DataType_Prop, old, aDataType);
    }

    /**
     * Returns the Y axis type.
     */
    public AxisType getAxisTypeY()  { return _axisTypeY; }

    /**
     * Sets the Y axis type.
     */
    public void setAxisTypeY(AxisType anAxisType)
    {
        if (anAxisType==getAxisTypeY()) return;
        if (anAxisType==null || !anAxisType.isAnyY())
            throw new IllegalArgumentException("DataSet.setAxisTypeY: Unsupported AxisTypeY: " + anAxisType);
        firePropChange(AxisTypeY_Prop, _axisTypeY, _axisTypeY = anAxisType);
    }

    /**
     * Returns whether to show symbols for this DataSet.
     */
    public boolean isShowSymbols()  { return _showSymbols; }

    /**
     * Sets whether to show symbols for this DataSet.
     */
    public void setShowSymbols(boolean aValue)
    {
        if (aValue==isShowSymbols()) return;
        firePropChange(ShowSymbols_Prop, _showSymbols, _showSymbols = aValue);
    }

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
        firePropChange(Disabled_Prop, _disabled, _disabled = aValue);
    }

    /**
     * Returns whether this dataset is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Returns the expression to apply to X values.
     */
    public String getExprX()  { return _exprX; }

    /**
     * Sets the expressions to apply to X values.
     */
    public void setExprX(String anExpr)
    {
        if (Objects.equals(anExpr, getExprX())) return;
        firePropChange(ExprX_Prop, _exprX, _exprX = anExpr);
        clearCachedData();
    }

    /**
     * Returns the expression to apply to Y values.
     */
    public String getExprY()  { return _exprY; }

    /**
     * Sets the expressions to apply to Y values.
     */
    public void setExprY(String anExpr)
    {
        if (Objects.equals(anExpr, getExprY())) return;
        firePropChange(ExprY_Prop, _exprY, _exprY = anExpr);
        clearCachedData();
    }

    /**
     * Returns the expression to apply to Z values.
     */
    public String getExprZ()  { return _exprZ; }

    /**
     * Sets the expressions to apply to Z values.
     */
    public void setExprZ(String anExpr)
    {
        if (Objects.equals(anExpr, getExprZ())) return;
        firePropChange(ExprZ_Prop, _exprZ, _exprZ = anExpr);
        clearCachedData();
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _rawData.getRowCount(); }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _rawData.getColCount(); }

    /**
     * Returns the number of points.
     */
    public int getPointCount()
    {
        return _rawData.getPointCount();
    }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        _rawData.setPointCount(aValue);
    }

    /**
     * Returns the data point at given index.
     */
    public DataPoint getPoint(int anIndex)
    {
        return new DataPoint(this, anIndex);
    }

    /**
     * Adds a point for given components at given index.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        _rawData.addPoint(aPoint, anIndex);
        clearCachedData();
        firePropChange(Point_Prop, aPoint, null, anIndex);
    }

    /**
     * Removes a point at given index.
     */
    public DataPoint removePoint(int anIndex)
    {
        // Get point at index
        DataPoint dataPoint = getPoint(anIndex);
        dataPoint.cacheValues();

        // Remove point from RawData
        _rawData.removePoint(anIndex);
        clearCachedData();
        firePropChange(Point_Prop, dataPoint, null, anIndex);

        // Return point
        return dataPoint;
    }

    /**
     * Clears all points.
     */
    public void clearPoints()
    {
        _rawData.clearPoints();
        clearCachedData();
    }

    /**
     * Adds a point for X/Y/Z/C values.
     */
    public void addPointXYZC(Double aX, Double aY, Double aZ, String aC)
    {
        DataPoint dpnt = new DataPoint(aX, aY, aZ, aC);
        addPoint(dpnt, getPointCount());
    }

    /**
     * Returns the X value at given index.
     */
    public double getX(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getX(anIndex);
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getY(anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getZ(anIndex);
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getC(anIndex);
    }

    /**
     * Sets the C value at given index.
     */
    public void setValueC(String aValue, int anIndex)
    {
        _rawData.setC(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the X value at given index (null if not set).
     */
    public Double getValueX(int anIndex)
    {
        return _rawData.getValueX(anIndex);
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        _rawData.setValueX(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Y value at given index (null if not set).
     */
    public Double getValueY(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getValueY(anIndex);
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        _rawData.setValueY(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Z value at given index (null if not set).
     */
    public Double getValueZ(int anIndex)
    {
        RawData procData = getProcessedData();
        return procData.getValueZ(anIndex);
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        _rawData.setValueZ(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Return data point as a string (either C or X).
     */
    public String getString(int anIndex)
    {
        // If point string is set, just return it
        String str = getC(anIndex);
        if(str!=null)
            return str;

        // If categories, return that
        Chart chart = getChart();
        List <String> cats = chart.getAxisX().getCategories();
        if (cats!=null && anIndex<cats.size())
            return cats.get(anIndex);

        // If start value is set
        int startValue = getDataSetList().getStartValue();
        if (startValue!=0)
            return String.valueOf(startValue + anIndex);

        // Otherwise return x val (as int, if whole number)
        double val = getX(anIndex);
        if (val==(int)val)
            return String.valueOf((int)val);
        return DataUtils.formatValue(val);
    }

    /**
     * Returns the value for channel and record index.
     */
    public Object getValueForChannel(DataChan aChan, int anIndex)
    {
        switch (aChan) {
            case X: return getValueX(anIndex);
            case Y: return getValueY(anIndex);
            case Z: return getValueZ(anIndex);
            case I: return anIndex;
            case C: return getString(anIndex);
            case T: return getValueX(anIndex);
            case R: return getValueY(anIndex);
            default: throw new RuntimeException("DataSet.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }
    }

    /**
     * Sets the value for channel and record index.
     */
    public void setValueForChannel(Object aValue, DataChan aChan, int anIndex)
    {
        switch (aChan) {
            case X:
                Double valX = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueX(valX, anIndex);
                break;
            case Y:
                Double valY = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueY(valY, anIndex);
                break;
            case Z:
                Double valZ = aValue!=null ? SnapUtils.doubleValue(aValue) : null;
                setValueZ(valZ, anIndex);
                break;
            case I:
                System.err.println("DataSet.setValueForChannel: Shouldn't set value for index channel");
                break;
            case C:
                String valC = aValue!=null ? SnapUtils.stringValue(aValue) : null;
                setValueC(valC, anIndex);
                break;
            default: throw new RuntimeException("DataSet.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }
    }

    /**
     * Returns the value for given channel index and record index.
     */
    public Object getValueForChannelIndex(int aChanIndex, int anIndex)
    {
        DataType dataType = getDataType();
        DataChan chan = dataType.getChannel(aChanIndex);
        return getValueForChannel(chan, anIndex);
    }

    /**
     * Sets given value for given channel index and record index.
     */
    public void setValueForChannelIndex(Object aValue, int aChanIndex, int anIndex)
    {
        DataType dataType = getDataType();
        DataChan chan = dataType.getChannel(aChanIndex);
        setValueForChannel(aValue, chan, anIndex);
    }

    /**
     * Returns the raw data.
     */
    public RawData getRawData()  { return _rawData; }

    /**
     * Sets the raw data.
     */
    public void setRawData(RawData aRawData)
    {
        _rawData = aRawData;
        clearCachedData();
    }

    /**
     * Returns the Processed Data.
     */
    public RawData getProcessedData()
    {
        // If already set, just return
        if (_procData != null) return _procData;

        // Get expressions
        String exprX = getExprX();
        String exprY = getExprY();
        String exprZ = getExprZ();
        RawData procData = RawDataUtils.getProcessedData(_rawData, exprX, exprY, exprZ);
        return _procData = procData;
    }

    /**
     * Returns the raw data in polar form (just normal data if already DataType.isPolar).
     */
    public RawData getPolarRawData()
    {
        // If already set, just return
        if (_polarRawData != null) return _polarRawData;

        // If already DataType.isPolar, set/return
        RawData procData = getProcessedData();
        DataType dataType = getDataType();
        if (dataType.isPolar())
            return _polarRawData = procData;

        // Get Polar DataType that makes the most sense
        DataType polarDataType = DataType.TR;
        if (dataType.hasZ())
            polarDataType = dataType == DataType.XYZZ ? DataType.TRZZ : DataType.TRZ;

        // Convert, set, return
        RawData polarRawData = RawDataUtils.getPolarRawDataForType(procData, polarDataType);
        return _polarRawData = polarRawData;
    }

    /**
     * Returns the raw data of PolarRawData converted to XY format.
     */
    public RawData getPolarXYRawData()
    {
        // If already set, just return
        if (_polarXYRawData != null) return _polarXYRawData;

        // Get PolarData, convert to polarXY, set/return
        RawData polarData = getPolarRawData();
        RawData xyData = RawDataUtils.getPolarXYRawDataForPolar(polarData);
        return _polarXYRawData = xyData;
    }

    /**
     * Returns an array of dataset X values.
     */
    public double[] getDataX()
    {
        RawData procData = getProcessedData();
        return procData.getDataX();
    }

    /**
     * Returns an array of dataset Y values.
     */
    public double[] getDataY()
    {
        RawData procData = getProcessedData();
        return procData.getDataY();
    }

    /**
     * Returns an array of dataset Z values.
     */
    public double[] getDataZ()
    {
        RawData procData = getProcessedData();
        return procData.getDataZ();
    }

    /**
     * Returns an array of dataset C values.
     */
    public String[] getDataC()
    {
        RawData procData = getProcessedData();
        return procData.getDataC();
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public double getMinX()
    {
        RawData procData = getProcessedData();
        return procData.getMinX();
    }

    /**
     * Returns the maximum X value in this dataset.
     */
    public double getMaxX()
    {
        RawData procData = getProcessedData();
        return procData.getMaxX();
    }

    /**
     * Returns the minimum Y value in this dataset.
     */
    public double getMinY()
    {
        RawData procData = getProcessedData();
        return procData.getMinY();
    }

    /**
     * Returns the maximum Y value in this dataset.
     */
    public double getMaxY()
    {
        RawData procData = getProcessedData();
        return procData.getMaxY();
    }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()
    {
        RawData procData = getProcessedData();
        return procData.getMinZ();
    }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()
    {
        RawData procData = getProcessedData();
        return procData.getMaxZ();
    }

    /**
     * Returns the dataX values for ZZ.
     */
    public double[] getDataXforZZ()
    {
        RawData procData = getProcessedData();
        return procData.getDataXforZZ();
    }

    /**
     * Returns the dataY values for ZZ.
     */
    private double[] getDataYforZZ()
    {
        RawData procData = getProcessedData();
        return procData.getDataYforZZ();
    }

    /**
     * Returns whether this dataset is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0)
            return false;
        return _rawData.isClear();
    }

    /**
     * Called when a points are added, removed or modified.
     */
    protected void clearCachedData()
    {
        _procData = null;
        _polarRawData = null;
        _polarXYRawData = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "DataSet { " + "DataType=" + getDataType() + ", PointCount=" + getPointCount();
        RawData rawData = getRawData();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = rawData.getMinMax(chan);
            str += ", Min" + chan + "=" + minMax.getMin() + ", Max" + chan + "=" + minMax.getMax();
        }
        return str + '}';
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive DataType
        DataType dataType = getDataType();
        e.add(DataType_Prop, dataType);

        // Archive ShowSymbols, Disabled
        if (isShowSymbols())
            e.add(ShowSymbols_Prop, true);
        if (isDisabled())
            e.add(Disabled_Prop, true);

        // Archive AxisTypeY
        if (getAxisTypeY() != AxisType.Y)
            e.add(AxisTypeY_Prop, getAxisTypeY());

        // If DataType has X, add DataX values
        if (dataType.hasChannel(DataChan.X)) {
            double[] dataX = dataType!=DataType.XYZZ ? getDataX() : getDataXforZZ();
            String dataStr = DataUtils.getStringForDoubleArray(dataX);
            e.add(new XMLElement("DataX", dataStr));
        }

        // If DataType has Y, add DataY values
        if (dataType.hasChannel(DataChan.Y)) {
            double[] dataY = dataType!=DataType.XYZZ ? getDataY() : getDataYforZZ();
            String dataStr = DataUtils.getStringForDoubleArray(dataY);
            e.add(new XMLElement("DataY", dataStr));
        }

        // If DataType has Z, add DataZ values
        if (dataType.hasChannel(DataChan.Z)) {
            String dataStr = DataUtils.getStringForDoubleArray(getDataZ());
            e.add(new XMLElement("DataZ", dataStr));
        }

        // If DataType has C, add DataC values
        if (dataType.hasChannel(DataChan.C)) {
            String dataStr = Arrays.toString(getDataC());
            e.add(new XMLElement("DataC", dataStr));
        }

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

        // Unarchive DataType
        String dataTypeStr = anElement.getAttributeValue(DataType_Prop);
        DataType dataType = DataType.valueOf(dataTypeStr);
        setDataType(dataType);

        // Unarchive ShowSymbols, Disabled
        setShowSymbols(anElement.getAttributeBoolValue(ShowSymbols_Prop, false));
        setDisabled(anElement.getAttributeBoolValue(Disabled_Prop, false));

        // Archive AxisTypeY
        String axisTypeStr = anElement.getAttributeValue(AxisTypeY_Prop);
        if (axisTypeStr != null)
            setAxisTypeY(AxisType.valueOf(axisTypeStr));

        // Get DataX
        double dataX[] = null;
        XMLElement dataX_XML = anElement.get("DataX");
        if (dataX_XML!=null) {
            String dataXStr = dataX_XML.getValue();
            dataX = DataUtils.getDoubleArrayForString(dataXStr);
        }

        // Get DataY
        double dataY[] = null;
        XMLElement dataY_XML = anElement.get("DataY");
        if (dataY_XML!=null) {
            String dataYStr = dataY_XML.getValue();
            dataY = DataUtils.getDoubleArrayForString(dataYStr);
        }

        // Get DataZ
        double dataZ[] = null;
        XMLElement dataZ_XML = anElement.get("DataZ");
        if (dataZ_XML!=null) {
            String dataZStr = dataZ_XML.getValue();
            dataZ = DataUtils.getDoubleArrayForString(dataZStr);
        }

        // Get DataC
        String dataC[] = null;
        XMLElement dataC_XML = anElement.get("DataC");
        if (dataC_XML!=null) {
            String dataCStr = dataC_XML.getValue();
            dataC = DataUtils.getStringArrayForString(dataCStr);
        }

        // Add Data points
        if (dataType == DataType.XYZZ)
            DataUtils.addDataSetPointsXYZZ(this, dataX, dataY, dataZ);
        else DataUtils.addDataSetPoints(this, dataX, dataY, dataZ, dataC);

        // Return this part
        return this;
    }
}