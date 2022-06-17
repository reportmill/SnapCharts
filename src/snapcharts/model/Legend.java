/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Pos;
import snap.geom.Size;
import snap.props.PropDefaults;
import snap.util.*;
import snapcharts.data.DataUtils;

import java.util.Objects;

/**
 * A class to represent the Legend of chart.
 */
public class Legend extends ParentPart {

    // Whether legend is showing
    private boolean  _showLegend;

    // The legend position
    private Pos  _position;

    // Whether legend is positioned inside
    private boolean  _inside;

    // The ChartText to hold title text
    private ChartText  _title;

    // Hacky support for customer case of floating legend with fractional XY
    private Point  _userXY;

    // Hacky support for customer case of floating legend with user defined size
    private Size  _userSize;

    // Property constants
    public static final String ShowLegend_Prop = "ShowLegend";
    public static final String Position_Prop = "Position";
    public static final String Inside_Prop = "Inside";
    public static final String UserXY_Prop = "UserXY";
    public static final String UserSize_Prop = "UserSize";

    // Constants for property defaults
    private static Pos DEFAULT_LEGEND_ALIGN = Pos.TOP_LEFT;
    private static Insets DEFAULT_LEGEND_MARGIN = new Insets(5, 5, 5, 5);
    private static Pos DEFAULT_POSITION = Pos.CENTER_RIGHT;

    /**
     * Constructor.
     */
    public Legend()
    {
        super();
        _position = (Pos) getPropDefault(Position_Prop);

        // Create/configure text
        _title = new ChartText();
        addChild(_title);
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
     * Returns the Legend title ChartText.
     */
    public ChartText getTitle()  { return _title; }

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
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        aPropDefaults.addProps(ShowLegend_Prop, Position_Prop, Inside_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // ShowLegend, Position, Inside
            case ShowLegend_Prop: return isShowLegend();
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

            // ShowLegend, Position, Inside
            case ShowLegend_Prop: setShowLegend(SnapUtils.boolValue(aValue)); break;
            case Position_Prop: setPosition((Pos) aValue); break;
            case Inside_Prop: setInside(SnapUtils.boolValue(aValue)); break;

            // UserXY, UserSize
            case UserXY_Prop: setUserXY((Point) aValue); break;
            case UserSize_Prop: setUserSize((Size) aValue); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * Override to provide custom defaults for Legend (Position).
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Align, Margin
            case Align_Prop: return DEFAULT_LEGEND_ALIGN;
            case Margin_Prop: return DEFAULT_LEGEND_MARGIN;

            // Handle Position
            case Position_Prop: return DEFAULT_POSITION;

            // UserXY, UserSize
            case UserXY_Prop: return null;
            case UserSize_Prop: return null;

            // Handle superclass properties
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

        // Archive ShowLegend, Position, Inside
        if (isShowLegend())
            e.add(ShowLegend_Prop, isShowLegend());
        if (!isPropDefault(Position_Prop))
            e.add(Position_Prop, getPosition());
        if (isInside())
            e.add(Inside_Prop, true);

        // Archive Title
        XMLElement titleXML = anArchiver.toXML(_title);
        if (titleXML.getAttributeCount() > 0 || titleXML.getElementCount() > 0) {
            titleXML.setName("Title");
            e.addElement(titleXML);
        }

        // Archive UserXY, UserSize
        if (!isPropDefault(UserXY_Prop)) {
            Point userXY = getUserXY();
            String pointStr = userXY.x + ", " + userXY.y;
            e.add(UserXY_Prop, pointStr);
        }
        if (!isPropDefault(UserSize_Prop)) {
            Size userSize = getUserSize();
            String sizeStr = userSize.width + ", " + userSize.height;
            e.add(UserXY_Prop, sizeStr);
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

        // Unarchive ShowLegend, Position, Inside
        if (anElement.hasAttribute(ShowLegend_Prop))
            setShowLegend(anElement.getAttributeBoolValue(ShowLegend_Prop));
        if (anElement.hasAttribute(Position_Prop))
            setPosition(Pos.get(anElement.getAttributeValue(Position_Prop)));
        if (anElement.hasAttribute(Inside_Prop))
            setInside(anElement.getAttributeBoolValue(Inside_Prop));

        // Unarchive Title
        XMLElement titleXML = anElement.getElement("Title");
        if (titleXML != null)
            getTitle().fromXML(anArchiver, titleXML);

        // Unarchive UserXY, UserSize
        if (anElement.hasAttribute(UserXY_Prop)) {
            String pointStr = anElement.getAttributeValue(UserXY_Prop);
            double[] pointVals = DataUtils.getDoubleArrayForString(pointStr);
            if (pointVals.length > 1)
                setUserXY(new Point(pointVals[0], pointVals[1]));
        }
        if (anElement.hasAttribute(UserSize_Prop)) {
            String sizeStr = anElement.getAttributeValue(UserSize_Prop);
            double[] sizeVals = DataUtils.getDoubleArrayForString(sizeStr);
            if (sizeVals.length > 1)
                setUserXY(new Point(sizeVals[0], sizeVals[1]));
        }

        // Return this part
        return this;
    }
}
