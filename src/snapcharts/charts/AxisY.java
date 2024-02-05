/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;
import snap.geom.Insets;
import snap.props.PropSet;
import snap.util.Convert;

/**
 * A class to represent a Chart Axis.
 */
public class AxisY extends Axis {

    // The axis type
    private AxisType  _axisType;

    // Whether to show legend graphic in axis
    private boolean  _showLegendGraphic;

    // Constants for properties
    public static final String ShowLegendGraphic_Prop = "ShowLegendGraphic";

    // Constants for default values
    private static final boolean DEFAULT_SHOW_LEGEND_GRAPHIC = false;
    public static final Insets DEFAULT_AXIS_Y_PADDING = new Insets(0, DEFAULT_AXIS_PAD, 0, DEFAULT_AXIS_PAD);

    /**
     * Constructor.
     */
    public AxisY(AxisType anAxisType)
    {
        super();
        _axisType = anAxisType;

        // Override defaults: Padding, Side
        _padding = DEFAULT_AXIS_Y_PADDING;
        _side = getSideDefault();
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return _axisType; }

    /**
     * Returns whether to show LegendEntry graphic in axis.
     */
    public boolean isShowLegendGraphic()  { return _showLegendGraphic; }

    /**
     * Sets whether to show LegendEntry graphic in axis.
     */
    public void setShowLegendGraphic(boolean aValue)
    {
        if (aValue == isShowLegendGraphic()) return;
        firePropChange(ShowLegendGraphic_Prop, _showLegendGraphic, _showLegendGraphic = aValue);
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: Padding
        aPropSet.getPropForName(Padding_Prop).setDefaultValue(DEFAULT_AXIS_Y_PADDING);

        // ShowLegendGraphic
        aPropSet.addPropNamed(ShowLegendGraphic_Prop, boolean.class, DEFAULT_SHOW_LEGEND_GRAPHIC);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // ShowLegendGraphic
            case ShowLegendGraphic_Prop: return isShowLegendGraphic();

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

            // ShowLegendGraphic
            case ShowLegendGraphic_Prop: setShowLegendGraphic(Convert.boolValue(aValue)); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }
}