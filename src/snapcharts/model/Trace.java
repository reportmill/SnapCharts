/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.props.Prop;
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
    private boolean  _showLine;

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
    private boolean  _showLegendEntry;

    // The original DataSet
    private DataSet  _dataSet = DataSet.newDataSet();

    // The DataSet processed with expressions
    private DataSet  _procData;

    // Processed Data in log form
    private DataSet[]  _logData;

    // Processed data in polar form
    private DataSet  _polarData;

    // Processed data in polar XY form
    private DataSet  _polarXYData;

    // Constant for how Trace area should be filled
    public enum FillMode { None, ToZeroY, ToNextY, ToZeroX, ToNextX, ToSelf, ToNext }

    // Basic properties
    public static final String ShowLine_Prop = "ShowLine";
    public static final String ShowArea_Prop = "ShowArea";
    public static final String ShowPoints_Prop = "ShowPoints";
    public static final String ShowTags_Prop = "ShowTags";

    // Properties for Line/Fill style
    public static final String PointJoin_Prop = "PointJoin";
    public static final String FillMode_Prop = "FillMode";

    // More properties
    public static final String AxisTypeY_Prop = "AxisTypeY";
    public static final String ExprX_Prop = "ExprX";
    public static final String ExprY_Prop = "ExprY";
    public static final String ExprZ_Prop = "ExprZ";
    public static final String Stacked_Prop = "Stacked";
    public static final String ShowLegendEntry_Prop = "ShowLegendEntry";
    public static final String Disabled_Prop = "Disabled";

    // DataSet properties
    public static final String DataSet_Prop = "DataSet";
    public static final String DataType_Prop = DataSet.DataType_Prop;

    // Properties for nested Point/Tag/Trace style objects
    public static final String PointStyle_Prop = "PointStyle";
    public static final String TagStyle_Prop = "TagStyle";
    public static final String TraceStyle_Prop = "TraceStyle";

    // Questionable properties
    public static final String Point_Prop = "Points";
    public static final String ThetaUnit_Prop = "ThetaUnit";

    // Constants for defaults
    public static final boolean DEFAULT_SHOW_LINE = true;
    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final PointJoin DEFAULT_POINT_JOIN = PointJoin.Line;
    public static final FillMode DEFAULT_FILL_MODE = FillMode.None;
    public static final boolean DEFAULT_SHOW_LEGEND_ENTRY = true;

    // A special color constant to indicate dynamic default
    public static final Color DEFAULT_DYNAMIC_COLOR = new Color();

    /**
     * Constructor.
     */
    public Trace()
    {
        super();

        // Set defaults
        _showLine = DEFAULT_SHOW_LINE;
        _lineWidth = DEFAULT_LINE_WIDTH;
        _pointJoin = DEFAULT_POINT_JOIN;
        _fillMode = DEFAULT_FILL_MODE;
        _showLegendEntry = DEFAULT_SHOW_LEGEND_ENTRY;

        // Set defaults special: These are computed dynamic if not explicitly set
        _lineColor = DEFAULT_DYNAMIC_COLOR;
        _fill = DEFAULT_DYNAMIC_COLOR;

        // Register listener for TagStyle, PointStyle prop changes
        _tagStyle._parent = this;
        _tagStyle.addPropChangeListener(pc -> childChartPartDidPropChange(pc));
        _pointStyle._parent = this;
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
     * Returns the color map color at index.
     */
    public Color getColorMapColor(int anIndex)
    {
        Chart chart = getChart();
        return chart.getColor(anIndex);
    }

    /**
     * Override to dynamically get line color if not explicitly set.
     */
    @Override
    public Color getLineColor()
    {
        if (_lineColor == DEFAULT_DYNAMIC_COLOR)
            return getDefaultLineColor();
        return super.getLineColor();
    }

    /**
     * Override to dynamically get fill if not explicitly set.
     */
    @Override
    public Paint getFill()
    {
        if (_fill == DEFAULT_DYNAMIC_COLOR)
            return getDefaultFill();
        return super.getFill();
    }

    /**
     * Returns the default color to draw trace line (Get from ColorMap lookup for Trace.Index).
     */
    public Color getDefaultLineColor()
    {
        int index = getIndex();
        return getColorMapColor(index);
    }

    /**
     * Returns the default color to fill trace area (Get from LineColor, half transparent).
     */
    public Color getDefaultFill()
    {
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
        firePropChange(ThetaUnit_Prop, old, aValue);
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
        if (aDataSet == _dataSet) return;
        clearCachedData();
        firePropChange(DataSet_Prop, _dataSet, _dataSet = aDataSet);
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
     * Called when a child chart part has prop change.
     */
    private void childChartPartDidPropChange(PropChange aPC)
    {
        Chart chart = getChart(); if (chart == null) return;
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
        aPropSet.addPropNamed(ShowLine_Prop, boolean.class, DEFAULT_SHOW_LINE);
        aPropSet.addPropNamed(ShowArea_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowPoints_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowTags_Prop, boolean.class, false);

        // PointJoin, FillMode
        aPropSet.addPropNamed(PointJoin_Prop, PointJoin.class, DEFAULT_POINT_JOIN);
        aPropSet.addPropNamed(FillMode_Prop, FillMode.class, DEFAULT_FILL_MODE);

        // AxisTypeY, ExprX, ExprY, ExprZ
        aPropSet.addPropNamed(AxisTypeY_Prop, AxisType.class, null);
        aPropSet.addPropNamed(ExprX_Prop, String.class, null);
        aPropSet.addPropNamed(ExprY_Prop, String.class, null);
        aPropSet.addPropNamed(ExprZ_Prop, String.class, null);

        // Stacked, ShowLegendEntry, Disabled
        aPropSet.addPropNamed(Stacked_Prop, boolean.class, false);
        aPropSet.addPropNamed(ShowLegendEntry_Prop, String.class, DEFAULT_SHOW_LEGEND_ENTRY);
        aPropSet.addPropNamed(Disabled_Prop, boolean.class, false);

        // PointStyleRel, TagStyle_Rel, TraceStyle_Rel
        Prop pointStyleProp = aPropSet.addPropNamed(PointStyle_Prop, PointStyle.class, EMPTY_OBJECT);
        pointStyleProp.setPreexisting(true);
        Prop tagStyleProp = aPropSet.addPropNamed(TagStyle_Prop, TagStyle.class, EMPTY_OBJECT);
        tagStyleProp.setPreexisting(true);
        Prop traceStyleProp = aPropSet.addPropNamed(TraceStyle_Prop, TraceStyle.class, EMPTY_OBJECT);
        traceStyleProp.setPreexisting(true);

        // DataSet
        aPropSet.addPropNamed(DataSet_Prop, DataSet.class, EMPTY_OBJECT);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // ShowLine, ShowArea, ShowPoints, ShowTags
            case ShowLine_Prop: return isShowLine();
            case ShowArea_Prop: return isShowArea();
            case ShowPoints_Prop: return isShowPoints();
            case ShowTags_Prop: return isShowTags();

            // PointJoin, FillMode
            case PointJoin_Prop: return getPointJoin();
            case FillMode_Prop: return getFillMode();

            // PointStyleRel, TagStyle_Rel, TraceStyle_Rel
            case PointStyle_Prop: return getPointStyle();
            case TagStyle_Prop: return getTagStyle();
            case TraceStyle_Prop: return getTraceStyle();

            // AxisTypeY, ExprX, ExprY, ExprZ
            case AxisTypeY_Prop: return getAxisTypeY();
            case ExprX_Prop: return getExprX();
            case ExprY_Prop: return getExprY();
            case ExprZ_Prop: return getExprZ();

            // Stacked, ShowLegendEntry, Disabled
            case Stacked_Prop: return isStacked();
            case ShowLegendEntry_Prop: return isShowLegendEntry();
            case Disabled_Prop: return isDisabled();

            // DataSet
            case DataSet_Prop: return getDataSet();

            // Do normal version
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

            // ShowLine, ShowArea, ShowPoints, ShowTags
            case ShowLine_Prop: setShowLine(SnapUtils.boolValue(aValue)); break;
            case ShowArea_Prop: setShowArea(SnapUtils.boolValue(aValue)); break;
            case ShowPoints_Prop: setShowPoints(SnapUtils.boolValue(aValue)); break;
            case ShowTags_Prop: setShowTags(SnapUtils.boolValue(aValue)); break;

            // PointJoint, FillMode
            case PointJoin_Prop: setPointJoin((PointJoin) aValue); break;
            case FillMode_Prop: setFillMode((FillMode) aValue); break;

            // AxisTypeY, ExprX, ExprY, ExprZ
            case AxisTypeY_Prop: setAxisTypeY((AxisType) aValue); break;
            case ExprX_Prop: setExprX(SnapUtils.stringValue(aValue)); break;
            case ExprY_Prop: setExprY(SnapUtils.stringValue(aValue)); break;
            case ExprZ_Prop: setExprZ(SnapUtils.stringValue(aValue)); break;

            // Stacked, ShowLegendEntry, Disabled
            case Stacked_Prop: setStacked(SnapUtils.boolValue(aValue)); break;
            case ShowLegendEntry_Prop: setShowLegendEntry(SnapUtils.boolValue(aValue)); break;
            case Disabled_Prop: setDisabled(SnapUtils.boolValue(aValue)); break;

            // DataSet
            case DataSet_Prop: setDataSet((DataSet) aValue); break;

            // Do normal version
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

            // Override LineColor, Fill
            case LineColor_Prop: return getDefaultLineColor();
            case Fill_Prop: return getDefaultFill();

            // Do normal version
            default: return super.getPropDefault(aPropName);
        }
    }
}