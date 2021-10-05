/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.geom.Insets;
import snap.geom.Side;
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

        // Override default property values
        _side = Side.BOTTOM;
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
     * Returns the prop default value for given key.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Padding
            case Padding_Prop: return DEFAULT_AXIS_X_PADDING;

            // Superclass properties
            default: return super.getPropDefault(aPropName);
        }
    }
}
