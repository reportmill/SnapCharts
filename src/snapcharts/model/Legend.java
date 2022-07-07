/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.*;
import snap.props.Prop;
import snap.props.PropSet;
import snap.util.*;
import java.util.Objects;

/**
 * A class to represent the Legend of chart.
 */
public class Legend extends ChartPart {

    // Whether legend is showing
    private boolean  _showLegend;

    // The ChartText to hold title text
    private ChartText  _title;

    // The legend position
    private Pos  _position;

    // Whether legend is positioned inside
    private boolean  _inside;

    // Hacky support for customer case of floating legend with fractional XY
    private Point  _userXY;

    // Hacky support for customer case of floating legend with user defined size
    private Size  _userSize;

    // Property constants
    public static final String Title_Prop = "Title";
    public static final String ShowLegend_Prop = "ShowLegend";
    public static final String Position_Prop = "Position";
    public static final String Inside_Prop = "Inside";
    public static final String UserXY_Prop = "UserXY";
    public static final String UserSize_Prop = "UserSize";

    // Constants for property defaults
    private static Pos DEFAULT_LEGEND_ALIGN = Pos.TOP_LEFT;
    private static Insets DEFAULT_LEGEND_MARGIN = new Insets(5, 5, 5, 5);
    private static Pos DEFAULT_LEGEND_POSITION = Pos.CENTER_RIGHT;

    /**
     * Constructor.
     */
    public Legend()
    {
        super();

        // Override standard defaults
        _align = DEFAULT_LEGEND_ALIGN;
        _margin = DEFAULT_LEGEND_MARGIN;
        _position = DEFAULT_LEGEND_POSITION;

        // Create/configure text
        _title = new ChartText();
        _title._parent = this;
    }

    /**
     * Returns whether to show legend.
     */
    public boolean isShowLegend()  { return _showLegend; }

    /**
     * Sets whether to show legend.
     */
    public void setShowLegend(boolean aValue)
    {
        if (aValue == isShowLegend()) return;
        firePropChange(ShowLegend_Prop, _showLegend, _showLegend = aValue);
    }

    /**
     * Returns the Legend title ChartText.
     */
    public ChartText getTitle()  { return _title; }

    /**
     * Returns the position.
     */
    public Pos getPosition()  { return _position; }

    /**
     * Sets the position.
     */
    public void setPosition(Pos aValue)
    {
        if (aValue == getPosition() || aValue == null) return;
        firePropChange(Position_Prop, _position, _position = aValue);
    }

    /**
     * Returns whether is positioned inside data area.
     */
    public boolean isInside()  { return _inside; }

    /**
     * Sets whether is positioned inside data area.
     */
    public void setInside(boolean aValue)
    {
        if (aValue == isInside()) return;
        firePropChange(Inside_Prop, _inside, _inside = aValue);
    }

    /**
     * Returns whether legend is floating (bounds are defined by Legend.Marker).
     */
    public boolean isFloating()
    {
        return getPosition() == Pos.CENTER && !isInside();
    }

    /**
     * Hacky support for customer case of floating legend with fractional XY.
     */
    public Point getUserXY()  { return _userXY; }

    /**
     * Hacky support for customer case of floating legend with fractional XY.
     */
    public void setUserXY(Point aPoint)
    {
        if (Objects.equals(aPoint, getUserXY())) return;
        firePropChange(UserXY_Prop, _userXY, _userXY = aPoint);
    }

    /**
     * Hacky support for customer case of floating legend with display coords width/height.
     */
    public Size getUserSize()  { return _userSize; }

    /**
     * Hacky support for customer case of floating legend with fraction XY and display coords width/height.
     */
    public void setUserSize(Size aSize)
    {
        if (Objects.equals(aSize, getUserSize())) return;
        firePropChange(UserSize_Prop, _userSize, _userSize = aSize);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: Align, Margin
        aPropSet.getPropForName(Align_Prop).setDefaultValue(DEFAULT_LEGEND_ALIGN);
        aPropSet.getPropForName(Margin_Prop).setDefaultValue(DEFAULT_LEGEND_MARGIN);

        // ShowLegend, Title
        aPropSet.addPropNamed(ShowLegend_Prop, boolean.class, false);
        Prop titleProp = aPropSet.addPropNamed(Title_Prop, ChartText.class, EMPTY_OBJECT);
        titleProp.setPreexisting(true);

        // Position, Inside
        aPropSet.addPropNamed(Position_Prop, Pos.class, DEFAULT_LEGEND_POSITION);
        aPropSet.addPropNamed(Inside_Prop, boolean.class, false);

        // UserXY, UserSize
        aPropSet.addPropNamed(UserXY_Prop, Point.class, null);
        aPropSet.addPropNamed(UserSize_Prop, Side.class, null);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // ShowLegend, Title
            case ShowLegend_Prop: return isShowLegend();
            case Title_Prop: return getTitle();

            // Position, Inside
            case Position_Prop: return getPosition();
            case Inside_Prop: return isInside();

            // UserXY, UserSize
            case UserXY_Prop: return getUserXY();
            case UserSize_Prop: return getUserSize();

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

            // ShowLegend
            case ShowLegend_Prop: setShowLegend(SnapUtils.boolValue(aValue)); break;

            // Position, Inside
            case Position_Prop: setPosition((Pos) aValue); break;
            case Inside_Prop: setInside(SnapUtils.boolValue(aValue)); break;

            // UserXY, UserSize
            case UserXY_Prop: setUserXY((Point) aValue); break;
            case UserSize_Prop: setUserSize((Size) aValue); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue); break;
        }
    }
}
