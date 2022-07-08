/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.props.PropSet;
import snap.util.SnapUtils;

/**
 * This ChartPart subclass holds style properties for Trace points.
 */
public class PointStyle extends ChartPart {

    // The Trace that holds this PointStyle
    private Trace  _trace;

    // The Symbol Size
    private int  _symbolSize = DEFAULT_SYMBOL_SIZE;

    // The Symbol Id
    private int  _symbolId;

    // The cached symbol
    private Symbol  _symbol;

    // The maximum number of symbols/tags visible
    private int  _maxPointCount = DEFAULT_MAX_POINT_COUNT;

    // The number of symbols/tags to skip to avoid excessive overlap
    private int  _skipPointCount = DEFAULT_SKIP_POINT_COUNT;

    // The minimum amount of space between symbols/tags to avoid excessive overlap
    private int  _pointSpacing = DEFAULT_POINT_SPACING;

    // Constants for properties
    public static final String SymbolSize_Prop = "SymbolSize";
    public static final String SymbolId_Prop = "SymbolId";
    public static final String PointSpacing_Prop = "PointSpacing";
    public static final String MaxPointCount_Prop = "MaxPointCount";
    public static final String SkipPointCount_Prop = "SkipPointCount";

    // Constants for property defaults
    public static final int DEFAULT_SYMBOL_SIZE = 8;
    public static final Color DEFAULT_SYMBOL_LINE_COLOR = Trace.DEFAULT_DYNAMIC_COLOR;
    public static final int DEFAULT_SYMBOL_LINE_WIDTH = 0;
    public static final Color DEFAULT_SYMBOL_FILL = Trace.DEFAULT_DYNAMIC_COLOR;
    public static final int DEFAULT_POINT_SPACING = 0;
    public static final int DEFAULT_MAX_POINT_COUNT = 0;
    public static final int DEFAULT_SKIP_POINT_COUNT = 0;

    /**
     * Constructor.
     */
    public PointStyle(Trace aTrace)
    {
        super();
        _trace = aTrace;

        // Set defaults special: These are computed dynamic if not explicitly set
        _lineColor = Trace.DEFAULT_DYNAMIC_COLOR;
        _fill = Trace.DEFAULT_DYNAMIC_COLOR;
    }

    /**
     * Returns the Symbol size.
     */
    public int getSymbolSize()  { return _symbolSize; }

    /**
     * Sets the Symbol size.
     */
    public void setSymbolSize(int aValue)
    {
        if (aValue == getSymbolSize()) return;
        firePropChange(SymbolSize_Prop, _symbolSize, _symbolSize = aValue);
        _symbol = null;
    }

    /**
     * Returns the Symbol Id.
     */
    public int getSymbolId()  { return _symbolId; }

    /**
     * Sets the Symbol Id.
     */
    public void setSymbolId(int aValue)
    {
        if (aValue == getSymbolId()) return;
        firePropChange(SymbolId_Prop, _symbolId, _symbolId = aValue);
        _symbol = null;
    }

    /**
     * Returns the Symbol.
     */
    public Symbol getSymbol()
    {
        // If already set, just return
        if (_symbol != null) return _symbol;

        // Get, set, return
        Symbol symbol = Symbol.getSymbolForId(_symbolId).copyForSize(_symbolSize);
        return _symbol = symbol;
    }

    /**
     * Returns the minimum amount of space between symbols/tags to avoid excessive overlap.
     */
    public int getPointSpacing()  { return _pointSpacing; }

    /**
     * Sets the minimum amount of space between symbols/tags to avoid excessive overlap.
     */
    public void setPointSpacing(int aValue)
    {
        if (aValue == getPointSpacing()) return;
        firePropChange(PointSpacing_Prop, _pointSpacing, _pointSpacing = aValue);
    }

    /**
     * Returns the maximum number of symbols/tags visible.
     */
    public int getMaxPointCount()  { return _maxPointCount; }

    /**
     * Sets the maximum number of symbols/tags visible.
     */
    public void setMaxPointCount(int aValue)
    {
        if (aValue == getMaxPointCount()) return;
        firePropChange(MaxPointCount_Prop, _maxPointCount, _maxPointCount = aValue);
    }

    /**
     * Returns the number of symbols/tags to skip to avoid excessive overlap.
     */
    public int getSkipPointCount()  { return _skipPointCount; }

    /**
     * Sets the number of symbols/tags to skip to avoid excessive overlap.
     */
    public void setSkipPointCount(int aValue)
    {
        if (aValue == getSkipPointCount()) return;
        firePropChange(SkipPointCount_Prop, _skipPointCount, _skipPointCount = aValue);
    }

    /**
     * Override to dynamically get line color if not explicitly set.
     */
    @Override
    public Color getLineColor()
    {
        if (_lineColor == Trace.DEFAULT_DYNAMIC_COLOR)
            return getDefaultLineColor();
        return super.getLineColor();
    }

    /**
     * Override to dynamically get fill color if not explicitly set.
     */
    @Override
    public Paint getFill()
    {
        if (_fill == Trace.DEFAULT_DYNAMIC_COLOR)
            return getDefaultFill();
        return super.getFill();
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultLineColor()
    {
        return _trace.getLineColor();
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultFill()
    {
        if (getLineWidth() > 0 && _lineColor == Trace.DEFAULT_DYNAMIC_COLOR)
            return Color.WHITE;
        return _trace.getDefaultLineColor();
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // SymbolSize, SymbolId
        aPropSet.addPropNamed(SymbolSize_Prop, int.class, DEFAULT_SYMBOL_SIZE);
        aPropSet.addPropNamed(SymbolId_Prop, int.class, 0);

        // PointSpacing, MaxPointCount, SkipPointCount
        aPropSet.addPropNamed(PointSpacing_Prop, int.class, DEFAULT_POINT_SPACING);
        aPropSet.addPropNamed(MaxPointCount_Prop, int.class, DEFAULT_MAX_POINT_COUNT);
        aPropSet.addPropNamed(SkipPointCount_Prop, int.class, DEFAULT_SKIP_POINT_COUNT);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // SymbolSize, SymbolId
            case SymbolSize_Prop: return getSymbolSize();
            case SymbolId_Prop: return getSymbolId();

            // Handle PointSpacing, MaxPointCount, SkipPointCount
            case PointSpacing_Prop: return getPointSpacing();
            case MaxPointCount_Prop: return getMaxPointCount();
            case SkipPointCount_Prop: return getSkipPointCount();

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

            // SymbolSize, SymbolId
            case SymbolSize_Prop: setSymbolSize(SnapUtils.intValue(aValue)); break;
            case SymbolId_Prop: setSymbolId(SnapUtils.intValue(aValue)); break;

            // Handle PointSpacing, MaxPointCount, SkipPointCount
            case PointSpacing_Prop: setPointSpacing(SnapUtils.intValue(aValue)); break;
            case MaxPointCount_Prop: setMaxPointCount(SnapUtils.intValue(aValue)); break;
            case SkipPointCount_Prop: setSkipPointCount(SnapUtils.intValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * Override to provide custom defaults.
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
