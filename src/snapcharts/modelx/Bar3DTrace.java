/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snapcharts.model.TraceType;

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
