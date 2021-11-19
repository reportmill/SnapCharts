/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * A class to represent a ContourAxis. Subclasses axis because of the similarities.
 */
public class ContourAxis extends ChartPart {

    /**
     * Constructor.
     */
    public ContourAxis()
    {
        super();
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.Z; }
}
