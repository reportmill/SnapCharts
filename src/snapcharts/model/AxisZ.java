package snapcharts.model;

/**
 * A class to represent a Chart Axis.
 */
public class AxisZ extends Axis {

    /**
     * Constructor.
     */
    public AxisZ(Chart aChart)
    {
        super();
        _chart = aChart;
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.Z; }
}
