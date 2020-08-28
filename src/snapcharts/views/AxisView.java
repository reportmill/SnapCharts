package snapcharts.views;
import snap.gfx.Color;
import snap.view.ParentView;
import snap.view.StringView;
import snapcharts.model.Chart;

import java.text.DecimalFormat;

/**
 * A View to display an axis.
 */
public class AxisView extends ParentView {

    // The DataView
    protected DataView _dataView;

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

    // A shared formatter
    private static DecimalFormat _fmt = new DecimalFormat("#.###");

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

    /**
     * Returns a formatted value.
     */
    protected String getLabelStringForValue(double aValue)
    {
        return getLabelStringForValueAndDelta(aValue, -1);
    }

    /**
     * Returns a formatted value.
     */
    protected String getLabelStringForValueAndDelta(double aValue, double aDelta)
    {
        // Handle case where delta is in the billions
        if (aDelta>=1000000000) { //&& aDelta/1000000000==((int)aDelta)/1000000000) {
            int val = (int)Math.round(aValue/1000000000);
            return val + "b";
        }

        // Handle case where delta is in the millions
        if (aDelta>=1000000) { //&& aDelta/1000000==((int)aDelta)/1000000) {
            int val = (int)Math.round(aValue/1000000);
            return val + "m";
        }

        // Handle case where delta is in the thousands
        //if (aDelta>=1000 && aDelta/1000==((int)aDelta)/1000) {
        //    int val = (int)Math.round(aLineVal/1000);
        //    return val + "k";
        //}

        // Handle case where delta is integer
        if (aDelta==(int)aDelta)
            return String.valueOf((int)aValue);

        return _fmt.format(aValue);
    }
}
