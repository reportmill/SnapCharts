/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snapcharts.data.DataPoint;

/**
 * A class to represent a data point.
 */
public class TracePoint extends DataPoint {

    // The Trace this point belongs to
    protected Trace  _trace;

    /**
     * Constructor.
     */
    public TracePoint(Trace aTrace, int anIndex)
    {
        super(aTrace.getProcessedData(), anIndex);
        _trace = aTrace;
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _trace; }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check basics
        if (anObj == this) return true;
        TracePoint other = anObj instanceof TracePoint ? (TracePoint) anObj : null;
        if (other == null) return false;

        // Check Trace, Index
        if (other._trace != _trace) return false;
        if (other._index != _index) return false;
        return true;
    }
}