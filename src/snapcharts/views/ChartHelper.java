package snapcharts.views;
import snap.util.ArrayUtils;
import snap.util.PropChange;
import snap.view.ViewUtils;
import snapcharts.model.*;

import java.util.*;

/**
 * A class to help customize ChartView for specific ChartType.
 */
public abstract class ChartHelper {

    // The ChartView
    protected ChartView  _chartView;

    // The DataSetList
    private DataSetList  _dataSetList;

    // The AxisTypes
    private AxisType[]  _axisTypes;

    // The AxisViews
    private Map<AxisType,AxisView> _axisViews = new HashMap<>();

    // The AxisViews array
    private AxisView[]  _axisViewsArray;

    // The X axis
    private AxisViewX  _axisX;

    // The Y axis
    private AxisViewY  _axisY;

    // The DataAreas
    private DataArea[]  _dataAreas;

    /**
     * Constructor.
     */
    protected ChartHelper(ChartView aChartView)
    {
        _chartView = aChartView;

        // Init to empty array so views get added in resetView() + resetAxisViews()
        _axisTypes = new AxisType[0];
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

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
    public DataSetList getDataSetListAll()
    {
        return getChart().getDataSetList();
    }

    /**
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()
    {
        // If already set, just return
        if (_dataSetList!=null) return _dataSetList;

        // Get DataSetList for DataSets that are enabled
        return _dataSetList = getDataSetListImpl();
    }

    /**
     * Returns a DataSetList of active datasets.
     */
    private DataSetList getDataSetListImpl()
    {
        // If all datasets are enabled, just return the existing DataSetList
        DataSetList dataSetList = getDataSetListAll();
        int activeCount = dataSetList.getDataSetCountEnabled();
        if (activeCount == dataSetList.getDataSetCount())
            return dataSetList;

        // Create new DataSetList and initialize with enabled sets
        DataSetList active = new DataSetList(getChart());
        List<DataSet> dsets = dataSetList.getDataSets();
        for (DataSet dset : dsets)
            if (dset.isEnabled())
                active.addDataSet(dset);
        active.setStartValue(dataSetList.getStartValue());
        return active;
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
        AxisView axis = _axisViews.get(anAxisType);
        if (axis!=null)
            return axis;

        // If not supported, return null
        if (!ArrayUtils.contains(getAxisTypes(), anAxisType))
            return null;

        // Create AxisView, init and add to map
        axis = createAxisView(anAxisType);
        axis._chartHelper = this;
        axis._chartView = _chartView;
        axis._dataView = _chartView.getDataView();
        _axisViews.put(anAxisType, axis);
        _axisViewsArray = null;
        return axis;
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
    public DataArea getDataAreaForFirstAxisY()
    {
        DataArea dataArea = getDataAreaForAxisTypeY(AxisType.Y);
        if (dataArea!=null)
            return dataArea;
        AxisType axisTypes[] = { AxisType.Y2, AxisType.Y3, AxisType.Y4 };
        for (AxisType axisType : axisTypes) {
            dataArea = getDataAreaForAxisTypeY(axisType);
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
        double min = axisView.getAxisMinForIntervalCalc();
        double max = axisView.getAxisMaxForIntervalCalc();
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
     * Called after a chart area is installed in chart view.
     */
    public void activate()
    {
        // Enable all datasets
        DataSetList dataSetList = getDataSetListAll();
        List<DataSet> dsets = dataSetList.getDataSets();
        for (DataSet dset : dsets)
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
        for (AxisView axisView : getAxisViews())
            ViewUtils.addChild(_chartView, axisView);
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

        // Handle DataSet/DataSetList change
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList) {
            if (_dataSetList!=null && _dataSetList!=getDataSetListAll())
                _dataSetList.clear();
            _dataSetList = null;
        }
    }

    /**
     * Creates a ChartHelper for given ChartView.
     */
    public static ChartHelper createChartHelper(ChartView aChartView)
    {
        ChartType chartType = aChartView.getChart().getType();
        switch (chartType) {
            case BAR: return new ChartHelperBar(aChartView);
            case PIE: return new ChartHelperPie(aChartView);
            case LINE: return new ChartHelperXY(aChartView, ChartType.LINE);
            case AREA: return new ChartHelperXY(aChartView, ChartType.AREA);
            case SCATTER: return new ChartHelperXY(aChartView, ChartType.SCATTER);
            case POLAR: return new ChartHelperPolar(aChartView);
            case CONTOUR: return new ChartHelperContour(aChartView);
            case BAR_3D: return new ChartHelperBar3D(aChartView);
            case PIE_3D: return new ChartHelperPie3D(aChartView);
            case LINE_3D: return new ChartHelperLine3D(aChartView);
            default: throw new RuntimeException("ChartHelper.createChartHelper: Unknown type: " + chartType);
        }
    }
}