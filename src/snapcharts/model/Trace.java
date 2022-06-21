/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.props.PropChange;
import snap.props.PropSet;
import snap.util.*;
import snapcharts.data.*;

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

    // The method by which points are joined
    private PointJoin  _pointJoin;

    // The FillMode
    private FillMode  _fillMode;

    // The PointStyle
    private PointStyle  _pointStyle = new PointStyle(this);

    // The TagStyle
    private TagStyle  _tagStyle = new TagStyle(this);

    // The TraceStyleHpr
    private TraceStyleHpr  _traceStyleHpr;

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

    // Constant for how Trace area should be filled
    public enum FillMode { None, ToZeroY, ToNextY, ToZeroX, ToNextX, ToSelf, ToNext }

    // Constants for properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String ShowArea_Prop = "ShowArea";
    public static final String ShowPoints_Prop = "ShowPoints";
    public static final String ShowTags_Prop = "ShowTags";
    public static final String PointJoin_Prop = "PointJoin";
    public static final String FillMode_Prop = "FillMode";
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
    public static final String TraceStyle_Rel = "TraceStyle";

    // Properties for defaults
    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final PointJoin DEFAULT_POINT_JOIN = PointJoin.Line;
    public static final FillMode DEFAULT_FILL_MODE = FillMode.None;

    /**
     * Constructor.
     */
    public Trace()
    {
        super();

        // Set defaults
        _lineWidth = DEFAULT_LINE_WIDTH;
        _pointJoin = DEFAULT_POINT_JOIN;
        _fillMode = DEFAULT_FILL_MODE;

        // Register listener for TagStyle, PointStyle prop changes
        _tagStyle.addPropChangeListener(pc -> childChartPartDidPropChange(pc));
        _pointStyle.addPropChangeListener(pc -> childChartPartDidPropChange(pc));

        // Configure TraceStyle via TraceStyleHpr
        _traceStyleHpr = new TraceStyleHpr(this);
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
     * Returns the method by which points are joined.
     */
    public PointJoin getPointJoin()  { return _pointJoin; }

    /**
     * Sets the method by which points are joined.
     */
    public void setPointJoin(PointJoin aPointJoin)
    {
        if (aPointJoin == getPointJoin()) return;
        firePropChange(PointJoin_Prop, _pointJoin, _pointJoin = aPointJoin);
    }

    /**
     * Returns the FillMode (how/whether to paint the data area).
     */
    public FillMode getFillMode()  { return _fillMode; }

    /**
     * Sets the FillMode (how/whether to paint the data area).
     */
    public void setFillMode(FillMode aFillMode)
    {
        if (aFillMode == _fillMode) return;
        firePropChange(FillMode_Prop, _fillMode, _fillMode = aFillMode);
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
     * Returns the default line color.
     */
    public Color getDefaultLineColor()
    {
        int index = getIndex();
        return getColorMapColor(index);
    }

    /**
     * Returns the color map color at index.
     */
    public Color getColorMapColor(int anIndex)
    {
        Chart chart = getChart();
        return chart.getColor(anIndex);
    }

    /**
     * Returns the default color to fill the data area.
     */
    public Color getFillColorDefault()
    {
        // Get from LineColor, half transparent
        return getDefaultLineColor().copyForAlpha(.5);
    }

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
    public DataUnit getThetaUnit()  { return _dataSet.getThetaUnit(); }

    /**
     * Sets the units for Theta data.
     */
    public void setThetaUnit(DataUnit aValue)
    {
        // If already set, just return
        if (aValue == getThetaUnit()) return;

        // Forward to DataSet
        DataUnit old = getThetaUnit();
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
        if (aValue == isStacked()) return;
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
        if (aValue == isDisabled()) return;
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
        if (aValue == isShowLegendEntry()) return;
        firePropChange(ShowLegendEntry_Prop, _showLegendEntry, _showLegendEntry = aValue);
    }

    /**
     * Returns the number of points.
     */
    public int getPointCount()  { return _dataSet.getPointCount(); }

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
     * Adds a given point at given index.
     */
    public void addPoint(DataPoint aPoint, int anIndex)
    {
        // Forward to DataSet
        _dataSet.addPoint(aPoint, anIndex);

        // Clear cache and firePropChange
        clearCachedData();
        firePropChange(Point_Prop, aPoint, null, anIndex);
    }

    /**
     * Removes a point at given index.
     */
    public void removePoint(int anIndex)
    {
        // Get point at index
        DataPoint dataPoint = getPoint(anIndex);

        // Forward to DataSet
        _dataSet.removePoint(anIndex);

        // Clear cache and firePropChange
        clearCachedData();
        firePropChange(Point_Prop, dataPoint, null, anIndex);
    }

    /**
     * Sets a given point at given index.
     */
    public void setPoint(DataPoint aPoint, int anIndex)
    {
        // Get point at index
        DataPoint dataPoint = getPoint(anIndex);

        // Forward to DataSet
        _dataSet.setPoint(aPoint, anIndex);

        // Clear cache and firePropChange
        clearCachedData();
        firePropChange(Point_Prop, aPoint, dataPoint, anIndex);
    }

    /**
     * Return data point as a string (either C or X).
     */
    public String getString(int anIndex)
    {
        // If point string is set, just return it
        DataSet procData = getProcessedData();
        String str = procData.getC(anIndex);
        if(str != null)
            return str;

        // If categories, return category at index
        Chart chart = getChart();
        List<String> categories = chart.getAxisX().getCategories();
        if (categories != null && anIndex < categories.size())
            return categories.get(anIndex);

        // If X value is int, return as int string
        double val = procData.getX(anIndex);
        if (val == (int) val)
            return String.valueOf((int) val);

        // Return formatted X value
        return DataUtils.formatValue(val);
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
        if (_logData != null && _logData[index] != null)
            return _logData[index];

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
     * Returns whether this trace is clear (no name and no values).
     */
    public boolean isClear()
    {
        if (getName() != null && getName().length() > 0)
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
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Called when a child chart part has prop change.
     */
    private void childChartPartDidPropChange(PropChange aPC)
    {
        Chart chart = getChart();
        chart.chartPartDidPropChange(aPC);
    }

    /**
     * Standard toStringProps implementation.
     */
    @Override
    public String toStringProps()
    {
        // Do normal version
        StringBuilder sb = new StringBuilder(super.toStringProps());
        if (sb.length() > 0) sb.append(", ");

        // Add DataType, PointCount
        sb.append("DataType=").append(getDataType());
        sb.append(", PointCount=").append(getPointCount());

        // Add DataChan Min/Max
        DataSet dataSet = getDataSet();
        String dataSetProps = dataSet.toStringProps();
        sb.append(", DataSet={ ").append(dataSetProps).append(" }");

        // Return
        return sb.toString();
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override LineWidth
        aPropSet.getPropForName(LineWidth_Prop).setDefaultValue(DEFAULT_LINE_WIDTH);

        // Handle ShowLine, ShowArea, ShowPoints, ShowTags
        aPropSet.addPropNamed(ShowLine_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowArea_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowPoints_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowTags_Prop, boolean.class, false);

        // PointJoin, FillMode
        aPropSet.addPropNamed(PointJoin_Prop, PointJoin.class, DEFAULT_POINT_JOIN);
        aPropSet.addPropNamed(FillMode_Prop, FillMode.class, DEFAULT_FILL_MODE);

        // Handle PointStyleRel, TagStyle_Rel, TraceStyle_Rel
        aPropSet.addPropNamed(PointStyle_Rel, PointStyle.class, null);
        aPropSet.addPropNamed(TagStyle_Rel, TagStyle.class, null);
        aPropSet.addPropNamed(TraceStyle_Rel, TraceStyle.class, null);
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

            // Handle PointJoin, FillMode
            case PointJoin_Prop: return getPointJoin();
            case FillMode_Prop: return getFillMode();

            // Handle PointStyleRel, TagStyle_Rel, TraceStyle_Rel
            case PointStyle_Rel: return getPointStyle();
            case TagStyle_Rel: return getTagStyle();
            case TraceStyle_Rel: return getTraceStyle();

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

            // Handle PointJoint, FillMode
            case PointJoin_Prop: setPointJoin((PointJoin) aValue); break;
            case FillMode_Prop: setFillMode((FillMode) aValue); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Override to define TraceStyle defaults
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Override Fill, LineColor
            case Fill_Prop: return getFillColorDefault();
            case LineColor_Prop: return getDefaultLineColor();

            // Do normal version
            default: return super.getPropDefault(aPropName);
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

        // Archive ShowLine, ShowArea, ShowPoints, ShowTags
        if (!isShowLine())
            e.add(ShowLine_Prop, false);
        if (isShowArea())
            e.add(ShowArea_Prop, true);
        if (isShowPoints())
            e.add(ShowPoints_Prop, true);
        if (isShowTags())
            e.add(ShowTags_Prop, true);

        // Archive PointJoin, FillMode
        if (!isPropDefault(PointJoin_Prop))
            e.add(PointJoin_Prop, getPointJoin());
        if (!isPropDefault(FillMode_Prop))
            e.add(FillMode_Prop, getFillMode());

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

        // Archive PointStyle
        PointStyle pointStyle = getPointStyle();
        XMLElement pointStyleXML = pointStyle.toXML(anArchiver);
        if (pointStyleXML.getAttributeCount() > 0 || pointStyleXML.getElementCount() > 0)
            e.addElement(pointStyleXML);

        // Archive TagStyle
        TagStyle tagStyle = getTagStyle();
        XMLElement tagStyleXML = tagStyle.toXML(anArchiver);
        if (tagStyleXML.getAttributeCount() > 0 || tagStyleXML.getElementCount() > 0)
            e.addElement(tagStyleXML);

        // Archive TraceStyle
        TraceStyle traceStyle = getTraceStyle();
        XMLElement traceStyleXML = traceStyle.toXML(anArchiver);
        if (traceStyleXML.getAttributeCount() > 0 || traceStyleXML.getElementCount() > 0)
            e.addElement(traceStyleXML);

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

        // This is for brief time when DataStyle (TraceStyle) held standard Trace display props
        XMLElement dataStyleXML = anElement.getElement("DataStyle");
        if (dataStyleXML != null)
            fromLegacyDataStyleXML(anArchiver, dataStyleXML);

        // Unarchive ShowLine, ShowArea, ShowPoints, ShowTags
        if (anElement.hasAttribute(ShowLine_Prop))
            setShowLine(anElement.getAttributeBoolValue(ShowLine_Prop));
        if (anElement.hasAttribute(ShowArea_Prop))
            setShowArea(anElement.getAttributeBoolValue(ShowArea_Prop));
        if (anElement.hasAttribute(ShowPoints_Prop))
            setShowPoints(anElement.getAttributeBoolValue(ShowPoints_Prop));
        else if (anElement.hasAttribute("ShowSymbols")) // Legacy
            setShowPoints(true);
        if (anElement.hasAttribute(ShowTags_Prop))
            setShowTags(anElement.getAttributeBoolValue(ShowTags_Prop));

        // Unarchive PointJoin, FillMode
        if (anElement.hasAttribute(PointJoin_Prop))
            setPointJoin(anElement.getAttributeEnumValue(PointJoin_Prop, PointJoin.class, DEFAULT_POINT_JOIN));
        if (anElement.hasAttribute(FillMode_Prop))
            setFillMode(anElement.getAttributeEnumValue(FillMode_Prop, FillMode.class, DEFAULT_FILL_MODE));

        // Unarchive PointStyle
        XMLElement pointStyleXML = anElement.getElement(PointStyle_Rel);
        if (pointStyleXML != null)
            getPointStyle().fromXML(anArchiver, pointStyleXML);

        // Unarchive TagStyle
        XMLElement tagStyleXML = anElement.getElement(TagStyle_Rel);
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
        XMLElement traceStyleXML = anElement.getElement(TraceStyle_Rel);
        if (traceStyleXML != null)
            getTraceStyle().fromXML(anArchiver, traceStyleXML);

        // Unarchive DataSet
        DataSet dataSet = getDataSet();
        dataSet.fromXML(anArchiver, anElement);

        // Return this part
        return this;
    }

    /**
     * Legacy unarchival for time when DataStyle held most Trace display properties.
     */
    private void fromLegacyDataStyleXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        if (anElement.hasAttribute(Trace.ShowLine_Prop) || anElement.hasAttribute(FillMode_Prop))
            super.fromXML(anArchiver, anElement);

        if (anElement.hasAttribute(Trace.ShowLine_Prop))
            setShowLine(anElement.getAttributeBoolValue(Trace.ShowLine_Prop));
        if (anElement.hasAttribute(FillMode_Prop))
            setShowArea(true);
        if (anElement.hasAttribute("ShowSymbols"))
            setShowPoints(anElement.getAttributeBoolValue("ShowSymbols"));
        if (anElement.hasAttribute(Trace.ShowTags_Prop))
            setShowTags(anElement.getAttributeBoolValue(Trace.ShowTags_Prop));
        XMLElement pointStyleXML = anElement.getElement("SymbolStyle");
        if (pointStyleXML != null)
            getPointStyle().fromXML(anArchiver, pointStyleXML);
        XMLElement tagStyleXML = anElement.getElement("TagStyle");
        if (tagStyleXML != null)
            getTagStyle().fromXML(anArchiver, tagStyleXML);
        if (anElement.hasAttribute(PointJoin_Prop))
            setPointJoin(anElement.getAttributeEnumValue(PointJoin_Prop, PointJoin.class, null));
        if (anElement.hasAttribute(PointStyle.PointSpacing_Prop))
            getPointStyle().setPointSpacing(anElement.getAttributeIntValue(PointStyle.PointSpacing_Prop));
        if (anElement.hasAttribute(PointStyle.MaxPointCount_Prop))
            getPointStyle().setMaxPointCount(anElement.getAttributeIntValue(PointStyle.MaxPointCount_Prop));
        if (anElement.hasAttribute(PointStyle.SkipPointCount_Prop))
            getPointStyle().setSkipPointCount(anElement.getAttributeIntValue(PointStyle.SkipPointCount_Prop));
    }
}