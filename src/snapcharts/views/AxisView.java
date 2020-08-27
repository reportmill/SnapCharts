package snapcharts.views;
import snap.gfx.Color;
import snap.view.ParentView;
import snap.view.StringView;
import snapcharts.model.Chart;

/**
 * A View to display an axis.
 */
public class AxisView extends ParentView {

    // The DataView
    protected DataView  _dataView;

    // The Title view
    protected StringView _titleView;

    // The grid line color
    protected Color _gridLineColor = GRID_LINES_COLOR;

    // The grid line
    protected double  _gridLineDashArray[];

    // Constants
    protected static Color   AXIS_LABELS_COLOR = Color.GRAY;
    protected static Color   GRID_LINES_COLOR = Color.get("#E6");
    protected static Color           AXIS_LINES_COLOR = Color.LIGHTGRAY;

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _dataView.getChart(); }

    /**
     * Returns the grid line color.
     */
    public Color getGridLineColor()  { return _gridLineColor; }

    /**
     * Returns the grid line color.
     */
    public void setGridLineColor(Color aColor)  { _gridLineColor = aColor; }

    /**
     * Returns the grid line dash array.
     */
    public double[] getGridLineDashArray()  { return _gridLineDashArray; }

    /**
     * Returns the grid line dash array.
     */
    public void setGridLineDashArray(double theVals[])  { _gridLineDashArray = theVals; }

    /**
     * Converts a point from dataset coords to view coords.
     */
    public double dataToViewX(double dataX)
    {
        double dispX = _dataView.dataToViewX(dataX);
        double dx = _dataView.getX() - getX();
        return dispX - dx;
    }

}
