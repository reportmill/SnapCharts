package snapcharts.view;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.util.ArrayUtils;
import snap.util.PropChange;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.ViewUtils;
import snapcharts.model.*;
import snapcharts.viewx.*;

import java.util.*;

/**
 * A class to help customize ChartView for specific ChartType.
 */
public abstract class ChartHelper {

    // The ChartView
    protected ChartView  _chartView;

    // The DataView
    protected DataView  _dataView;

    // The DataSetList
    private DataSetList  _dataSetList;

    // The AxisTypes
    private AxisType[]  _axisTypes;

    // The AxisViews
    private Map<AxisType,AxisView>  _axisViews = new HashMap<>();

    // The AxisViews array
    private AxisView[]  _axisViewsArray;

    // The X axis
    private AxisViewX  _axisX;

    // The Y axis
    private AxisViewY  _axisY;

    // The DataAreas
    private DataArea[]  _dataAreas;

    // A helper class to handle Pan/Zoom
    private ChartHelperPanZoom _panZoomer;

    /**
     * Constructor.
     */
    protected ChartHelper(ChartView aChartView)
    {
        _chartView = aChartView;

        // Init to empty array so views get added in resetView() + resetAxisViews()
        _axisTypes = new AxisType[0];

        // Create PanZoomer
        _panZoomer = new ChartHelperPanZoom(this);
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()
    {
        if (_dataView != null) return _dataView;
        return _dataView = _chartView.getDataView();
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chartView.getChart(); }

    /**
     * Returns the ChartType.
     */
    public abstract ChartType getChartType();

    /**
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()
    {
        // If already set, just return
        if (_dataSetList!=null) return _dataSetList;

        // Get DataSetList for DataSets that are enabled
        return _dataSetList = getChart().getDataSetList();
    }

    /**
     * Returns the layout for the chart.
     */
    public ChartViewLayout createLayout()
    {
        return new ChartViewLayout(_chartView);
    }

    /**
     * Returns the AxisTypes.
     */
    public AxisType[] getAxisTypes()
    {
        if (_axisTypes!=null) return _axisTypes;
        return _axisTypes = getAxisTypesImpl();
    }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return getDataSetList().getAxisTypes();
    }

    /**
     * Returns whether given axis type exists in chart.
     */
    public boolean isAxisType(AxisType anAxisType)
    {
        return ArrayUtils.contains(getAxisTypes(), anAxisType);
    }

    /**
     * Returns the X AxisView.
     */
    public AxisViewX getAxisViewX()
    {
        if (_axisX != null) return _axisX;
        return _axisX = (AxisViewX) getAxisView(AxisType.X);
    }

    /**
     * Returns the Y Axis.
     */
    public AxisViewY getAxisViewY()
    {
        if (_axisY != null) return _axisY;
        return _axisY = (AxisViewY) getAxisView(AxisType.Y);
    }

    /**
     * Returns the AxisView for given type.
     */
    public AxisView getAxisView(AxisType anAxisType)
    {
        // If already set, just return
        AxisView axisView = _axisViews.get(anAxisType);
        if (axisView!=null)
            return axisView;

        // If not supported, return null
        if (!ArrayUtils.contains(getAxisTypes(), anAxisType))
            return null;

        // Create AxisView, init and add to map
        axisView = createAxisView(anAxisType);
        axisView._chartHelper = this;
        axisView._chartView = _chartView;
        axisView._dataView = _chartView.getDataView();
        axisView.addPropChangeListener(pc -> axisViewDidChange(pc));
        _axisViews.put(anAxisType, axisView);
        _axisViewsArray = null;
        return axisView;
    }

