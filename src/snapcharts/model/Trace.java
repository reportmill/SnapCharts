/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.*;
import snapcharts.data.*;
import snapcharts.util.MinMax;

import java.util.*;

/**
 * This class represents a 'rendered' or 'painted' dataset. It contains the original data (DataSet) and paint properties
 * as well as expressions, filters and more to provide processed data.
 */
public class Trace extends ChartPart {
    
    // The index in parent TraceList
    protected int  _index;

    // Whether to show line
    private boolean  _showLine = true;

    // Whether to show area
    private boolean  _showArea;

    // Whether to show data points
    private boolean  _showPoints;

    // Whether to show data tags
    private boolean  _showTags;

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

    // Whether trace is disabled
    private boolean  _disabled;

    // Whether to show legend entry
    private boolean  _showLegendEntry = true;

    // The TraceStyleHpr
    private TraceStyleHpr _traceStyleHpr;

    // The PointStyle
    private PointStyle  _pointStyle = new PointStyle(this);

    // The TagStyle
    private TagStyle  _tagStyle = new TagStyle(this);

    // The original DataSet
    private DataSet  _dataSet = DataSet.newDataSet();

    // The DataSet processed with expressions
    private DataSet  _procData;

    // Processed Data in log form
    private DataSet[]  _logData;

    // Processed data in polar form
    private DataSet _polarData;

    // Processed data in polar XY form
    private DataSet _polarXYData;

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String ShowArea_Prop = "ShowArea";
    public static final String ShowPoints_Prop = "ShowPoints";
    public static final String ShowTags_Prop = "ShowTags";
    public static final String DataType_Prop = DataSet.DataType_Prop;
    public static final String ThetaUhit_Prop = "ThetaUnit";
    public static final String AxisTypeY_Prop = "AxisTypeY";
    public static final String ExprX_Prop = "ExpressionX";
    public static final String ExprY_Prop = "ExpressionY";
    public static final String ExprZ_Prop = "ExpressionZ";
    public static final String Stacked_Prop = "Stacked";
    public static final String ShowLegendEntry_Prop = "ShowLegendEntry";
    public static final String Disabled_Prop = "Disabled";
    public static final String Point_Prop = "Points";

    // Constants for relations
    public static final String PointStyle_Rel = "PointStyle";
    public static final String TagStyle_Rel = "TagStyle";

    /**
     * Constructor.
     */
    public Trace()
    {
        super();

        _traceStyleHpr = new TraceStyleHpr(this);

        // Register listener for TagStyle, PointStyle prop changes
        _tagStyle.addPropChangeListener(pc -> childChartPartDidPropChange(pc));
        _pointStyle.addPropChangeListener(pc -> childChartPartDidPropChange(pc));
    }

    /**
     * Returns the index in TraceList.
     */
    public int getIndex()  { return _index; }

    /**
     * Returns whether to show line for Trace.
     */
    public boolean isShowLine()
    {
        return _showLine;
    }

    /**
     * Sets whether to show line for Trace.
     */
    public void setShowLine(boolean aValue)
    {
        if (aValue == isShowLine()) return;
        firePropChange(ShowLine_Prop, _showLine, _showLine = aValue);
    }

    /**
     * Returns whether to show area for Trace.
     */
    public boolean isShowArea()
    {
        return _showArea;
    }

    /**
     * Sets whether to show area for Trace.
     */
    public void setShowArea(boolean aValue)
    {
        if (aValue == isShowArea()) return;
        firePropChange(ShowArea_Prop, _showArea, _showArea = aValue);
    }

    /**
     * Returns whether to show points/symbols for Trace.
     */
    public boolean isShowPoints()
    {
        return _showPoints;
    }

    /**
     * Sets whether to show points/symbols for Trace.
     */
    public void setShowPoints(boolean aValue)
    {
        if (aValue == isShowPoints()) return;
        firePropChange(ShowPoints_Prop, _showPoints, _showPoints = aValue);
    }

