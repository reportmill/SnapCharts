package snapcharts.views;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.text.StringBox;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.StringView;
import snap.view.ViewAnim;
import snap.view.ViewUtils;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import java.text.DecimalFormat;

/**
 * A View to display an axis.
 */
public abstract class AxisView<T extends Axis> extends ChartPartView<T> {

    // The ChartHelper
    protected ChartHelper  _chartHelper;

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
     * Returns the dataset.
     */
    public DataSetList getDataSetList()
    {
        return _chartHelper.getDataSetList();
    }

    /**
     * Returns the axis.
     */
    public abstract Axis getAxis();

    /**
     * Returns the axis type.
     */
    public AxisType getAxisType()  { return getAxis().getType(); }

    /**
     * Returns the Axis MinMax.
     */
    public MinMax getAxisMinMax()
    {
        // Get min val for Axis MinBound
        Axis axis = getAxis();
        AxisBound minBound = axis.getMinBound();
        double min = axis.getMinValue();
        if (minBound == AxisBound.AUTO)
            min = getAxisMin();
        else if (minBound == AxisBound.DATA)
            min = getDataSetList().getMinForAxis(axis.getType());

        // Get max val for Axis MaxBound
        AxisBound maxBound = axis.getMaxBound();
        double max = axis.getMaxValue();
        if (maxBound == AxisBound.AUTO)
            max = getAxisMax();
        else if (maxBound == AxisBound.DATA)
            max = getDataSetList().getMaxForAxis(axis.getType());

        // Return min/max
        return new MinMax(min, max);
    }

    /**
     * Returns the axis min.
     */
    public double getAxisMin()
    {
        return getIntervals().getMin();
    }

    /**
     * Sets the axis min (override).
     */
    public void setAxisMin(double aValue)
    {
        if (aValue==_minOverride) return;
        _minOverride = aValue;
        System.out.println("Setting to: " + (aValue==UNSET_DOUBLE ? "unset" : aValue));
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
     * Sets the axis min (override).
     */
    public void setAxisMax(double aValue)
    {
        if (aValue==_maxOverride) return;
        _maxOverride = aValue;
        _maxOverride = UNSET_DOUBLE;
        clearIntervals();

        // Repaint ChartView and clear ChartView.TargPoint
        getChartView().repaint();
        getChartView().setTargPoint(null);
    }

    /**
     * Returns the axis min.
     */
    protected double getAxisMinForIntervalCalc()
    {
        // If explicitly set, just return
        if (_minOverride!=UNSET_DOUBLE) return _minOverride;

        // Get Axis, Axis.MinBound
        Axis axis = getAxis();
        AxisBound minBound = axis.getMinBound();

        // Get min
        double min;
        if (minBound == AxisBound.VALUE)
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
     * Returns the axis max.
     */
    protected double getAxisMaxForIntervalCalc()
    {
        // If explicitly set, just return
        if (_maxOverride!=UNSET_DOUBLE) return _maxOverride;

        // Get Axis, Axis.MaxBound
        Axis axis = getAxis();
        AxisBound maxBound = axis.getMaxBound();

        // Get max
        double max;
        if (maxBound == AxisBound.VALUE)
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
     * Returns whether axis is logarithmic.
     */
    public boolean isLog()
    {
        Axis axis = getAxis();
        return axis.isLog();
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
        if (_intervals!=null) return _intervals;

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
            boolean isBar = getChart().getType().isBarType();
            DataSetList dsetList = getDataSetList();
            DataType dataType = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0).getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY) {
                int pointCount = dsetList.getPointCount();
                int maxX = isBar ? pointCount : pointCount - 1;
                return Intervals.getIntervalsSimple(0, maxX);
            }
        }

        // Get axis min, max, display length
        double min = getAxisMinForIntervalCalc();
        double max = getAxisMaxForIntervalCalc();
        double axisLen = getAxisLen();
        double divLen = getAxisType()==AxisType.X ? 40 : 30;

        // Get whether interval ends should be adjusted
        boolean minFixed = _minOverride!=UNSET_DOUBLE || getAxis().getMinBound() != AxisBound.AUTO;
        boolean maxFixed = _maxOverride!=UNSET_DOUBLE || getAxis().getMaxBound() != AxisBound.AUTO;

        // Handle Log
        if (isLog())
            return Intervals.getIntervalsLog(min, max, false, maxFixed);

        // Return intervals
        return Intervals.getIntervalsForMinMaxLen(min, max, axisLen, divLen, minFixed, maxFixed);
    }

    /**
     * Clears the intervals when axis len changes or data min/max change.
     */
    public void clearIntervals()
    {
        _intervals = null;
        _tickLabels = null;
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
     * Converts a value from axis data coords to display coords (allows for log axis support).
     */
    public double axisDataToView(double axisDataXY)
    {
        // Basic convert from data to display
        boolean isHor = isHorizontal();
        double areaW = isHor ? getWidth() : getHeight();
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double dispVal = isHor ? (axisDataXY - dataMin) / (dataMax - dataMin) * areaW :
                areaW - (axisDataXY - dataMin) / (dataMax - dataMin) * areaW;

        // Return display val
        return dispVal;
    }

    /**
     * Converts an value from dataset coords to view coords.
     */
    public double dataToView(double dataXY)
    {
        // Get data min/max (data coords)
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (isLog()) {
            dataXY = log10(dataXY);
            dataMin = log10(dataMin);
            dataMax = log10(dataMax);
        }

        // Get display len (min is zero)
        boolean isHor = isHorizontal();
        double areaW = isHor ? getWidth() : getHeight();

        // Convert data to display
        double dispXY = isHor ? (dataXY - dataMin) / (dataMax - dataMin) * areaW :
                areaW - (dataXY - dataMin) / (dataMax - dataMin) * areaW;

        // Return display val
        return dispXY;
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(double dispXY)
    {
        // Get data min/max (data coords)
        Intervals intervals = getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (isLog()) {
            dataMin = log10(dataMin);
            dataMax = log10(dataMax);
        }

        // Get display len (min is zero)
        boolean isHor = isHorizontal();
        double areaW = isHor ? getWidth() : getHeight();

        // Convert display to data
        double dataXY = isHor ? dataMin + dispXY / areaW * (dataMax - dataMin) :
                dataMax - dispXY / areaW * (dataMax - dataMin);

        // Handle log
        if (isLog())
            dataXY = invLog10(dataXY);

        // Return data val
        return dataXY;
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
     * Returns a formatted value.
     */
    protected String getLabelStringForValueAndDelta(double aValue, double aDelta)
    {
        // Handle Log axis: Only show text for  values that are a factor of 10 (1[0]* or 0.[0]*1)
        if (isLog()) {
            String str = TICKS_FORMAT.format(aValue);
            if (str.matches("1[0]*|0\\.[0]*1"))
                return str;
            return "";
        }

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

        // Check/reset intervals
        if (!isIntervalsValid())
            clearIntervals();
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
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList || src instanceof Axis) {
            clearIntervals();
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
        clearIntervals();
    }

    /**
     * Returns the log of given value.
     */
    private double log10(double aValue)
    {
        if (aValue<=0)
            return 0;
        return Math.log10(aValue);
    }

    /**
     * Returns the inverse of log10.
     */
    public double invLog10(double aValue)
    {
        return Math.pow(10, aValue);
    }
}
