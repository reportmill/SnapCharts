/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts;

/**
 * This enum class describes the method by which Trace points are joined.
 */
public enum PointJoin {

    Line,

    Spline,

    StepHV,

    StepVH,

    StepHVH,

    Y0Between;

    /**
     * Returns the reverse of this PointJoin.
     */
    public PointJoin getReverse()
    {
        if (this == StepHV)
            return StepVH;
        if (this == StepVH)
            return StepHV;
        return this;
    }
}
