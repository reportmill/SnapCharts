/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.geom.Size;
import snap.gfx.*;
import snap.util.PropChange;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // The view that holds text for markers configured with ShowTextInAxis
    protected ChildView  _markersBox;

    // The view that holds TickLabels
    protected ChildView  _tickLabelBox;

    // The Axis min override
    protected double  _minOverride = UNSET_DOUBLE;

    // The Axis max override
    protected double  _maxOverride = UNSET_DOUBLE;

    // The intervals for axis
    private Intervals  _intervals;

    // The MaxTickLabelRotatedSize
    private Size  _maxTickLabelRotatedSize;

    // The tick labels as StringBoxes
    private TickLabel[]  _tickLabels;

    // The marker labels as StringBoxes
    private TickLabel[]  _markerLabels;

    // A helper to do tick label formatting
    private TickLabelFormat  _tickFormat;

    // Constants for Properties
    public static final String AxisMin_Prop = "AxisMin";
    public static final String AxisMax_Prop = "AxisMax";

    // Constants for layout
    protected final int AXIS_MARGIN = 5;
    protected final int TITLE_TICKS_SPACING = 8;

    // Grid Constants
    public static Color TICK_LINE_COLOR = Color.GRAY;

    // Constants for Min/MaxOverride to indicate not set
    public static double  UNSET_DOUBLE = Double.NEGATIVE_INFINITY;

    // Constants for minimum interval div length X and Y
    private static int MIN_DIV_LEN_X = 40;
    private static int MIN_DIV_LEN_Y = 30;

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

        // Create/configure MarkersBox
        _markersBox = new MarkersBox();
        addChild(_markersBox);

        // Create TickLabelBox
        _tickLabelBox = new TickLabelBox();
        addChild(_tickLabelBox);

        // Enable events
        enableEvents(MouseEvents);
        enableEvents(Scroll);
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
        if (aValue == _minOverride) return;

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
        if (aValue == _maxOverride) return;

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
     * Returns whether axis min value is fixed (as opposed to being able to adjust for intervals).
     */
    public boolean isAxisMinFixed()
    {
        return _minOverride != UNSET_DOUBLE || getAxis().getMinBound() != AxisBound.AUTO;
    }

    /**
     * Returns whether axis max value is fixed (as opposed to being able to adjust for intervals).
     */
    public boolean isAxisMaxFixed()
    {
        return _maxOverride != AxisView.UNSET_DOUBLE || getAxis().getMaxBound() != AxisBound.AUTO;
    }

    /**
     * Returns whether given axis is category axis.
     */
    public boolean isCategoryAxis()
    {
        // If not X axis, return false
        AxisType axisType = getAxisType();
        if (axisType != AxisType.X)
            return false;

        // If ChartType Bar, return true
        boolean isBar = getChartType().isBarType();
        if (isBar)
            return true;

        // If DataSet is IY or CY, return true
        DataSetList dataSetList = getDataSetList();
        DataSet dataSet = dataSetList.getDataSetCount() > 0 ? dataSetList.getDataSet(0) : null;
        DataType dataType = dataSet != null ? dataSet.getDataType() : null;
        if (dataType == DataType.IY || dataType == DataType.CY)
            return true;

        // Return false
        return false;
    }

    /**
     * Returns the axis intervals for active datasets.
     *
     * This is tricky because of chicken-and-egg between Intervals and DivLen:
     *
     *     - Intervals depends on DivLen
     *     - DivLen depends on MaxTickLabelRotatedSize
     *     - MaxTickLabelRotatedSize depends on largest label determined by Intervals
     *
     *     Even worse, when/if X axis gets taller, it shrinks Y axis and visa-versa.
     */
    public Intervals getIntervals()
    {
        // If already set, just return
        if (_intervals != null) return _intervals;

        // Get intervals for Min_Div_Len
        _maxTickLabelRotatedSize = new Size();
        double divLen = getDivLen();
        _intervals = _chartHelper.createIntervals(this);

        // Get new DivLen for new intervals (must clear MaxTickLabelRotatedSize for this)
        _maxTickLabelRotatedSize = null;
        double divLen2 = getDivLen();

        // Iterate until DivLen stabilizes (only let it go smaller once to avoid possible back-and-forth)
        boolean wentSmallerOnce = false;
        while (divLen2 > divLen || (divLen2 < divLen && !wentSmallerOnce)) {
            if (divLen2 < divLen)
                wentSmallerOnce = true;
            _intervals = _chartHelper.createIntervals(this);
            divLen = divLen2;
            _maxTickLabelRotatedSize = null;
            divLen2 = getDivLen();
        }

        // Return intervals
        return _intervals;
    }

    /**
     * Returns the axis length.
     */
    protected double getAxisLen()
    {
        if (this instanceof AxisViewX)
            return getWidth();
        return getHeight();
    }

    /**
     * Returns the recommended grid spacing size.
     * This is based on MaxTickLabelRotatedSize, but constrained to a min size.
     */
    protected double getDivLen()
    {
        // Get max label size, add reasonable buffer space, constrain to X/Y minimum and return
        Size maxTicksSize = getMaxTickLabelRotatedSize();
        double divLen;
        if (this instanceof AxisViewX)
            divLen = Math.max(maxTicksSize.width + 16, MIN_DIV_LEN_X);
        else divLen = Math.max(maxTicksSize.height + 16, MIN_DIV_LEN_Y);
        return divLen;
    }

    /**
     * Returns the tick labels maximum rotated size.
     */
    protected Size getMaxTickLabelRotatedSize()
    {
        // If already set, just return
        if (_maxTickLabelRotatedSize != null) return _maxTickLabelRotatedSize;

        // Get max label size, set and return
        Size maxLabelSize = TickLabelBox.getMaxTickLabelRotatedSize(this);
        return _maxTickLabelRotatedSize = maxLabelSize;
    }

    /**
     * Returns the TickLabelFormat.
     */
    protected TickLabelFormat getTickLabelFormat()
    {
        if (_tickFormat != null) return _tickFormat;
        Intervals intervals = getIntervals();
        return _tickFormat = new TickLabelFormat(this, intervals);
    }

    /**
     * Clears the intervals when axis len changes or data min/max change.
     */
    public void clearIntervals()
    {
        // If already clear, just return
        if (_intervals == null) return;

        // Cache last intervals and clear
        _intervals = null;
        _maxTickLabelRotatedSize = null;
        _tickFormat = null;

        // Remove/clear TickLabels
        if (_tickLabels != null) {
            _tickLabelBox.removeChildren();
            _tickLabels = null;
        }

        // Remove/clear MarkerLabels
        if (_markerLabels != null) {
            _markersBox.removeChildren();
            _markerLabels = null;
        }

        // Make sure to register for relayout (TickLabelBox could be empty)
        _tickLabelBox.relayout();
        _tickLabelBox.relayoutParent();
    }

    /**
     * Clears the Markers when axis markers change.
     */
    public void clearMarkers()
    {
        // Remove/clear MarkerLabels
        if (_markerLabels != null) {
            _markersBox.removeChildren();
            _markerLabels = null;
        }
    }

    /**
     * Returns the array of tick label StringBoxes.
     */
    protected TickLabel[] getTickLabels()
    {
        // If already set, just return
        if (_tickLabels != null) return _tickLabels;

        // Create TickLabels for intervals
        _tickLabels = TickLabelBox.createTickLabels(this);

        // Add TickLabels to TickLabelBox
        for (TickLabel tickLabel : _tickLabels)
            _tickLabelBox.addChild(tickLabel);

        // Return
        return _tickLabels;
    }

    /**
     * Returns the array of Marker TickLabel.
     */
    protected TickLabel[] getMarkerLabels()
    {
        // If already set, just return
        if (_markerLabels != null) return _markerLabels;

        // Create MarkerLabels
        _markerLabels = createMarkerLabels();

        // Add MarkerLabels to MarkersBox
        for (StringView markerView : _markerLabels)
            _markersBox.addChild(markerView);

        // Return
        return _markerLabels;
    }

    /**
     * Returns the array of Marker TickLabel.
     */
    protected TickLabel[] createMarkerLabels()
    {
        // Get Markers
        Chart chart = getChart();
        Marker[] markers = chart.getMarkers();
        AxisType axisType = getAxisType();
        if (markers.length == 0 || axisType != AxisType.X)
            return new TickLabel[0];

        // Get count for Markers for Axis
        int count = 0;
        for (Marker marker : markers)
            if (marker.isShowTextInAxis() && marker.getCoordSpaceX().getAxisType() == axisType)
                count++;
        if (count == 0)
            return new TickLabel[0];

        // Get Markers for axis
        Predicate<Marker> filter = m -> m.isShowTextInAxis() && m.getCoordSpaceX().getAxisType() == axisType;
        Marker[] axisMarkers = Stream.of(markers).filter(filter).collect(Collectors.toList()).toArray(new Marker[0]);

        // Get Marker paint properties
        Font markerFont = getFont();
        Paint markerFill = getAxis().getTextFill();

        // Get MarkerLabels
        TickLabel[] markerLabels = new TickLabel[count];
        for (int i = 0; i < count; i++) {
            Marker marker = axisMarkers[i];
            TickLabel markerLabel = markerLabels[i] = new TickLabel(this, marker.getX());
            markerLabel.setText(marker.getText());
            markerLabel.setFont(markerFont);
            markerLabel.setTextFill(markerFill);
        }

        // Create/return array of TickLabels
        return markerLabels;
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
     * Override to paint axis line and ticks.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        paintAxisLineAndTicks(aPntr);
    }

    /**
     * Override to paint axis line and ticks.
     */
    protected void paintAxisLineAndTicks(Painter aPntr)
    {
        TickPainter tickPainter = new TickPainter(getChartHelper());
        tickPainter.paintAxisLineAndTicks(aPntr, this);
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

        // Configure TickLabelBox.Visible
        _tickLabelBox.setVisible(axis.isShowTickLabels());

        // If no MarkerLabels, make MarkersBox not visible
        TickLabel[] markerLabels = getMarkerLabels();
        _markersBox.setVisible(markerLabels.length > 0);
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
        String propName = aPC.getPropName();
        if (src instanceof DataSet || src instanceof DataSetList || src instanceof Axis) {
            clearIntervals();
        }

        // Handle Marker changes
        if (src instanceof Marker || propName == Chart.Markers_Rel)
            clearMarkers();
    }

    /**
     * Override to clear intervals.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        if (this instanceof AxisViewX)
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
        if (this instanceof AxisViewY)
            clearIntervals();
    }

    /**
     * A container view to hold text for Markers configured with ShowTextInAxis.
     */
    protected static class MarkersBox extends ChildView {

        /**
         * Override to return something.
         */
        @Override
        protected double getPrefWidthImpl(double aH)
        {
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