/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;

/**
 * A Trace subclass for Pie3D chart properties.
 */
public class Pie3DTrace extends Trace {

    /**
     * Constructor.
     */
    public Pie3DTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Pie3D; }
}