    /**
     * Returns whether to show data tags for Trace.
     */
    public boolean isShowTags()
    {
        return _showTags;
    }

    /**
     * Sets whether to show data tags for Trace.
     */
    public void setShowTags(boolean aValue)
    {
        if (aValue == isShowTags()) return;
        firePropChange(ShowTags_Prop, _showTags, _showTags = aValue);
    }

    /**
     * Returns the PointStyle for this Trace.
     */
    public PointStyle getPointStyle()  { return _pointStyle; }

    /**
     * Returns the TagStyle for this Trace.
     */
    public TagStyle getTagStyle()  { return _tagStyle; }

    /**
     * Returns the DataType.
     */
    public DataType getDataType()
    {
        return _dataSet.getDataType();
    }

    /**
     * Sets the DataType.
     */
    public void setDataType(DataType aDataType)
    {
        // If already set, just return
        if (aDataType == getDataType()) return;

        // Forward to DataSet
        DataType old = getDataType();
        _dataSet.setDataType(aDataType);

        // Clear cached data and firePropChange
        clearCachedData();
        firePropChange(DataType_Prop, old, aDataType);
    }

    /**
     * Returns the units for Theta data.
     */
    public DataSet.ThetaUnit getThetaUnit()  { return _dataSet.getThetaUnit(); }

