/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;

/**
 * This enum represents different Axis types.
 */
public enum AxisType {

    // X Axis
    X,

    // Y Axis
    Y,

    // Second Y Axis
    Y2,

    // Third Y Axis
    Y3,

    // Fourth Y Axis
    Y4,

    // Z Axis
    Z;

    // Constant for XYZ Axes
    public static final AxisType[] XYZ_AXES = { X, Y, Z };

    /**
     * Returns whether AxisType is X.
     */
    public boolean isX()  { return this == X; }

    /**
     * Returns whether AxisType is Y.
     */
    public boolean isY()  { return this == Y; }

    /**
     * Returns whether AxisType is Y, Y2, Y3 or Y4.
     */
    public boolean isAnyY()  { return this == Y || this == Y2 || this == Y3 || this == Y4; }

    /**
     * Returns whether AxisType is Z type.
     */
    public boolean isZ()  { return this == Z; }
}
