/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.charts.traces;
import snapcharts.charts.TraceType;

/**
 * A Trace subclass for Bar3D chart properties.
 */
public class Bar3DTrace extends BarTrace {

    /**
     * Constructor.
     */
    public Bar3DTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Bar3D; }

}