    /**
     * Sets the units for Theta data.
     */
    public void setThetaUnit(DataSet.ThetaUnit aValue)
    {
        // If already set, just return
        if (aValue == getThetaUnit()) return;

        // Forward to DataSet
        DataSet.ThetaUnit old = getThetaUnit();
        _dataSet.setThetaUnit(aValue);

        // Clear cached data and firePropChange
        clearCachedData();
        firePropChange(ThetaUhit_Prop, old, aValue);
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
        // If already set, just return
        if (anAxisType == getAxisTypeY()) return;

        // If not Y AxisType, complain
        if (anAxisType==null || !anAxisType.isAnyY())
            throw new IllegalArgumentException("Trace.setAxisTypeY: Unsupported AxisTypeY: " + anAxisType);

        // Set and firePropChange
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
     * Returns whether this trace is stacked.
     */
    public boolean isStacked()  { return _stacked; }

    /**
     * Sets whether this trace is stacked.
     */
    public void setStacked(boolean aValue)
    {
        if (aValue==isStacked()) return;
        firePropChange(Stacked_Prop, _stacked, _stacked = aValue);
    }

    /**
     * Returns whether this trace is disabled.
     */
    public boolean isDisabled()  { return _disabled; }

    /**
     * Sets whether this trace is disabled.
     */
    public void setDisabled(boolean aValue)
    {
        if (aValue==isDisabled()) return;
        firePropChange(Disabled_Prop, _disabled, _disabled = aValue);
    }

    /**
     * Returns whether this trace is enabled.
     */
    public boolean isEnabled()  { return !_disabled; }

    /**
     * Returns whether to show legend entry for this trace.
     */
    public boolean isShowLegendEntry()  { return _showLegendEntry; }

    /**
     * Sets whether to show legend entry for this trace.
     */
    public void setShowLegendEntry(boolean aValue)
    {
        if (aValue==isShowLegendEntry()) return;
        firePropChange(ShowLegendEntry_Prop, _showLegendEntry, _showLegendEntry = aValue);
    }

    /**
     * Returns the TraceStyle for this trace (and ChartType).
     */
    public TraceStyle getTraceStyle()
    {
        ChartType chartType = getTraceChartType();
        return _traceStyleHpr.getTraceStyleForChartType(chartType);
    }

    /**
     * Returns the Trace ChartType. This should be the same as Chart.ChartType, but can be overridden.
     */
    public ChartType getTraceChartType()
    {
        // Get Chart.ChartType
        ChartType chartType = getChartType();

        // If Contour but no Z data, use Scatter instead
        if (chartType.isContourType() && !getDataType().hasZ())
            chartType = chartType.isPolarType() ? ChartType.POLAR : ChartType.SCATTER;

        // Return ChartType
        return chartType;
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount()  { return _dataSet.getRowCount(); }

    /**
     * Returns the number of columns.
     */
    public int getColCount()  { return _dataSet.getColCount(); }

    /**
     * Returns the number of points.
     */
    public int getPointCount()
    {
        return _dataSet.getPointCount();
    }

    /**
     * Sets the number of points.
     */
    public void setPointCount(int aValue)
    {
        _dataSet.setPointCount(aValue);
    }

    /**
     * Returns the data point at given index.
     */
    public TracePoint getPoint(int anIndex)
    {
        return new TracePoint(this, anIndex);
    }

    /**
     * Adds a point for given components at given index.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        _dataSet.addPoint(aPoint, anIndex);
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

        // Remove point from DataSet
        _dataSet.removePoint(anIndex);
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
        _dataSet.clearPoints();
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
        DataSet procData = getProcessedData();
        return procData.getX(anIndex);
    }

    /**
     * Returns the Y value at given index.
     */
    public double getY(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getY(anIndex);
    }

    /**
     * Returns the Z value at given index.
     */
    public double getZ(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getZ(anIndex);
    }

    /**
     * Returns the C value at given index.
     */
    public String getC(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getC(anIndex);
    }

    /**
     * Sets the C value at given index.
     */
    public void setValueC(String aValue, int anIndex)
    {
        _dataSet.setC(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the X value at given index (null if not set).
     */
    public Double getValueX(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueX(anIndex);
    }

    /**
     * Sets the X value at given index.
     */
    public void setValueX(Double aValue, int anIndex)
    {
        _dataSet.setValueX(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Y value at given index (null if not set).
     */
    public Double getValueY(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueY(anIndex);
    }

    /**
     * Sets the Y value at given index.
     */
    public void setValueY(Double aValue, int anIndex)
    {
        _dataSet.setValueY(aValue, anIndex);
        clearCachedData();
    }

    /**
     * Returns the Z value at given index (null if not set).
     */
    public Double getValueZ(int anIndex)
    {
        DataSet procData = getProcessedData();
        return procData.getValueZ(anIndex);
    }

    /**
     * Sets the Z value at given index.
     */
    public void setValueZ(Double aValue, int anIndex)
    {
        _dataSet.setValueZ(aValue, anIndex);
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
        int startValue = getTraceList().getStartValue();
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
            default: throw new RuntimeException("Trace.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }
    }

    /**
     * Sets the value for channel and record index.
     */
    public void setValueForChannel(Object aValue, DataChan aChan, int anIndex)
    {
        // Get point
        DataPoint dataPoint = getPoint(anIndex);

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
                System.err.println("Trace.setValueForChannel: Shouldn't set value for index channel");
                break;
            case C:
                String valC = aValue!=null ? SnapUtils.stringValue(aValue) : null;
                setValueC(valC, anIndex);
                break;
            default: throw new RuntimeException("Trace.getValueForChannelAndIndex: Unknown channel: " + aChan);
        }

        // Get point
        DataPoint dataPoint2 = getPoint(anIndex);
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
     * Returns the original DataSet.
     */
    public DataSet getDataSet()  { return _dataSet; }

    /**
     * Sets the original DataSet.
     */
    public void setDataSet(DataSet aDataSet)
    {
        _dataSet = aDataSet;
        clearCachedData();
    }

    /**
     * Returns the Processed Data.
     */
    public DataSet getProcessedData()
    {
        // If already set, just return
        if (_procData != null) return _procData;

        // Get expressions
        String exprX = getExprX();
        String exprY = getExprY();
        String exprZ = getExprZ();
        DataSet procData = DataSetUtils.getProcessedData(_dataSet, exprX, exprY, exprZ);
        return _procData = procData;
    }

    /**
     * Returns the ProcessedData converted to log.
     */
    public DataSet getLogData(boolean doLogX, boolean doLogY)
    {
        // If already set, just return
        int index = (doLogX && doLogY) ? 2 : doLogX ? 0 : 1;
        if (_logData != null && _logData[index] != null) return _logData[index];

        // Make sure LogData array is present
        if (_logData == null)
            _logData = new DataSet[3];

        // If already DataType.isPolar, set/return
        DataSet procData = getProcessedData();
        DataSet logData = DataSetUtils.getLogData(procData, doLogX, doLogY);
        return _logData[index] = logData;
    }

    /**
     * Returns the ProcessedData in polar form (just normal data if already DataType.isPolar).
     */
    public DataSet getPolarData()
    {
        // If already set, just return
        if (_polarData != null) return _polarData;

        // If already DataType.isPolar, set/return
        DataSet procData = getProcessedData();
        DataType dataType = getDataType();
        if (dataType.isPolar())
            return _polarData = procData;

        // Get Polar DataType that makes the most sense
        DataType polarDataType = DataType.TR;
        if (dataType.hasZ())
            polarDataType = dataType == DataType.XYZZ ? DataType.TRZZ : DataType.TRZ;

        // Convert, set, return
        DataSet polarData = DataSetUtils.getPolarDataForType(procData, polarDataType);
        return _polarData = polarData;
    }

    /**
     * Returns the PolarData converted to XY format.
     */
    public DataSet getPolarXYData()
    {
        // If already set, just return
        if (_polarXYData != null) return _polarXYData;

        // Get PolarData, convert to polarXY, set/return
        DataSet polarData = getPolarData();
        DataSet xyData = DataSetUtils.getPolarXYDataForPolar(polarData);
        return _polarXYData = xyData;
    }

    /**
     * Returns the minimum Z value in this trace.
     */
    public double getMinZ()
    {
        DataSet procData = getProcessedData();
        return procData.getMinZ();
    }

    /**
     * Returns the maximum Z value in this trace.
     */
    public double getMaxZ()
    {
        DataSet procData = getProcessedData();
        return procData.getMaxZ();
    }

    /**
     * Returns whether this trace is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName()!=null && getName().length()>0)
            return false;
        return _dataSet.isClear();
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
        String str = "Trace { " + "Name=" + getName() + ", DataType=" + getDataType() + ", PointCount=" + getPointCount();
        DataSet dataSet = getDataSet();
        for (DataChan chan : getDataType().getChannels()) {
            MinMax minMax = dataSet.getMinMax(chan);
            str += ", Min" + chan + "=" + minMax.getMin() + ", Max" + chan + "=" + minMax.getMax();
        }
        return str + '}';
    }

    /**
     * Called when a child chart part has prop change.
     */
    private void childChartPartDidPropChange(PropChange aPC)
    {
        Chart chart = getChart();
        chart.chartPartDidPropChange(aPC);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        aPropDefaults.addProps(ShowLine_Prop, ShowArea_Prop, ShowPoints_Prop, ShowTags_Prop);

        aPropDefaults.addRelations(PointStyle_Rel, TagStyle_Rel);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ShowLine, ShowArea, ShowPoints, ShowTags
            case ShowLine_Prop: return isShowLine();
            case ShowArea_Prop: return isShowArea();
            case ShowPoints_Prop: return isShowPoints();
            case ShowTags_Prop: return isShowTags();

            // Handle PointStyleRel, TagStyle_Rel
            case PointStyle_Rel: return getPointStyle();
            case TagStyle_Rel: return getTagStyle();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ShowLine, ShowArea, ShowPoints, ShowTags
            case ShowLine_Prop: setShowLine(SnapUtils.boolValue(aValue)); break;
            case ShowArea_Prop: setShowArea(SnapUtils.boolValue(aValue)); break;
            case ShowPoints_Prop: setShowPoints(SnapUtils.boolValue(aValue)); break;
            case ShowTags_Prop: setShowTags(SnapUtils.boolValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLine, ShowArea
        if (!isShowLine())
            e.add(ShowLine_Prop, false);
        if (isShowArea())
            e.add(ShowArea_Prop, true);

        // Archive ShowPoints
        if (isShowPoints()) {
            e.add(ShowPoints_Prop, true);

            // Archive PointStyle
            PointStyle pointStyle = getPointStyle();
            XMLElement pointStyleXML = pointStyle.toXML(anArchiver);
            if (pointStyleXML.getAttributeCount() > 0 || pointStyleXML.getElementCount() > 0)
                e.addElement(pointStyleXML);
        }

        // Archive ShowTags
        if (isShowTags()) {
            e.add(ShowTags_Prop, true);

            // Archive TagStyle
            TagStyle tagStyle = getTagStyle();
            XMLElement tagStyleXML = tagStyle.toXML(anArchiver);
            if (tagStyleXML.getAttributeCount() > 0 || tagStyleXML.getElementCount() > 0)
                e.addElement(tagStyleXML);
        }

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

        // Archive Stacked, Disabled, ShowLegendEntry
        if (isStacked())
            e.add(Stacked_Prop, true);
        if (isDisabled())
            e.add(Disabled_Prop, true);
        if (!isShowLegendEntry())
            e.add(ShowLegendEntry_Prop, false);

        // Archive TraceStyle
        TraceStyle traceStyle = getTraceStyle();
        XMLElement traceStyleXML = traceStyle.toXML(anArchiver);
        if (traceStyleXML.getAttributeCount() > 0) {
            traceStyleXML.setName("TraceStyle");
            e.addElement(traceStyleXML);
        }

        // Archive DataSet
        DataSet dataSet = getDataSet();
        dataSet.toXML(anArchiver, e);

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

        // Unarchive ShowLine, ShowArea
        if (anElement.hasAttribute(ShowLine_Prop))
            setShowLine(anElement.getAttributeBoolValue(ShowLine_Prop));
        if (anElement.hasAttribute(ShowArea_Prop))
            setShowArea(anElement.getAttributeBoolValue(ShowArea_Prop));

        // Unarchive ShowPoints (and legacy ShowSymbols)
        if (anElement.hasAttribute(ShowPoints_Prop))
            setShowPoints(anElement.getAttributeBoolValue(ShowPoints_Prop));
        else if (anElement.hasAttribute("ShowSymbols"))
            setShowPoints(anElement.getAttributeBoolValue("ShowSymbols"));

        // Unarchive ShowTags
        if (anElement.hasAttribute(ShowTags_Prop))
            setShowTags(anElement.getAttributeBoolValue(ShowTags_Prop));

        // Unarchive PointStyle
        XMLElement pointStyleXML = anElement.getElement("PointStyle");
        if (pointStyleXML == null)
            pointStyleXML = anElement.getElement("SymbolStyle");
        if (pointStyleXML != null)
            getPointStyle().fromXML(anArchiver, pointStyleXML);

        // Unarchive TagStyle
        XMLElement tagStyleXML = anElement.getElement("TagStyle");
        if (tagStyleXML != null)
            getTagStyle().fromXML(anArchiver, tagStyleXML);

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

        // Unarchive Stacked, Disabled, ShowLegendEntry
        if (anElement.hasAttribute(Stacked_Prop))
            setStacked(anElement.getAttributeBoolValue(Stacked_Prop, false));
        if (anElement.hasAttribute(Disabled_Prop))
            setDisabled(anElement.getAttributeBoolValue(Disabled_Prop, false));
        if (anElement.hasAttribute(ShowLegendEntry_Prop))
            setShowLegendEntry(anElement.getAttributeBoolValue(ShowLegendEntry_Prop));

        // Unarchive TraceStyle
        XMLElement traceStyleXML = anElement.getElement("TraceStyle");
        if (traceStyleXML == null)
            traceStyleXML = anElement.getElement("DataStyle");
        if (traceStyleXML != null) {
            getTraceStyle().fromXML(anArchiver, traceStyleXML);
        }

        // Unarchive DataSet
        DataSet dataSet = getDataSet();
        dataSet.fromXML(anArchiver, anElement);

        // Return this part
        return this;
    }
}