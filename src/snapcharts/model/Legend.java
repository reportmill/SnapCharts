/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.util.*;

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

    // Property constants
    public static final String ShowLegend_Prop = "ShowLegend";
    public static final String Position_Prop = "Position";
    public static final String Inside_Prop = "Inside";

    // Constants for property defaults
    private static Pos DEFAULT_LEGEND_ALIGN = Pos.TOP_LEFT;
    private static Insets DEFAULT_LEGEND_MARGIN = new Insets(5, 5, 5, 5);

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
            case Position_Prop: return Pos.CENTER_RIGHT;

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

        // Unarchive ShowLegend, Position
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

        // Return this part
        return this;
    }
}
