/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.gfx.Color;
import snap.util.*;

/**
 * A class to represent properties to render data for a specific ChartType.
 */
public class TraceStyle extends ChartPart {

    // The method by which points are joined
    private PointJoin _pointJoin = PointJoin.Line;

    // The FillMode
    private FillMode  _fillMode = FillMode.None;

    // The maximum number of symbols/tags visible
    private int  _maxPointCount = DEFAULT_MAX_POINT_COUNT;

    // The number of symbols/tags to skip to avoid excessive overlap
    private int  _skipPointCount = DEFAULT_SKIP_POINT_COUNT;

    // The minimum amount of space between symbols/tags to avoid excessive overlap
    private int  _pointSpacing = DEFAULT_POINT_SPACING;

    // Constants for properties
    public static final String PointJoin_Prop = "PointJoin";
    public static final String FillMode_Prop = "FillMode";
    public static final String PointSpacing_Prop = "PointSpacing";
    public static final String MaxPointCount_Prop = "MaxPointCount";
    public static final String SkipPointCount_Prop = "SkipPointCount";

    // Constants for property defaults
    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final PointJoin DEFAULT_POINT_JOIN = PointJoin.Line;
    public static final FillMode DEFAULT_FILL_MODE = FillMode.None;
    public static final int DEFAULT_POINT_SPACING = 0;
    public static final int DEFAULT_MAX_POINT_COUNT = 0;
    public static final int DEFAULT_SKIP_POINT_COUNT = 0;

    // Constant for how Trace area should be filled
    public enum FillMode { None, ToZeroY, ToNextY, ToZeroX, ToNextX, ToSelf, ToNext };

    /**
     * Constructor.
     */
    public TraceStyle()
    {
        super();
        setLineWidth(DEFAULT_LINE_WIDTH);
    }

    /**
     * Returns the default line color.
     */
    public Color getDefaultLineColor()
    {
        // Get from Trace
        ChartPart parent = getParent();
        if (parent instanceof Trace) {
            Trace trace = (Trace) parent;
            int index = trace.getIndex();
            return getColorMapColor(index);
        }

        // Shouldn't get here
        return Color.BLACK;
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
     * Returns whether to paint area for data.
     */
    //public boolean isShowArea()  { return _fillMode != FillMode.None; }

    /**
     * Sets whether to paint area for data.
     */
    /*public void setShowArea(boolean aValue)
    {
        FillMode fillMode = aValue ? FillMode.ToZeroY : FillMode.None;
        setFillMode(fillMode);
    }*/

    /**
     * Returns the default color to fill the data area.
     */
    public Color getFillColorDefault()
    {
        // Get from LineColor, half transparent
        return getDefaultLineColor().copyForAlpha(.5);
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
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }

    /**
     * Override to register props.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        aPropDefaults.addProps(PointJoin_Prop, FillMode_Prop,
            PointSpacing_Prop, MaxPointCount_Prop, SkipPointCount_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle PointJoin
            case PointJoin_Prop: return getPointJoin();

            // Handle FillMode
            case FillMode_Prop: return getFillMode();

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

            // Handle PointJoint
            case PointJoin_Prop: setPointJoin((PointJoin) aValue); break;

            // Handle FillMode
            case FillMode_Prop: setFillMode((FillMode) aValue); break;

            // Handle PointSpacing, MaxPointCount, SkipPointCount
            case PointSpacing_Prop: setPointSpacing(SnapUtils.intValue(aValue)); break;
            case MaxPointCount_Prop: setMaxPointCount(SnapUtils.intValue(aValue)); break;
            case SkipPointCount_Prop: setSkipPointCount(SnapUtils.intValue(aValue)); break;

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

            // Override LineColor_Prop, LineWidth_Prop
            case LineColor_Prop: return getDefaultLineColor();
            case LineWidth_Prop: return DEFAULT_LINE_WIDTH;

            // Override Fill
            case Fill_Prop: return getFillColorDefault();

            // PointJoin, FillMode
            case PointJoin_Prop: return DEFAULT_POINT_JOIN;
            case FillMode_Prop: return DEFAULT_FILL_MODE;

            // PointSpacing properties
            case PointSpacing_Prop: return DEFAULT_POINT_SPACING;
            case MaxPointCount_Prop: return DEFAULT_MAX_POINT_COUNT;
            case SkipPointCount_Prop: return DEFAULT_SKIP_POINT_COUNT;
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

        // Archive PointJoin
        if (!isPropDefault(PointJoin_Prop))
            e.add(PointJoin_Prop, getPointJoin());

        // Archive FillMode
        if (!isPropDefault(FillMode_Prop))
            e.add(FillMode_Prop, getFillMode());

        // Archive PointSpacing, MaxPointCount, SkipPointCount
        if (!isPropDefault(PointSpacing_Prop))
            e.add(PointSpacing_Prop, getPointSpacing());
        if (!isPropDefault(MaxPointCount_Prop))
            e.add(MaxPointCount_Prop, getMaxPointCount());
        if (!isPropDefault(SkipPointCount_Prop))
            e.add(SkipPointCount_Prop, getSkipPointCount());

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

        // Unarchive FillMode
        if (anElement.hasAttribute(PointJoin_Prop))
            setPointJoin(anElement.getAttributeEnumValue(PointJoin_Prop, PointJoin.class, DEFAULT_POINT_JOIN));

        // Unarchive FillMode
        if (anElement.hasAttribute(FillMode_Prop))
            setFillMode(anElement.getAttributeEnumValue(FillMode_Prop, FillMode.class, DEFAULT_FILL_MODE));

        // Unarchive PointSpacing, MaxPointCount, SkipPointCount
        if (anElement.hasAttribute(PointSpacing_Prop))
            setPointSpacing(anElement.getAttributeIntValue(PointSpacing_Prop));
        if (anElement.hasAttribute(MaxPointCount_Prop))
            setMaxPointCount(anElement.getAttributeIntValue(MaxPointCount_Prop));
        if (anElement.hasAttribute(SkipPointCount_Prop))
            setSkipPointCount(anElement.getAttributeIntValue(SkipPointCount_Prop));

        // Legacy stuff
        Trace trace = (Trace) getParent();
        if (anElement.hasAttribute(Trace.ShowLine_Prop))
            trace.setShowLine(anElement.getAttributeBoolValue(Trace.ShowLine_Prop));
        if (anElement.hasAttribute(FillMode_Prop))
            trace.setShowArea(true);
        if (anElement.hasAttribute(Trace.ShowPoints_Prop))
            trace.setShowPoints(anElement.getAttributeBoolValue(Trace.ShowPoints_Prop));
        else if (anElement.hasAttribute("ShowSymbols"))
            trace.setShowPoints(anElement.getAttributeBoolValue("ShowSymbols"));
        if (anElement.hasAttribute(Trace.ShowTags_Prop))
            trace.setShowTags(anElement.getAttributeBoolValue(Trace.ShowTags_Prop));
        XMLElement pointStyleXML = anElement.getElement("PointStyle");
        if (pointStyleXML == null)
            pointStyleXML = anElement.getElement("SymbolStyle");
        if (pointStyleXML != null)
            trace.getPointStyle().fromXML(anArchiver, pointStyleXML);
        XMLElement tagStyleXML = anElement.getElement("TagStyle");
        if (tagStyleXML != null)
            trace.getTagStyle().fromXML(anArchiver, tagStyleXML);

        // Return this part
        return this;
    }
}
