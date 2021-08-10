/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * A class to represent a ColorBar. Subclasses axis because of the similarities.
 */
public class ColorBar extends Axis {

    /**
     * Constructor.
     */
    public ColorBar()
    {
        super();
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.Z; }
}
