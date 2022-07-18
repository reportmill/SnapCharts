/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;

/**
 * A Trace subclass for Line chart properties.
 */
public class ScatterTrace extends Trace {

    /**
     * Constructor.
     */
    public ScatterTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Scatter; }
}
