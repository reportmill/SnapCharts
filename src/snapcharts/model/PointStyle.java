/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.props.PropSet;
import snap.util.SnapUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

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
    public static final Color DEFAULT_SYMBOL_FILL = null;
    public static final Color DEFAULT_SYMBOL_BORDER_COLOR = null; //Color.BLACK;
    public static final int DEFAULT_SYMBOL_BORDER_WIDTH = 0;
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
     * Returns the default fill color.
     */
    private Color getDefaultLineColor()
    {
        return _trace.getLineColor();
    }

    /**
     * Returns the default fill color.
     */
    private Color getDefaultFillColor()
    {
        if (getLineWidth() > 0 && !isLineColorSet())
            return Color.WHITE;
        return _trace.getDefaultLineColor();
    }

    /**
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add Props
        aPropSet.addProps(SymbolSize_Prop, SymbolId_Prop, PointSpacing_Prop, MaxPointCount_Prop, SkipPointCount_Prop);
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
            case Fill_Prop: return getDefaultFillColor();

            // SymbolSize, SymbolId
            case SymbolSize_Prop: return DEFAULT_SYMBOL_SIZE;
            case SymbolId_Prop: return 0;

            // PointSpacing properties
            case PointSpacing_Prop: return DEFAULT_POINT_SPACING;
            case MaxPointCount_Prop: return DEFAULT_MAX_POINT_COUNT;
            case SkipPointCount_Prop: return DEFAULT_SKIP_POINT_COUNT;

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

        // Archive SymbolSize, SymbolId
        if (!isPropDefault(SymbolSize_Prop))
            e.add(SymbolSize_Prop, getSymbolSize());
        if (!isPropDefault(SymbolSize_Prop))
            e.add(SymbolId_Prop, getSymbolId());

        // Archive PointSpacing, MaxPointCount, SkipPointCount
        if (!isPropDefault(PointSpacing_Prop))
            e.add(PointSpacing_Prop, getPointSpacing());
        if (!isPropDefault(MaxPointCount_Prop))
            e.add(MaxPointCount_Prop, getMaxPointCount());
        if (!isPropDefault(SkipPointCount_Prop))
            e.add(SkipPointCount_Prop, getSkipPointCount());

        // Return xml
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

        // Unarchive SymbolSize, SymbolId
        if (anElement.hasAttribute(SymbolSize_Prop))
            setSymbolSize(anElement.getAttributeIntValue(SymbolSize_Prop));
        if (anElement.hasAttribute(SymbolId_Prop))
            setSymbolId(anElement.getAttributeIntValue(SymbolId_Prop, 0));

        // Unarchive PointSpacing, MaxPointCount, SkipPointCount
        if (anElement.hasAttribute(PointSpacing_Prop))
            setPointSpacing(anElement.getAttributeIntValue(PointSpacing_Prop));
        if (anElement.hasAttribute(MaxPointCount_Prop))
            setMaxPointCount(anElement.getAttributeIntValue(MaxPointCount_Prop));
        if (anElement.hasAttribute(SkipPointCount_Prop))
            setSkipPointCount(anElement.getAttributeIntValue(SkipPointCount_Prop));

        // Return
        return this;
    }
}
