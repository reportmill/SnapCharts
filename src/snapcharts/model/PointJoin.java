/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * This enum class describes the method by which DataSet points are joined.
 */
public enum PointJoin {

    Line,

    Spline,

    HV,

    VH,

    HVH;

    /**
     * Returns the reverse of this PointJoin.
     */
    public PointJoin getReverse()
    {
        if (this == HV)
            return VH;
        if (this == VH)
            return HV;
        return this;
    }
}
