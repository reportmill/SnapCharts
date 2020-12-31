package snapcharts.views;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.text.StringBox;
import snap.util.SnapUtils;
import snap.view.StringView;
import snap.view.ViewAnim;
import snap.view.ViewUtils;
import snapcharts.model.*;
import java.text.DecimalFormat;

/**
 * A View to display an axis.
 */
public abstract class AxisView<T extends Axis> extends ChartPartView<T> {

    // The ChartView
    protected ChartView  _chartView;

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

    // The tick labels as StringBoxes
    private StringBox[]  _tickLabels;

    // Constants for Properties
    public static final String AxisMin_Prop = "AxisMin";
    public static final String AxisMax_Prop = "AxisMax";

    // Constants for layout
    protected final int AXIS_MARGIN = 5;
    protected final int TITLE_TICKS_SPACING = 8;

    // Constants for painting
    protected static Font AXIS_LABEL_FONT = Font.Arial12.getBold().deriveFont(13);
    protected static Color AXIS_LABEL_TEXT_COLOR = Color.GRAY;
    protected static Color  AXIS_LABELS_COLOR = Color.DARKGRAY;

    // Grid Constants
    protected static Color GRID_COLOR = Color.get("#E6");
    protected static Stroke GRID_STROKE = Stroke.Stroke1;
    protected static Color TICK_LINE_COLOR = Color.GRAY;

    // Other constants
    public static double  UNSET_DOUBLE = Double.NEGATIVE_INFINITY;
    private static DecimalFormat TICKS_FORMAT = new DecimalFormat("#.###");

    /**
     * Constructor.
     */
    public AxisView()
    {
        super();

        // Set font
        setFont(Font.Arial12);

        // Create configure TitleView
        _titleView = new StringView();
        _titleView.setFont(AXIS_LABEL_FONT);
        _titleView.setTextFill(AXIS_LABEL_TEXT_COLOR);
        _titleView.setShrinkToFit(true);
        addChild(_titleView);
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

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
        return getIntervals().getMin();
    }

    /**
     * Returns the axis min.
     */
    protected double getAxisMinForIntervalCalc()
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
            min = dsetList.getMinForAxis(getAxisType());
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
        clearIntervals();

        // Repaint ChartView and clear ChartView.TargPoint
        getChartView().repaint();
        getChartView().setTargPoint(null);
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMax()
    {
        return getIntervals().getMax();
    }

    /**
     * Returns the axis max.
     */
    protected double getAxisMaxForIntervalCalc()
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

        // Handle BoundType.Data
        else {
            DataSetList dsetList = getDataSetList();
            max = dsetList.getMaxForAxis(getAxisType());
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
        clearIntervals();

        // Repaint ChartView and clear ChartView.TargPoint
        getChartView().repaint();
        getChartView().setTargPoint(null);
    }

    /**
     * Returns the grid line color.
     */
    public Color getGridColor()  { return GRID_COLOR; }

    /**
     * Returns the grid line stroke.
     */
    public Stroke getGridStroke()
    {
        //double dashes[] = getAxis().getGridDashArray();
        //Stroke stroke = dashes==null ? Stroke.Stroke1 : new Stroke(GRID_LINE_WIDTH, dashes, 0);
        return GRID_STROKE;
    }

    /**
     * Returns the axis length.
     */
    public double getAxisLen()
    {
        if (this instanceof AxisViewX)
            return getWidth();
        return getHeight();
    }

    /**
     * Returns the axis intervals for active datasets.
     */
    public Intervals getIntervals()
    {
        // If already set, just return
        if (isIntervalsValid())
            return _intervals;

        // Create, set and return
        _intervals = createIntervals();
        _tickLabels = null;
        return _intervals;
    }

