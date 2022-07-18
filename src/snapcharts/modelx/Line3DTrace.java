/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;

/**
 * A Trace subclass for Line3D chart properties.
 */
public class Line3DTrace extends Trace {

    /**
     * Constructor.
     */
    public Line3DTrace()
    {
        super();
    }

    /**
     * Returns Type.
     */
    @Override
    public TraceType getType()  { return TraceType.Line3D; }
}
