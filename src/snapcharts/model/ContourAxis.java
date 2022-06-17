/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.props.PropSet;
import snap.util.*;
import snapcharts.util.MinMax;
import java.util.Objects;

/**
 * A class to represent a ContourAxis. Subclasses axis because of the similarities.
 */
public class ContourAxis extends ChartPart {

    // The Title
    private String  _title;

    // The Axis Min Bounding
    private AxisBound  _minBound = AxisBound.AUTO;

    // The Axis Min Bounding
    private AxisBound  _maxBound = AxisBound.AUTO;

    // The Axis Min Value (if MinBounding is VALUE)
    private double  _minValue;

    // The Axis Max Value (if MaxBounding is VALUE)
    private double  _maxValue;

    // The number of levels of contours
    private int  _levelCount = 16;

    // Constants for properties
    public static final String Title_Prop = "Title";
    public static final String MinBound_Prop = "MinBound";
    public static final String MaxBound_Prop = "MaxBound";
    public static final String MinValue_Prop = "MinValue";
    public static final String MaxValue_Prop = "MaxValue";
    public final String LevelCount_Prop = "LevelCount";

    // Constants for property defaults
    public static final int DEFAULT_LEVEL_COUNT = 16;
    private static Insets DEFAULT_CONTOUR_AXIS_MARGIN = new Insets(0, 5, 0, 5);
    private static Insets DEFAULT_CONTOUR_AXIS_PADDING = new Insets(5, 5, 5, 5);

    /**
     * Constructor.
     */
    public ContourAxis()
    {
        super();
    }

    /**
     * Returns the Axis title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the Axis title.
     */
    public void setTitle(String aStr)
    {
        if (Objects.equals(aStr, _title)) return;
        firePropChange(Title_Prop, _title, _title =aStr);
    }

    /**
     * Returns the Axis Min Bound.
     */
    public AxisBound getMinBound()  { return _minBound; }

    /**
     * Sets the Axis Min Bound.
     */
    public void setMinBound(AxisBound aBound)
    {
        if (aBound == _minBound) return;
        firePropChange(MinBound_Prop, _minBound, _minBound = aBound);
    }

    /**
     * Returns the Axis Max Bound.
     */
    public AxisBound getMaxBound()  { return _maxBound; }

    /**
     * Sets the Axis Max Bound.
     */
    public void setMaxBound(AxisBound aBound)
    {
        if (aBound == _maxBound) return;
        firePropChange(MaxBound_Prop, _maxBound, _maxBound = aBound);
    }

    /**
     * Returns the Axis Min Value (if AxisBound is VALUE).
     */
    public double getMinValue()  { return _minValue; }

    /**
     * Sets the Axis Min Value (if AxisBound is VALUE).
     */
    public void setMinValue(double aValue)
    {
        if (aValue == _minValue) return;
        firePropChange(MinValue_Prop, _minValue, _minValue = aValue);
    }

    /**
     * Returns the Axis Max Value (if AxisBound is VALUE).
     */
    public double getMaxValue()  { return _maxValue; }

    /**
     * Sets the Axis Max Value (if AxisBound is VALUE).
     */
    public void setMaxValue(double aValue)
    {
        if (aValue == _maxValue) return;
        firePropChange(MaxValue_Prop, _maxValue, _maxValue = aValue);
    }

    /**
     * Returns the min/max value.
     */
    public MinMax getMinMax()
    {
        double min = getMinValue();
        double max = getMaxValue();
        return new MinMax(min, max);
    }

    /**
     * Returns the number of levels of contours.
     */
    public int getLevelCount()  { return _levelCount; }

    /**
     * Sets the number of levels of contours.
     */
    public void setLevelCount(int aValue)
    {
        if (aValue == getLevelCount()) return;
        firePropChange(LevelCount_Prop, _levelCount, _levelCount = aValue);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add Props
        aPropSet.addProps(MinBound_Prop, MaxBound_Prop, MinValue_Prop, MaxValue_Prop, LevelCount_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // MinBound, MaxBound, MinValue, MaxValue
            case MinBound_Prop: return getMinBound();
            case MaxBound_Prop: return getMaxBound();
            case MinValue_Prop: return getMinValue();
            case MaxValue_Prop: return getMaxValue();

            // LevelCount
            case LevelCount_Prop: return getLevelCount();

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

            // MinBound, MaxBound, MinValue, MaxValue
            case MinBound_Prop: setMinBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MaxBound_Prop: setMaxBound(AxisBound.get(SnapUtils.stringValue(aValue))); break;
            case MinValue_Prop: setMinValue(SnapUtils.doubleValue(aValue)); break;
            case MaxValue_Prop: setMaxValue(SnapUtils.doubleValue(aValue)); break;

            // LevelCount
            case LevelCount_Prop: setLevelCount(SnapUtils.intValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Returns the prop default value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // MinBound, MaxBound, MinValue, MaxValue
            case MinBound_Prop: return AxisBound.AUTO;
            case MaxBound_Prop: return AxisBound.AUTO;
            case MinValue_Prop: return 0;
            case MaxValue_Prop: return 5;

            // LevelCount
            case LevelCount_Prop: return DEFAULT_LEVEL_COUNT;

            // Margin, Padding override
            case Margin_Prop: return DEFAULT_CONTOUR_AXIS_MARGIN;
            case Padding_Prop: return DEFAULT_CONTOUR_AXIS_PADDING;

            // Superclass properties
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

        // Archive Title
        if (getTitle() != null && getTitle().length() > 0)
            e.add(Title_Prop, getTitle());

        // Archive MinBound, MaxBounds, MinValue, MaxValue
        if (!isPropDefault(MinBound_Prop))
            e.add(MinBound_Prop, getMinBound());
        if (!isPropDefault(MaxBound_Prop))
            e.add(MaxBound_Prop, getMaxBound());
        if (!isPropDefault(MinValue_Prop))
            e.add(MinValue_Prop, getMinValue());
        if (!isPropDefault(MaxValue_Prop))
            e.add(MaxValue_Prop, getMaxValue());

        // Archive LevelCount
        if (!isPropDefault(LevelCount_Prop))
            e.add(LevelCount_Prop, getLevelCount());

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

        // Unarchive Title
        if (anElement.hasAttribute(Title_Prop))
            setTitle(anElement.getAttributeValue(Title_Prop));

        // Unarchive MinBound, MaxBounds, MinValue, MaxValue
        if (anElement.hasAttribute(MinBound_Prop))
            setMinBound(anElement.getAttributeEnumValue(MinBound_Prop, AxisBound.class, null));
        if (anElement.hasAttribute(MaxBound_Prop))
            setMaxBound(anElement.getAttributeEnumValue(MaxBound_Prop, AxisBound.class, null));
        if (anElement.hasAttribute(MinValue_Prop))
            setMinValue(anElement.getAttributeDoubleValue(MinValue_Prop));
        if (anElement.hasAttribute(MaxValue_Prop))
            setMaxValue(anElement.getAttributeDoubleValue(MaxValue_Prop));

        // Unarchive LevelCount
        if (anElement.hasAttribute(LevelCount_Prop))
            setLevelCount(anElement.getAttributeIntValue(LevelCount_Prop));

        // Return this part
        return this;
    }
}