    /**
     * Creates the axis intervals for active datasets.
     */
    protected Intervals createIntervals()
    {
        // Special case, bar
        if (getAxisType()==AxisType.X) {
            boolean isBar = getChart().getType().isBarType();
            DataSetList dsetList = getDataSetList();
            DataType dataType = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0).getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY) {
                int pointCount = dsetList.getPointCount();
                int maxX = isBar ? pointCount : pointCount - 1;
                return Intervals.getIntervalsSimple(0, maxX);
            }
        }

        // Normal case
        double min = getAxisMinForIntervalCalc();
        double max = getAxisMaxForIntervalCalc();
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
            boolean isBar = getChart().getType().isBarType();
            DataSetList dsetList = getDataSetList();
            DataType dataType = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0).getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY) {
                int pointCount = dsetList.getPointCount();
                double max = isBar ? pointCount : pointCount - 1;
                return _intervals.matchesMinMax(0, max);
            }
        }

        // Normal case: Return true if min/max are the same
        double min = getAxisMinForIntervalCalc();
        double max = getAxisMaxForIntervalCalc();
        return _intervals.matchesMinMax(min, max);
    }

    /**
     * Clears the intervals when axis len changes or data min/max change.
     */
    private void clearIntervals()
    {
        _intervals = null;
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    protected StringBox[] getTickLabels()
    {
        // Call this to make sure intervals are up to date
        getIntervals();

        // If already set, just return
        if (_tickLabels != null) return _tickLabels;

        // Get Intervals info
        Intervals intervals = getIntervals();
        int count = intervals.getCount();
        double delta = intervals.getDelta();

        // Create array
        StringBox[] sboxes = new StringBox[count];

        // Iterate over intervals
        for (int i = 0; i < count; i++) {
            double dataX = intervals.getInterval(i);
            String str = getLabelStringForValueAndDelta(dataX, delta);
            StringBox sbox = sboxes[i] = new StringBox(str);
            sbox.setFont(getFont());
            sbox.setColor(AXIS_LABELS_COLOR);
        }

        // Set/return
        _tickLabels = sboxes;
        layoutTickLabels();
        return _tickLabels;
    }

    /**
     * Layout TickLabels.
     */
    protected void layoutTickLabels()  { }

    /**
     * Paint axis.
     */
    protected void paintFront(Painter aPntr)
    {
        StringBox tickLabels[] = getTickLabels();
        for (StringBox sbox : tickLabels)
            sbox.paint(aPntr);
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public abstract double viewToData(double dispX);

    /**
     * Converts an value from dataset coords to view coords.
     */
    public double dataToView(double dataXY)
    {
        if (this instanceof AxisViewX)
            return dataToViewX(dataXY);
        return dataToViewY(dataXY);
    }

    /**
     * Converts an X value from dataset coords to view coords.
     */
    public double dataToViewX(double dataX)
    {
        double axisX = 0;
        double areaW = getWidth();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        return axisX + (dataX - dataMin)/(dataMax - dataMin)*areaW;
    }

    /**
     * Converts a Y value from dataset coords to view coords.
     */
    public double dataToViewY(double dataY)
    {
        double areaY = 0;
        double areaH = getHeight();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        return areaY + areaH - (dataY - dataMin)/(dataMax - dataMin)*areaH;
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxes()
    {
        setAxisMin(UNSET_DOUBLE);
        setAxisMax(UNSET_DOUBLE);
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxesAnimated()
    {
        // Get min/max for start/end
        double min0 = getAxisMin();
        double max0 = getAxisMax();
        setAxisMin(UNSET_DOUBLE);
        setAxisMax(UNSET_DOUBLE);
        double min1 = getAxisMin();
        double max1 = getAxisMax();
        setAxisMin(min0);
        setAxisMax(max0);

        // Get/configure animation
        ViewAnim anim = getAnimCleared(600);
        anim.setValue(AxisMin_Prop, min1);
        anim.setValue(AxisMax_Prop, max1);
        anim.setOnFinish(() -> ViewUtils.runLater(() -> resetAxes()));
        anim.play();
    }

    /**
     * Sets the axis Min/Max animated.
     */
    public void setAxisMinMax(double aMin, double aMax, boolean isAnimated)
    {
        if (isAnimated) {
            Point targPoint = getChartView().getTargPoint();
            ViewAnim anim = getAnimCleared(600);
            anim.setValue(AxisMin_Prop, aMin);
            anim.setValue(AxisMax_Prop, aMax);
            anim.setOnFinish(() -> getChartView().setTargPoint(targPoint));
            anim.play();
        }
        else {
            setAxisMin(aMin);
            setAxisMax(aMax);
        }
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

        return TICKS_FORMAT.format(aValue);
    }

    /**
     * Override to add support for this view properties.
     */
    @Override
    public Object getValue(String aPropName)
    {
        switch (aPropName) {
            case AxisMin_Prop: return getAxisMin();
            case AxisMax_Prop: return getAxisMax();
            default: return super.getValue(aPropName);
        }
    }

    /**
     * Override to add support for this view properties.
     */
    @Override
    public void setValue(String aPropName, Object aValue)
    {
        switch (aPropName) {
            case AxisMin_Prop: setAxisMin(SnapUtils.doubleValue(aValue)); break;
            case AxisMax_Prop: setAxisMax(SnapUtils.doubleValue(aValue)); break;
            default: super.setValue(aPropName, aValue);
        }
    }

    /**
     * Override to clear intervals.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        //if (isHorizontal())
            clearIntervals();
    }

    /**
     * Override to clear intervals.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        //if (isVertical())
            clearIntervals();
    }
}
