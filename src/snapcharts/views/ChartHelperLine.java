package snapcharts.views;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;

/**
 * A ChartHelper for ChartType LINE, AREA, SCATTER.
 */
public class ChartHelperLine extends ChartHelper {

    // The ChartType
    private ChartType  _chartType;

    /**
     * Constructor.
     */
    public ChartHelperLine(ChartView aChartView, ChartType aChartType)
    {
        super(aChartView);
        _chartType = aChartType;
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return _chartType; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y };
    }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        return new DataArea[] { new DataAreaLine(this) };
    }
}
