/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts.traces;
import snapcharts.charts.TraceType;

/**
 * A Trace subclass for Contour3D chart properties.
 */
public class PolarContourTrace extends ContourTrace {

    /**
     * Constructor.
     */
    public PolarContourTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.PolarContour; }
}
