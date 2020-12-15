package snapcharts.views;
import snap.view.ViewUtils;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartType;

/**
 * A class to display data (via DataArea).
 */
public class DataView<T extends ChartPart> extends ChartPartView<T> {

    // The ChartView
    private ChartView  _chartView;

    // The DataView
    private DataArea _dataArea;

    // The X AxisView
    private AxisViewX  _axisX;

    // The Y AxisView
    private AxisViewY  _axisY;

    /**
     * Constructor.
     */
    public DataView(ChartView aChartView)
    {
        _chartView = aChartView;

        // Create/add axes
        _axisX = new AxisViewX();
        _axisY = new AxisViewY();

        // Create/set DataView
        setDataView(DataArea.createDataAreaForType(ChartType.LINE));
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return null; }

    /**
     * Returns the ChartView.
     */
    @Override
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the DataArea.
     */
    public DataArea getDataArea()  { return _dataArea; }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataArea aDataArea)
    {
        // Remove old
        if (_dataArea !=null) {
            _dataArea.deactivate();
            removeChild(_dataArea);
        }

        // Set/add new
        _dataArea = aDataArea;
        addChild(_dataArea);

        // Update Axes
        _dataArea.setDataView(this);
        _axisY._dataView = _axisX._dataView = aDataArea;

        _dataArea.activate();
    }

    /**
     * Returns the X Axis View.
     */
    public AxisViewX getAxisX()  { return _axisX; }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisY()  { return _axisY; }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Do normal version
        super.resetView();

        // Get info
        Chart chart = _chartView.getChart();
        ChartType chartType = chart.getType();

        // Update DataArea: Get DataArea for type and set in this DataView
        if (_dataArea ==null || chartType!= getDataArea().getChartType()) {
            DataArea dataArea = DataArea.createDataAreaForType(chartType);
            setDataView(dataArea);
        }

        // Reset X Axis
        _axisX.resetView();

        // Reset Y Axis
        _axisY.resetView();

        // Reset DataView
        _dataArea.reactivate();

        // Trigger animate (after delay so size is set for first time)
        _dataArea.setReveal(0);
        ViewUtils.runLater(() -> _dataArea.animate());
        _axisY.repaint();
        _axisX.repaint();
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        _dataArea.setSize(viewW, viewH);
    }
}
