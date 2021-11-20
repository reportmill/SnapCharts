/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * A class to represent properties to render data for a specific ChartType.
 */
public class TraceStyle extends ChartPart {

    /**
     * Constructor.
     */
    public TraceStyle()
    {
        super();
    }

    /**
     * Override to prevent client code from using border instead of line props.
     */
    @Override
    public boolean isBorderSupported()  { return false; }
}
