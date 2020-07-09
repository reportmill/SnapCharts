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

    // The length of the vertical tick lines drawn from the X axis down twards it's labels and title
    private double  _tickLength = 10;

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

    /**
     * Returns the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public double getTickLength()  { return _tickLength; }

    /**
     * Sets the length of the vertical tick lines drawn from the X axis down twards it's labels and title.
     */
    public void setTickLength(double aValue)  { _tickLength = aValue; }

    /**
     * Returns the label string at given index.
     */
    public String getLabel(int anIndex)
    {
        // If categories exist, return the category string at index
        if (_categories!=null && anIndex<_categories.size())
            return _categories.get(anIndex);

        // Otherwise, return string for start value and index
        DataSet dset = getChart().getDataSet();
        int val = dset.getSeriesStart() + anIndex;
        return String.valueOf(val);
    }
}
