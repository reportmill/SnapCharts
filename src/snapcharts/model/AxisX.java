/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.props.PropSet;
import java.util.List;

/**
 * A class to represent a Chart Axis.
 */
public class AxisX extends Axis {

    // The categories
    private List <String>  _categories;

    // Constants for default values
    public static final Insets DEFAULT_AXIS_X_PADDING = new Insets(DEFAULT_AXIS_PAD, 0, DEFAULT_AXIS_PAD, 0);

    /**
     * Constructor.
     */
    public AxisX()
    {
        super();
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.X; }

    /**
     * Returns the categories.
     */
    public List<String> getCategories()  { return _categories; }

    /**
     * Sets the categories.
     */
    public void setCategories(List <String> theStrings)
    {
        _categories = theStrings;
    }

    /**
     * Override to configure props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Override super defaults: Padding
        aPropSet.getPropForName(Padding_Prop).setDefaultValue(DEFAULT_AXIS_X_PADDING);
    }
}
