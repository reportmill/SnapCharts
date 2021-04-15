package snapcharts.model;

/**
 * A class to represent a ColorBar. Subclasses axis because of the similarities.
 */
public class ColorBar extends Axis {

    /**
     * Constructor.
     */
    public ColorBar(Chart aChart)
    {
        super();
        _chart = aChart;
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.Z; }
}