    /**
     * Returns the AxisViews.
     */
    public AxisView[] getAxisViews()
    {
        // If already set, just return
        if (_axisViewsArray != null) return _axisViewsArray;

        // Iterate over types to get cached AxisViews
        AxisType[] types = getAxisTypes();
        AxisView[] views = new AxisView[types.length];
        for (int i=0; i< types.length; i++)
            views[i] = getAxisView(types[i]);
        return _axisViewsArray = views;
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        if (anAxisType == AxisType.X)
            return new AxisViewX();
        if (anAxisType.isAnyY())
            return new AxisViewY(anAxisType);
        throw new RuntimeException("ChartHelper.createAxisView: Unknown type: " + anAxisType);
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxes()
    {
        for (AxisView axisView : getAxisViews())
            axisView.resetAxes();
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxesAnimated()
    {
        // If axes don't need reset, do anim instead
        boolean isAxisOverrideSet = false;
        for (AxisView axisView : getAxisViews())
            isAxisOverrideSet |= axisView.isAxisMinOverrideSet() || axisView.isAxisMaxOverrideSet();
        if (!isAxisOverrideSet) {
            getChartView().animate();
            return;
        }

        for (AxisView axisView : getAxisViews())
            axisView.resetAxesAnimated();
    }

    /**
     * Returns the DataAreas.
     */
    public DataArea[] getDataAreas()
    {
        if (_dataAreas!=null) return _dataAreas;
        return _dataAreas = createDataAreas();
    }

    /**
     * Creates the DataAreas.
     */
    protected abstract DataArea[] createDataAreas();

    /**
     * Returns the first DataArea for axis type.
     */
    public DataArea getDataAreaForDataSet(DataSet aDataSet)
    {
        DataArea[] dataAreas = getDataAreas();
        for (DataArea dataArea : dataAreas)
            if (dataArea.getDataSet() == aDataSet)
                return dataArea;

        // Some charts have only one shared DataArea (Bar, Pie)
        if (dataAreas.length > 0)
            return dataAreas[0];
        return null;
    }

    /**
     * Returns the first DataArea for axis type.
     */
    public DataArea getDataAreaForFirstAxisY()
    {
        AxisType[] axisTypes = getAxisTypes();
        for (AxisType axisType : axisTypes) {
            if (!axisType.isAnyY()) continue;
            DataArea dataArea = getDataAreaForAxisTypeY(axisType);
            if (dataArea!=null)
                return dataArea;
        }
        return null;
    }

    /**
     * Returns the first DataArea for axis type.
     */
    public DataArea getDataAreaForAxisTypeY(AxisType anAxisType)
    {
        for (DataArea dataArea : getDataAreas())
            if (dataArea.getAxisTypeY() == anAxisType)
                return dataArea;
        return null;
    }

    /**
     * Creates the axis intervals for active datasets.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // Get info
        AxisType axisType = axisView.getAxisType();
        Axis axis = axisView.getAxis();

        // Special case, bar
        if (axisType == AxisType.X) {
            boolean isBar = getChartType().isBarType();
            DataSetList dsetList = getDataSetList();
            DataSet dset = dsetList.getDataSetCount()>0 ? dsetList.getDataSet(0) : null;
            DataType dataType = dset != null ? dset.getDataType() : null;
            if (isBar || dataType==DataType.IY || dataType==DataType.CY)
                return createIntervalsForBarAxis();
        }

        // Get axis min, max, display length
        double min = getAxisMinForIntervalCalc(axisView);
        double max = getAxisMaxForIntervalCalc(axisView);
        double axisLen = axisView.getAxisLen();
        double divLen = axisType == AxisType.X ? 40 : 30;

        // Get whether interval ends should be adjusted
        boolean minFixed = axisView._minOverride!=AxisView.UNSET_DOUBLE || axis.getMinBound() != AxisBound.AUTO;
        boolean maxFixed = axisView._maxOverride!=AxisView.UNSET_DOUBLE || axis.getMaxBound() != AxisBound.AUTO;

        // Handle Log
        if (axis.isLog())
            return Intervals.getIntervalsLog(min, max, false, maxFixed);

        // Return intervals
        return Intervals.getIntervalsForMinMaxLen(min, max, axisLen, divLen, minFixed, maxFixed);
    }

    /**
     * Creates the axis intervals for discrete X axis, like bar or indexed value axis.
     */
    protected Intervals createIntervalsForBarAxis()
    {
        boolean isBar = getChartType().isBarType();
        DataSetList dsetList = getDataSetList();
        int pointCount = dsetList.getPointCount();
        int maxX = isBar ? pointCount : pointCount - 1;
        return Intervals.getIntervalsSimple(0, maxX);
    }

    /**
     * Returns the axis min.
     */
    public double getAxisMinForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView._minOverride != AxisView.UNSET_DOUBLE)
            return axisView._minOverride;

        // Get Min based on Axis.MinBound and Axis.ZeroRequired
        Axis axis = axisView.getAxis();
        double min = axis.getMinValueForBoundAndZeroRequired();
        return min;
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMaxForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView._maxOverride != AxisView.UNSET_DOUBLE)
            return axisView._maxOverride;

