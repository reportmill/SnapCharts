package snapcharts.model;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.util.DataUtils;
import snapcharts.util.MinMax;
import snapcharts.util.DataStoreUtils;
import java.util.*;

/**
 * This class is a high-level representation of a chart data set.
 *
 * It contains the raw data (DataStore) and expressions, filters and more to provide processed data (also a DataStore).
 */
public class DataSet extends ChartPart {
    
    // The DataSetList that owns this dataset
    protected DataSetList  _dsetList;

    // The index in data set
    protected int  _index;

    // The Y Axis type
    private AxisType  _axisTypeY = AxisType.Y;

    // The expression to apply to X values
    private String  _exprX;

    // The expression to apply to Y values
    private String  _exprY;

    // The expression to apply to Z values
    private String  _exprZ;

    // Whether data is stacked
    private boolean  _stacked;

    // Whether dataset is disabled
    private boolean  _disabled;

    // The DataStyleHpr
    private DataStyleHpr  _dataStyleHpr;

    // The RawData
    private DataStore  _rawData = DataStore.newDataStore();

    // The Processed Data
    private DataStore  _procData;

    // Processed Data in log form
    private DataStore[]  _logData;

    // Processed data in polar form
    private DataStore  _polarData;

    // Processed data in polar XY form
    private DataStore  _polarXYData;

    // Constants for properties
    public static final String DataType_Prop = "DataType";
    public static final String Disabled_Prop = "Disabled";
    public static final String Point_Prop = "Points";
    public static final String AxisTypeY_Prop = "AxisTypeY";
    public static final String ExprX_Prop = "ExpressionX";
    public static final String ExprY_Prop = "ExpressionY";
    public static final String ExprZ_Prop = "ExpressionZ";
    public static final String Stacked_Prop = "Stacked";

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
    public Chart getChart()
    {
        if (_chart != null)
            return _chart;
        return _dsetList !=null ? _dsetList.getChart() : null;
    }

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
     * Returns whether this dataset is stacked.
     */
    public boolean isStacked()  { return _stacked; }

