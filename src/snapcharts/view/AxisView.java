package snapcharts.view;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import java.util.ArrayList;
import java.util.List;

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

    // The view that holds TickLabels
    protected ChildView  _tickLabelBox;

    // The Axis min override
    protected double  _minOverride = UNSET_DOUBLE;

    // The Axis max override
    protected double  _maxOverride = UNSET_DOUBLE;

    // The intervals for axis
    private Intervals  _intervals;

    // The tick labels as StringBoxes
    private TickLabel[]  _tickLabels;

    // A helper to do tick label formatting
    private TickLabelFormat _tickFormat;

    // Constants for Properties
    public static final String AxisMin_Prop = "AxisMin";
    public static final String AxisMax_Prop = "AxisMax";

    // Constants for layout
    protected final int AXIS_MARGIN = 5;
    protected final int TITLE_TICKS_SPACING = 8;

    // Grid Constants
    public static Color TICK_LINE_COLOR = Color.GRAY;

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
        _titleView.setShrinkToFit(true);
        addChild(_titleView);

        // Create TickLabelBox
        _tickLabelBox = new TickLabelBox();
        addChild(_tickLabelBox);

        // Enable events
        enableEvents(MouseEvents);
        enableEvents(Scroll);

        // Create TickFormatter
        _tickFormat = new TickLabelFormat(this);
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
     * Returns whether AxisMin override value is set.
     */
    public boolean isAxisMinOverrideSet()  { return _minOverride != UNSET_DOUBLE; }

    /**
     * Returns AxisMin override value.
     */
    public double getAxisMinOverride()  { return _minOverride; }

    /**
     * Returns whether AxisMax override value is set.
     */
    public boolean isAxisMaxOverrideSet()  { return _maxOverride != UNSET_DOUBLE; }

    /**
     * Returns AxisMax override value.
     */
    public double getAxisMaxOverride()  { return _maxOverride; }

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

        // Remove/clear TickLabels
        if (_tickLabels != null) {
            _tickLabelBox.removeChildren();
            _tickLabels = null;
        }

        // Register for check to see if tick format has changed
        _tickFormat.checkForFormatChange();
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    protected TickLabel[] getTickLabels()
    {
        // If already set, just return
        if (_tickLabels != null) return _tickLabels;

        // Special case, bar
        if (isBarAxis())
            _tickLabels = createTickLabelsForBarAxis();
        else _tickLabels = createTickLabels();

        // Add TickLabels to TickLabelBox
        for (TickLabel tickLabel : _tickLabels)
            _tickLabelBox.addChild(tickLabel);

        // Return
        return _tickLabels;
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    protected TickLabel[] createTickLabels()
    {
        // Get Intervals info
        Intervals intervals = getIntervals();
        int intervalCount = intervals.getCount();
        double delta = intervals.getDelta();

        // Get TickLabel attributes
        Axis axis = getAxis();
        Font tickLabelFont = getFont();
        Paint tickTextFill = axis.getTextFill();

        // Create list
        List<TickLabel> tickLabels = new ArrayList<>(intervalCount);

        // Iterate over intervals
        for (int i = 0; i < intervalCount; i++) {
            double dataX = intervals.getInterval(i);

            // If edge div too close to next div, skip
            if (i == 0 || i + 1 == intervalCount) {
                double nextX = intervals.getInterval(i == 0 ? 1 : intervalCount - 2);
                double delta2 = i == 0 ? (nextX - dataX) : (dataX - nextX);
                if (delta2 < delta * .99) {  // Was .67
                    continue;
                }
            }

            // Create/config/add TickLabel
            TickLabel tickLabel = new TickLabel(this, dataX);
            String str = _tickFormat.format(dataX);
            if (str.length() == 0)
                continue;
            tickLabel.setText(str);
            tickLabel.setFont(tickLabelFont);
            tickLabel.setTextFill(tickTextFill);
            tickLabels.add(tickLabel);
        }

        // Create/return array of TickLabels
        return tickLabels.toArray(new TickLabel[0]);
    }

    /**
     * Returns whether axis is a bar axis (labels represent bin indexs, not axis values).
     */
    private boolean isBarAxis()
    {
        if (getAxisType() != AxisType.X)
            return false;
        if (getChartType().isBarType())
            return true;

        // Also treat IY and CY Data sets as Bar axis types
        DataSetList dsetList = getDataSetList();
        DataSet dset = dsetList.getDataSetCount() > 0 ? dsetList.getDataSet(0) : null;
        DataType dataType = dset != null ? dset.getDataType() : null;
        return dataType == DataType.IY || dataType == DataType.CY;
    }

    /**
     * Creates Bar Axis Labels.
     */
    private TickLabel[] createTickLabelsForBarAxis()
    {
        // Get DataSet and pointCount
        DataSetList dsetList = getDataSetList();
        DataSet dset = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0) : null;
        int pointCount = dsetList.getPointCount();
        TickLabel[] tickLabels = new TickLabel[pointCount];

        // Get TickLabel attributes
        Axis axis = getAxis();
        Font tickLabelFont = getFont();
        Paint tickTextFill = axis.getTextFill();

        // Iterate over points and create/set TickLabel
        for (int i = 0; i < pointCount; i++) {
            TickLabel tickLabel = tickLabels[i] = new TickLabel(this, i + .5);
            String str = dset.getString(i); // was getC(i)
            tickLabel.setText(str);
            tickLabel.setFont(tickLabelFont);
            tickLabel.setTextFill(tickTextFill);
        }

        // Return TickLabels
        return tickLabels;
    }

    /**
     * Returns the max label width.
     */
    protected double getTickLabelsMaxWidth()
    {
        return _tickFormat.getLongSampleStringWidth();
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
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {
            case AxisMin_Prop: return getAxisMin();
            case AxisMax_Prop: return getAxisMax();
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to add support for this view properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {
            case AxisMin_Prop: setAxisMin(SnapUtils.doubleValue(aValue)); break;
            case AxisMax_Prop: setAxisMax(SnapUtils.doubleValue(aValue)); break;
            default: super.setPropValue(aPropName, aValue);
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
        _titleView.setFont(axis.getFont());
        _titleView.setTextFill(axis.getTextFill());

        // If no title, make TitleView not visible so it takes no space (for AxisViewY, TitleView is in WrapView)
        boolean titleVisible = title != null && title.length() > 0;
        _titleView.setVisible(titleVisible);
        if (_titleView.getParent() != this)
            _titleView.getParent().setVisible(titleVisible);
    }

    /**
     * Override to forward to ChartHelper.
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        _chartHelper.processEventForChartPartView(this, anEvent);
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
     * A container view to hold TickLabels.
     */
    protected static class TickLabelBox extends ChildView {

        /**
         * Override to return TickLabels.MaxWidth for AxisViewY.
         */
        @Override
        protected double getPrefWidthImpl(double aH)
        {
            ParentView parent = getParent();
            if (parent instanceof AxisViewY) {
                return ((AxisViewY) parent).getTickLabelsMaxWidth();
            }
            return 200;
        }

        /**
         * Override to return text height for AxisViewX.
         */
        @Override
        protected double getPrefHeightImpl(double aW)
        {
            ParentView parent = getParent();
            if (parent instanceof AxisViewX) {
                Font font = getFont();
                int ascent = (int) Math.ceil(font.getAscent());
                int descent = (int) Math.ceil(font.getDescent());
                return ascent + descent;
            }
            return 200;
        }
    }
}