package snapcharts.views;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartType;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to display data (via DataArea).
 */
public class DataView<T extends ChartPart> extends ChartPartView<T> {

    // The ChartView
    private ChartView  _chartView;

    // The DataView
    private DataArea  _dataArea;

    // The DataAreas
    private List<DataArea>  _dataAreas = new ArrayList<>();

    // The X AxisView
    private AxisViewX  _axisX;

    // The Y AxisView
    private AxisViewY  _axisY;

    // Constants
    protected static int DEFAULT_REVEAL_TIME = 2000;

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
        setDataArea(DataArea.createDataAreaForType(ChartType.LINE));
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
     * Sets the DataArea.
     */
    protected void setDataArea(DataArea aDataArea)
    {
        // Remove old
        if (_dataArea !=null) {
            _dataArea.deactivate();
            removeChild(_dataArea);
            _dataAreas.remove(_dataArea);
        }

        // Set/add new
        _dataArea = aDataArea;
        _dataAreas.add(aDataArea);
        addChild(_dataArea);

        // Update Axes
        _dataArea.setDataView(this);
        _axisY._dataView = _axisX._dataView = aDataArea;

        _dataArea.activate();
    }

    /**
     * Returns the DataArea.
     */
    public List<DataArea> getDataAreas()  { return _dataAreas; }

    /**
     * Returns the X Axis View.
     */
    public AxisViewX getAxisX()  { return _axisX; }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisY()  { return _axisY; }

    /**
     * Return the ratio of the chart to show horizontally.
     */
    public double getReveal()
    {
        return _chartView!=null ? _chartView.getReveal() : null;
    }

    /**
     * Sets the reation of the chart to show horizontally.
     */
    public void setReveal(double aValue)
    {
        for (DataArea dataArea : getDataAreas())
            dataArea.setReveal(aValue);
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        int revealTime = 0;
        for (DataArea dataArea : getDataAreas())
            revealTime = Math.max(revealTime, dataArea.getRevealTime());
        return revealTime;
    }

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
        if (_dataArea==null || chartType!= getDataArea().getChartType()) {
            DataArea dataArea = DataArea.createDataAreaForType(chartType);
            setDataArea(dataArea);
        }

        // Reset X Axis
        _axisX.resetView();

        // Reset Y Axis
        _axisY.resetView();

        // Reset DataView
        _dataArea.reactivate();
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
