package snapcharts.views;
import snap.util.ArrayUtils;
import snapcharts.model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to help customize ChartView for specific ChartType.
 */
public abstract class ChartHelper {

    // The ChartView
    protected ChartView  _chartView;

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

        // Create AxisViews
        for (AxisType axisType : getAxisTypes())
            getAxisView(axisType);
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
    protected abstract AxisType[] getAxisTypesImpl();

    /**
     * Returns the X Axis.
     */
    public AxisViewX getAxisX()
    {
        if (_axisX != null) return _axisX;
        return _axisX = (AxisViewX) getAxisView(AxisType.X);
    }

    /**
     * Returns the Y Axis.
     */
    public AxisViewY getAxisY()
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
        if (_axisViewsArray != null) return _axisViewsArray;
        return _axisViewsArray = _axisViews.values().toArray(new AxisView[0]);
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        if (anAxisType == AxisType.X)
            return new AxisViewX();
        if (anAxisType == AxisType.Y)
            return new AxisViewY();
        throw new RuntimeException("ChartHelper.createAxisView: Unknown type: " + anAxisType);
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxes()
    {
        getAxisX().resetAxes();
        getAxisY().resetAxes();
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxesAnimated()
    {
        getAxisX().resetAxesAnimated();
        getAxisY().resetAxesAnimated();
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
     * Called after a chart area is installed in chart view.
     */
    public void activate()
    {
        // Enable all datasets
        DataSetList dataSetList = _chartView.getDataSetList();
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
    public void reactivate()  { }

    /**
     * Call to clear any cached data.
     */
    public void clearCache()
    {
        for (DataArea dataArea : getDataAreas())
            dataArea.clearCache();
    }

    /**
     * Creates a ChartHelper for given ChartView.
     */
    public static ChartHelper createChartHelper(ChartView aChartView)
    {
        ChartType chartType = aChartView.getChart().getType();
        switch (chartType) {
            case BAR: return new ChartHelperBar(aChartView);
            case BAR_3D: return new ChartHelperBar3D(aChartView);
            case PIE: return new ChartHelperPie(aChartView);
            case LINE: return new ChartHelperXY(aChartView, ChartType.LINE);
            case AREA: return new ChartHelperXY(aChartView, ChartType.AREA);
            case SCATTER: return new ChartHelperXY(aChartView, ChartType.SCATTER);
            default: throw new RuntimeException("ChartHelper.createChartHelper: Unknown type: " + chartType);
        }
    }
}