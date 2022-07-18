/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;

/**
 * A Trace subclass for Pie chart properties.
 */
public class PieTrace extends Trace {

    /**
     * Constructor.
     */
    public PieTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Pie; }
}
