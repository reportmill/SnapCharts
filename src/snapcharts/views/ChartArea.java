package snapcharts.views;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;

/**
 * A class to display essential chart content: DataView and AxisViews.
 */
public class ChartArea extends ChartPartView {

    // The ChartView
    private ChartView  _chartView;

    // The DataView
    private DataView  _dataView;

    // The X AxisView
    private AxisViewX  _axisX;

    // The Y AxisView
    private AxisViewY  _axisY;

    /**
     * Constructor.
     */
    public ChartArea(ChartView aChartView)
    {
        setGrowWidth(true);
        setGrowHeight(true);

        _chartView = aChartView;

        // Create/add axes
        _axisX = new AxisViewX();
        _axisY = new AxisViewY();
        addChild(_axisY);
        addChild(_axisX);

        // Create/set DataView
        setDataView(DataView.createDataViewForType(ChartType.LINE));
    }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()  { return _dataView; }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDataView)
    {
        // Remove old
        if (_dataView !=null)
            removeChild(_dataView);

        // Set/add new
        _dataView = aDataView;
        addChild(_dataView, 1);

        // Update Axes
        _dataView.setChartView(_chartView);
        _axisY._dataView = _axisX._dataView = aDataView;

        _dataView.activate();
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
        // Get info
        Chart chart = _chartView.getChart();
        ChartType chartType = chart.getType();

        // Update DataView: Get DataView for type and set in this ChartArea
        if (_dataView ==null || chartType!= getDataView().getChartType()) {
            DataView dataView = DataView.createDataViewForType(chartType);
            setDataView(dataView);
        }

        // Reset X Axis
        _axisX.resetView();

        // Reset Y Axis
        _axisY.resetView();

        // Reset DataView
        _dataView.reactivate();

        // Trigger animate
        _dataView.animate();
        _axisY.repaint();
        _axisX.repaint();
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double prefW = _dataView.getPrefWidth();
        if (_axisY.isVisible())
            prefW += _axisY.getPrefWidth();
        return prefW;
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        double prefH = _dataView.getPrefHeight();
        if (_axisX.isVisible())
            prefH += _axisX.getPrefHeight();
        return prefH;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Set DataView height first, since height can effect yaxis label width
        double viewW = getWidth();
        double viewH = getHeight();
        double axisH = _axisX.isVisible() ? _axisX.getPrefHeight() : 0;
        _dataView.setHeight(viewH - axisH);

        // Now set bounds of DataView
        double axisW = _axisY.isVisible() ? _axisY.getPrefWidth(viewH - axisH) : 0;
        double dataW = viewW - axisW;
        double dataH = viewH - axisH;
        _dataView.setBounds(axisW,0, dataW, dataH);

        // Set bounds of AxisX, AxisY
        _axisX.setBounds(axisW, dataH, dataW, axisH);
        _axisY.setBounds(0,0, axisW, dataH);
    }
}
