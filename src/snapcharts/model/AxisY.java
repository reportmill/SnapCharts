package snapcharts.model;
import snap.geom.Side;

/**
 * A class to represent a Chart Axis.
 */
public class AxisY extends Axis {

    // The axis type
    private AxisType  _axisType;

    // The Side this axis
    private Side  _side;

    // Constants for properties
    public static final String Side_Prop = "Side";


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

    /**
     * Returns the side this axis is shown on.
     */
    public Side getSide()
    {
        return _side!=null ? _side : getSideDefault();
    }

    /**
     * Sets the side this axis is shown on.
     */
    public void setSide(Side aSide)
    {
        if (aSide==getSide()) return;
        if (aSide!=null && !aSide.isHorizontal())
            throw new IllegalArgumentException("AxisY.setSide: Can't set AxisY side to " + aSide);
        firePropChange(Side_Prop, _side, _side = aSide);
    }

    /**
     * Returns the default side for this axis.
     */
    public Side getSideDefault()
    {
        switch (getType()) {
            case Y: return Side.LEFT;
            case Y2: return Side.RIGHT;
            case Y3: return Side.LEFT;
            case Y4: return Side.RIGHT;
            default: throw new RuntimeException("AxisY.getSide: Unknown AxisType: " + getType());
        }
    }
}
