package snapcharts.views;
import snap.gfx.Color;
import snap.view.StringView;
import snapcharts.model.*;
import java.text.DecimalFormat;

/**
 * A View to display an axis.
 */
public abstract class AxisView<T extends Axis> extends ChartPartView<T> {

    // The DataView
    protected DataView  _dataView;

    // The Title view
    protected StringView  _titleView;

    // The Axis min override
    private double  _minOverride = UNSET_DOUBLE;

    // The Axis max override
    private double  _maxOverride = UNSET_DOUBLE;

    // The intervals for axis
    private Intervals  _intervals;

    // The grid line color
    protected Color  _gridLineColor = DataView.GRID_LINES_COLOR;

    // The grid line
    protected double  _gridLineDashArray[];

    // Constants
    protected static Color  AXIS_LABELS_COLOR = Color.DARKGRAY;
    public static double  UNSET_DOUBLE = Double.NEGATIVE_INFINITY;

    // A shared formatter
    private static DecimalFormat  _fmt = new DecimalFormat("#.###");

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getAxis(); }

    /**
     * Returns the axis.
     */
    public abstract Axis getAxis();

    /**
     * Returns the axis type.
     */
    public AxisType getAxisType()  { return getAxis().getType(); }

    /**
     * Returns the axis min.
     */
    public double getAxisMin()
    {
        // If explicitly set, just return
        if (_minOverride!=UNSET_DOUBLE) return _minOverride;

        // Get Axis
        Axis axis = getAxis();
        Axis.AxisBoundType boundType = axis.getMinBoundType();

        // Get min
        double min;
        if (boundType== Axis.AxisBoundType.VALUE)
            min = axis.getMinValue();
        else {
            DataSetList dsetList = getDataSetList();
            min = dsetList.getMinForAxis(axis.getType());
        }

        // If ZeroRequired and min greater than zero, reset min
        if (axis.isZeroRequired() && min>0)
            min = 0;

        // Return min
        return min;
    }

    /**
     * Sets the axis min (override).
     */
    public void setAxisMin(double aValue)
    {
        if (aValue==_minOverride) return;
        _minOverride = aValue;
        _intervals = null;
        getChartView().repaint();
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMax()
    {
        // If explicitly set, just return
        if (_maxOverride!=UNSET_DOUBLE) return _maxOverride;

        // Get Axis
        Axis axis = getAxis();
        Axis.AxisBoundType boundType = axis.getMaxBoundType();

        // Get max
        double max;
        if (boundType== Axis.AxisBoundType.VALUE)
            max = axis.getMaxValue();
        else {
            DataSetList dsetList = getDataSetList();
            max = dsetList.getMaxForAxis(axis.getType());
        }

        // If ZeroRequired and max less than zero, reset max
        if (axis.isZeroRequired() && max<0)
            max = 0;

        // Return max
        return max;
    }

    /**
     * Sets the axis min (override).
     */
    public void setAxisMax(double aValue)
    {
        if (aValue==_maxOverride) return;
        _maxOverride = aValue;
        _intervals = null;
        getChartView().repaint();
    }

    /**
     * Returns the axis length.
     */
    public double getAxisLen()
    {
        switch (getAxisType()) {
            case X: return _dataView.getWidth() - _dataView.getInsetsAll().getWidth();
            case Y: return _dataView.getHeight() - _dataView.getInsetsAll().getHeight();
            default: throw new RuntimeException("AxisView.getAxisLen: Unknown axis type: "+ getAxisType());
        }
    }

    /**
     * Returns the axis intervals for active datasets.
     */
    public Intervals getIntervals()
    {
        // If already set, just return
        if (isIntervalsValid()) return _intervals;

        // Create, set and return
        _intervals = createIntervals();
        return _intervals;
    }

    /**
     * Creates the axis intervals for active datasets.
     */
    protected Intervals createIntervals()
    {
        // Special case, bar
        if (getAxisType()==AxisType.X) {
            boolean isBar = _dataView.isChartTypeBar();
            DataSetList dsetList = getDataSetList();
            DataType dataType = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0).getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY) {
                int pointCount = dsetList.getPointCount();
                int maxX = isBar ? pointCount : pointCount - 1;
                return Intervals.getIntervalsSimple(0, maxX);
            }
        }

        // Normal case
        double min = getAxisMin();
        double max = getAxisMax();
        double axisLen = getAxisLen();
        double divLen = getAxisType()==AxisType.X ? 40 : 30;
        boolean minFixed = _minOverride!=UNSET_DOUBLE;
        boolean maxFixed = _maxOverride!=UNSET_DOUBLE;
        return Intervals.getIntervalsForMinMaxLen(min, max, axisLen, divLen, minFixed, maxFixed);
    }

    /**
     * Returns true if intervals are okay.
     */
    private boolean isIntervalsValid()
    {
        // If intervals not set, return false
        if (_intervals==null)
            return false;

        // Special case, bar
        if (getAxisType()==AxisType.X) {
            boolean isBar = _dataView.isChartTypeBar();
            DataSetList dsetList = getDataSetList();
            DataType dataType = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0).getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY) {
                int pointCount = dsetList.getPointCount();
                double max = isBar ? pointCount : pointCount - 1;
                return _intervals.matchesMinMaxLen(0, max, max);
            }
        }

        // Normal case: Return true if min, max and AxisLen are the same
        double min = getAxisMin();
        double max = getAxisMax();
        double len = getAxisLen();
        return _intervals.matchesMinMaxLen(min, max, len);
    }

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
     * Converts an X value from dataset coords to view coords.
     */
    public double dataToViewX(double dataX)
    {
        double dispX = _dataView.dataToViewX(dataX);
        double dx = _dataView.getX() - getX();
        return dispX - dx;
    }

    /**
     * Converts a Y value from dataset coords to view coords.
     */
    public double dataToViewY(double dataY)
    {
        double dispY = _dataView.dataToViewY(dataY);
        double dy = _dataView.getY() - getY();
        return dispY - dy;
    }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Get Axis
        Axis axis = getAxis();

        // Reset title
        String title = axis.getTitle();
        _titleView.setText(title);
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