    /**
     * Sets whether this dataset is stacked.
     */
    public void setStacked(boolean aValue)
    {
        if (aValue==isStacked()) return;
        firePropChange(Stacked_Prop, _stacked, _stacked = aValue);
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
     * Returns the DataStyle for this DataSet (and ChartType).
     */
    public DataStyle getDataStyle()
    {
        if (_dataStyleHpr == null)
            _dataStyleHpr = new DataStyleHpr(this);
        return _dataStyleHpr.getDataStyle();
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
        DataStore procData = getProcessedData();
        return procData.getX(anIndex);
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        DataStore procData = getProcessedData();
        return procData.getY(anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        DataStore procData = getProcessedData();
        return procData.getZ(anIndex);
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        DataStore procData = getProcessedData();
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
        DataStore procData = getProcessedData();
        return procData.getValueX(anIndex);
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
        DataStore procData = getProcessedData();
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
        DataStore procData = getProcessedData();
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
        // Get point
        DataPoint dataPoint = getPoint(anIndex);
        dataPoint.cacheValues();

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

        // Get point
        DataPoint dataPoint2 = getPoint(anIndex);
        dataPoint2.cacheValues();
        firePropChange(Point_Prop, dataPoint, dataPoint2, anIndex);
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
    public DataStore getRawData()  { return _rawData; }

    /**
     * Sets the raw data.
     */
    public void setRawData(DataStore aDataStore)
    {
        _rawData = aDataStore;
        clearCachedData();
    }

    /**
     * Returns the Processed Data.
     */
    public DataStore getProcessedData()
    {
        // If already set, just return
        if (_procData != null) return _procData;

        // Get expressions
        String exprX = getExprX();
        String exprY = getExprY();
        String exprZ = getExprZ();
        DataStore procData = DataStoreUtils.getProcessedData(_rawData, exprX, exprY, exprZ);
        return _procData = procData;
    }

    /**
     * Returns the ProcessedData converted to log.
     */
    public DataStore getLogData(boolean doLogX, boolean doLogY)
    {
        // If already set, just return
        int index = (doLogX && doLogY) ? 2 : doLogX ? 0 : 1;
        if (_logData != null && _logData[index] != null) return _logData[index];

        // Make sure LogData array is present
        if (_logData == null)
            _logData = new DataStore[3];

        // If already DataType.isPolar, set/return
        DataStore procData = getProcessedData();
        DataStore logData = DataStoreUtils.getLogData(procData, doLogX, doLogY);
        return _logData[index] = logData;
    }

    /**
     * Returns the ProcessedData in polar form (just normal data if already DataType.isPolar).
     */
    public DataStore getPolarData()
    {
        // If already set, just return
        if (_polarData != null) return _polarData;

        // If already DataType.isPolar, set/return
        DataStore procData = getProcessedData();
        DataType dataType = getDataType();
        if (dataType.isPolar())
            return _polarData = procData;

        // Get Polar DataType that makes the most sense
        DataType polarDataType = DataType.TR;
        if (dataType.hasZ())
            polarDataType = dataType == DataType.XYZZ ? DataType.TRZZ : DataType.TRZ;

        // Convert, set, return
        DataStore polarData = DataStoreUtils.getPolarDataForType(procData, polarDataType);
        return _polarData = polarData;
    }

    /**
     * Returns the PolarData converted to XY format.
     */
    public DataStore getPolarXYData()
    {
        // If already set, just return
        if (_polarXYData != null) return _polarXYData;

        // Get PolarData, convert to polarXY, set/return
        DataStore polarData = getPolarData();
        DataStore xyData = DataStoreUtils.getPolarXYDataForPolar(polarData);
        return _polarXYData = xyData;
    }

    /**
     * Returns the minimum Z value in this dataset.
     */
    public double getMinZ()
    {
        DataStore procData = getProcessedData();
        return procData.getMinZ();
    }

    /**
     * Returns the maximum Z value in this dataset.
     */
    public double getMaxZ()
    {
        DataStore procData = getProcessedData();
        return procData.getMaxZ();
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
        _logData = null;
        _polarData = null;
        _polarXYData = null;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "DataSet { " + "Name=" + getName() + ", DataType=" + getDataType() + ", PointCount=" + getPointCount();
        DataStore dataStore = getRawData();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = dataStore.getMinMax(chan);
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

        // Archive AxisTypeY
        if (getAxisTypeY() != AxisType.Y)
            e.add(AxisTypeY_Prop, getAxisTypeY());

        // Archive ExprX, ExprY, ExprZ
        if (getExprX() != null && getExprX().length() > 0)
            e.add(ExprX_Prop, getExprX());
        if (getExprY() != null && getExprY().length() > 0)
            e.add(ExprY_Prop, getExprY());
        if (getExprZ() != null && getExprZ().length() > 0)
            e.add(ExprZ_Prop, getExprZ());

        // Archive Stacked
        if (isStacked())
            e.add(Stacked_Prop, true);

        // Archive Disabled
        if (isDisabled())
            e.add(Disabled_Prop, true);

        // Archive DataStyle
        DataStyle dataStyle = getDataStyle();
        XMLElement dataStyleXML = dataStyle.toXML(anArchiver);
        if (dataStyleXML.getAttributeCount() > 0)
            e.addElement(dataStyleXML);

        // Archive RawData
        DataStore rawData = getRawData();
        rawData.toXML(anArchiver, e);

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

        // Unarchive AxisTypeY
        String axisTypeStr = anElement.getAttributeValue(AxisTypeY_Prop);
        if (axisTypeStr != null)
            setAxisTypeY(AxisType.valueOf(axisTypeStr));

        // Unarchive ExprX, ExprY, ExprZ
        if (anElement.hasAttribute(ExprX_Prop))
            setExprX(anElement.getAttributeValue(ExprX_Prop));
        if (anElement.hasAttribute(ExprY_Prop))
            setExprY(anElement.getAttributeValue(ExprY_Prop));
        if (anElement.hasAttribute(ExprZ_Prop))
            setExprZ(anElement.getAttributeValue(ExprZ_Prop));

        // Unarchive Stacked
        if (anElement.hasAttribute(Stacked_Prop))
            setStacked(anElement.getAttributeBoolValue(Stacked_Prop, false));

        // Unarchive Disabled
        if (anElement.hasAttribute(Disabled_Prop))
            setDisabled(anElement.getAttributeBoolValue(Disabled_Prop, false));

        // Unarchive DataStyle
        XMLElement dataStyleXML = anElement.getElement("DataStyle");
        if (dataStyleXML != null)
            getDataStyle().fromXML(anArchiver, dataStyleXML);

        // Unarchive RawData
        DataStore rawData = getRawData();
        rawData.fromXML(anArchiver, anElement);

        // Legacy
        if (anElement.hasAttribute(DataStyle.ShowSymbols_Prop)) {
            boolean showSymbols = anElement.getAttributeBoolValue(DataStyle.ShowSymbols_Prop);
            getDataStyle().setShowSymbols(showSymbols);
        }

        // Return this part
        return this;
    }
}