package snapcharts.model;
import java.util.List;

/**
 * A class to represent a Chart Axis.
 */
public class AxisX extends Axis {

    // The categories
    private List <String>  _categories;

    // The x/y offset of labels
    private double  _labelsX, _labelsY = 8;

    /**
     * Constructor.
     */
    public AxisX()
    {
        super();
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

    /**
     * Returns the x of labels.
     */
    public double getLabelsX()  { return _labelsX; }

    /**
     * Sets the x of labels.
     */
    public void setLabelsX(double aValue)  { _labelsX = aValue; }

    /**
     * Returns the y offset of labels.
     */
    public double getLabelsY()  { return _labelsY; }

    /**
     * Returns the y offset of labels.
     */
    public void setLabelsY(double aValue)  { _labelsY = aValue; }
}
