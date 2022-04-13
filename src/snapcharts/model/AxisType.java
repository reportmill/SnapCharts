/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * This enum represents different Axis types.
 */
public enum AxisType {

    X,
    Y,
    Y2,
    Y3,
    Y4,
    Z;

    // Constant for XYZ Axes
    public static final AxisType[] XYZ_AXES = { X, Y, Z };

    /**
     * Returns whether AxisType is Y type.
     */
    public boolean isX()  { return this == X; }

    /**
     * Returns whether AxisType is Y type.
     */
    public boolean isAnyY()  { return this == Y || this == Y2 || this == Y3 || this == Y4; }
}
