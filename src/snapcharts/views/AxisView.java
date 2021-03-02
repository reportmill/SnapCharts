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
    protected double  _minOverride = UNSET_DOUBLE;

    // The Axis max override
    protected double  _maxOverride = UNSET_DOUBLE;

    // The intervals for axis
    private Intervals  _intervals;

    // The tick labels as StringBoxes
    private StringBox[]  _tickLabels;

    // A helper to do tick label formatting
    private AxisViewTickFormat _tickFormat;

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

        // Create TickFormatter
        _tickFormat = new AxisViewTickFormat(this);
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
        // If already set, just return
        if (aValue==_minOverride) return;

        // Set value, firePropChange, clear intervals
        firePropChange(AxisMin_Prop, _minOverride, _minOverride = aValue);
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
        // If already set, just return
        if (aValue==_maxOverride) return;

        // Set value, clear intervals
        firePropChange(AxisMax_Prop, _maxOverride, _maxOverride = aValue);
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
        Intervals ivals = _chartHelper.createIntervals(this);
        return _intervals = ivals;
    }

    /**
     * Clears the intervals when axis len changes or data min/max change.
     */
    public void clearIntervals()
    {
        _intervals = null;
        _tickLabels = null;
        repaint();

        // Register for check to see if tick format has changed
        _tickFormat.checkForFormatChange();
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    protected StringBox[] getTickLabels()
    {
        // If already set, just return
        if (_tickLabels != null) return _tickLabels;

        // Special case, bar
        if (getAxisType()==AxisType.X) {
            boolean isBar = getChart().getType().isBarType();
            DataSetList dsetList = getDataSetList();
            DataSet dset = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0) : null;
            DataType dataType = dset != null ? dset.getDataType() : null;
            if (dataType==DataType.CY) { //isBar || dataType==DataType.IY || dataType==DataType.CY
                int pointCount = dsetList.getPointCount();
                StringBox[] sboxes = new StringBox[pointCount];
                for (int i = 0; i < pointCount; i++) {
                    String str = dset.getC(i);
                    StringBox sbox = sboxes[i] = new StringBox(str);
                    sbox.setFont(getFont());
                    sbox.setColor(AXIS_LABELS_COLOR);
                }
                _tickLabels = sboxes;
                layoutTickLabels();
                return _tickLabels;
            }
        }

        // Get Intervals info
        Intervals intervals = getIntervals();
        int count = intervals.getCount();

        // Create array
        StringBox[] sboxes = new StringBox[count];

        // Iterate over intervals
        for (int i = 0; i < count; i++) {
            double dataX = intervals.getInterval(i);
            String str = _tickFormat.format(dataX); //getLabelStringForValueAndDelta(dataX, delta);
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
     * Returns the max label width.
     */
    protected double getTickLabelsMaxWidth()
    {
        return _tickFormat.getLongSampleStringWidth();
    }

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
     * Converts an value from dataset coords to view coords.
     */
    public double dataToView(double dataXY)
    {
        return _chartHelper.dataToView(this, dataXY);
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(double dispXY)
    {
        return _chartHelper.viewToData(this, dispXY);
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
}