package snapcharts.model;
import snap.geom.Side;
import java.util.List;

/**
 * A class to represent a Chart Axis.
 */
public class AxisX extends Axis {

    // The categories
    private List <String>  _categories;

    /**
     * Constructor.
     */
    public AxisX()
    {
        super();

        // Override default property values
        _side = Side.BOTTOM;
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return AxisType.X; }

    /**
     * Returns the categories.
     */
    public List<String> getCategories()  { return _categories; }

    /**
     * Sets the categories.
     */
    public void setCategories(List <String> theStrings)
    {
        _categories = theStrings;
    }
}
