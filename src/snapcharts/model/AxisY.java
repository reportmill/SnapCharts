package snapcharts.model;

/**
 * A class to represent a Chart Axis.
 */
public class AxisY extends Axis {

    // The axis type
    private AxisType  _axisType;

    /**
     * Constructor.
     */
    public AxisY(Chart aChart, AxisType anAxisType)
    {
        super();
        _chart = aChart;
        _axisType = anAxisType;
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return _axisType; }
}