        // Get Max based on Axis.MaxBound and Axis.ZeroRequired
        Axis axis = axisView.getAxis();
        double max = axis.getMaxValueForBoundAndZeroRequired();
        return max;
    }

    /**
     * Converts from data to view for given axis type.
     */
    public double dataToView(AxisType anAxisType, double dataXY)
    {
        AxisView axisView = getAxisView(anAxisType);
        return dataToView(axisView, dataXY);
    }

    /**
     * Converts from data to view for given axis type.
     */
    public double dataToView(AxisView axisView, double dataXY)
    {
        // Get AxisView and axis min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (axisView.isLog()) {
            dataXY = ChartViewUtils.log10(dataXY);
            dataMin = ChartViewUtils.log10(dataMin);
            dataMax = ChartViewUtils.log10(dataMax);
        }

        // Handle horizontal (X) axis
        boolean isHor = axisView.getAxisType() == AxisType.X;
        if (isHor) {
            double areaW = axisView.getWidth();
            double dispX = (dataXY - dataMin) / (dataMax - dataMin) * areaW;
            return dispX;
        }

        // Handle vertical (Y) axis
        double areaH = axisView.getHeight();
        double dispY = areaH - (dataXY - dataMin) / (dataMax - dataMin) * areaH;
        return dispY;
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(AxisType anAxisType, double dispXY)
    {
        AxisView axisView = getAxisView(anAxisType);
        return viewToData(axisView, dispXY);
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(AxisView axisView, double dispXY)
    {
        // Get AxisView and axis min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle log
        if (axisView.isLog()) {
            dataMin = ChartViewUtils.log10(dataMin);
            dataMax = ChartViewUtils.log10(dataMax);
        }

        // Handle horizontal (X) axis
        boolean isHor = axisView.getAxisType() == AxisType.X;
        double dataXY;
        if (isHor) {
            double areaW = axisView.getWidth();
            dataXY = dataMin + dispXY / areaW * (dataMax - dataMin);
        }

        // Handle vertical (Y) axis
        else {
            double areaW = axisView.getHeight();
            dataXY = dataMax - dispXY / areaW * (dataMax - dataMin);
        }

        // Handle log
        if (axisView.isLog())
            dataXY = ChartViewUtils.invLog10(dataXY);

        // Return data val
        return dataXY;
    }

    /**
     * Returns the data point for given View + X/Y.
     */
    public DataPoint getDataPointForViewXY(View aView, double aX, double aY)
    {
        // Local vars for data point closest to x/y
        DataPoint dataPoint = null;
        double dist = Float.MAX_VALUE;

        // Get View XY in DataView coords
        DataView dataView = getDataView();
        Point dispXY = dataView.isAncestor(aView) ? dataView.parentToLocal(aX, aY, aView) : new Point(aX, aY);
        double dispX = dispXY.x;
        double dispY = dispXY.y;

        // If point out of bounds, return null
        if (!dataView.contains(dispX, dispY))
            return null;

        // Iterate over data areas to find closest DataPoint
        DataArea[] dataAreas = getDataAreas();
        for (DataArea dataArea : dataAreas) {
            DataPoint dpnt = dataArea.getDataPointForLocalXY(dispX, dispY);
            if (dpnt != null) {
                Point dspXY = dataArea.getLocalXYForDataPoint(dpnt);
                double dst = Point.getDistance(dispX, dispY, dspXY.x, dspXY.y);
                if (dst < dist) {
                    dataPoint = dpnt;
                    dist = dst;
                }
            }
        }

        // Return DataPoint
        return dataPoint;
    }

    /**
     * Returns the given data point as X/Y in given view coords.
     */
    public Point getViewXYForDataPoint(View aView, DataPoint aDP)
    {
        // Get DataArea for DataPoint
        DataSet dataSet = aDP.getDataSet();
        DataArea dataArea = getDataAreaForDataSet(dataSet);
        if (dataArea == null) return null;

        // Get DataArea X/Y for DataPoint and return point converted to given view coords
        Point dispXY = dataArea.getLocalXYForDataPoint(aDP);
        DataView dataView = getDataView();
        if (dataView.isAncestor(aView))
            dispXY = dataView.localToParent(dispXY.x, dispXY.y, aView);
        return dispXY;
    }

    /**
     * Called after a chart area is installed in chart view.
     */
    public void activate()
    {
        // Enable all datasets
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        for (DataSet dset : dataSets)
            dset.setDisabled(false);
    }

    /**
     * Called before a chart area is removed from a chart view.
     */
    public void deactivate()  { }

    /**
     * Called when chart is reloaded.
     */
    public void resetView()
    {
        // If AxisTypes have changed, resetAxisViews
        if (!Objects.equals(getAxisTypes(), getDataSetList().getAxisTypes()))
            resetAxisViews();

        // Reset Axes
        for (AxisView axisView : getAxisViews())
            axisView.resetView();

        // Reset DataAreas
        for (DataArea dataArea : getDataAreas())
            if (dataArea.isDataSetEnabled())
                dataArea.resetView();
    }

    /**
     * Resets the axis views.
     */
    protected void resetAxisViews()
    {
        // Remove old axis views
        removeAxisViews();

        // Reset cached values
        _axisTypes = null;
        _axisViewsArray = null;
        _axisX = null;
        _axisY = null;

        // Add new axis views
        for (AxisView axisView : getAxisViews()) {
            ViewUtils.addChild(_chartView, axisView);
            axisView.clearIntervals();
        }
    }

    /**
     * Removes AxisViews.
     */
    protected void removeAxisViews()
    {
        // Remove old axis views
        AxisView[] oldAxisViews = _chartView.getChildrenForClass(AxisView.class);
        for (AxisView axisView : oldAxisViews)
            ViewUtils.removeChild(_chartView, axisView);
    }

    /**
     * Returns whether view is in ZoomSelectMode.
     */
    public boolean isZoomSelectMode()  { return _panZoomer.isZoomSelectMode(); }

    /**
     * Sets whether view is in ZoomSelectMode.
     */
    public void setZoomSelectMode(boolean aValue)
    {
        if (!getChartType().isXYType()) return;
        _panZoomer.setZoomSelectMode(aValue);
    }

    /**
     * Called to process event for hook.
     */
    protected void processEventForChartPartView(ChartPartView aView, ViewEvent anEvent)
    {
        if (getChartType().isXYType())
            _panZoomer.processEventForChartPartView(aView, anEvent);
    }

    /**
     * Override to forward to PanZoom.
     */
    protected void paintAboveForChartPartView(ChartPartView aView, Painter aPntr)
    {
        if (aView instanceof DataView)
            _panZoomer.paintAbove(aPntr);
    }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        if (getChartType().isXYType())
            _panZoomer.scaleAxesMinMaxForFactor(aScale, isAnimated);
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Forward to DataAreas
        for (DataArea dataArea : getDataAreas())
            dataArea.chartPartDidChange(aPC);

        // Forward to AxisViews
        for (AxisView axisView : getAxisViews())
            axisView.chartPartDidChange(aPC);
    }

    /**
     * Called when DataView changes size.
     */
    protected void dataViewSizeDidChange()
    {
        // Forward to DataAreas
        for (DataArea dataArea : getDataAreas())
            dataArea.dataViewDidChangeSize();
    }

    /**
     * Called when AxisView changes properties.
     */
    protected void axisViewDidChange(PropChange aPC)
    {
        // Forward to DataAreas
        for (DataArea dataArea : getDataAreas())
            dataArea.axisViewDidChange(aPC);
    }

    /**
     * Creates a ChartHelper for given ChartView.
     */
    public static ChartHelper createChartHelper(ChartView aChartView)
    {
        ChartType chartType = aChartView.getChartType();
        switch (chartType) {
            case BAR: return new BarChartHelper(aChartView);
            case PIE: return new PieChartHelper(aChartView);
            case SCATTER: return new XYChartHelper(aChartView, ChartType.SCATTER);
            case CONTOUR: return new ContourChartHelper(aChartView);
            case POLAR: return new PolarChartHelper(aChartView);
            case POLAR_CONTOUR: return new PolarContourChartHelper(aChartView);
            case BAR_3D: return new Bar3DChartHelper(aChartView);
            case PIE_3D: return new Pie3DChartHelper(aChartView);
            case LINE_3D: return new Line3DChartHelper(aChartView);
            default: throw new RuntimeException("ChartHelper.createChartHelper: Unknown type: " + chartType);
        }
    }
}