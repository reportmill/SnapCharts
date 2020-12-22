package snapcharts.views;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;

/**
 * A ChartHelper for ChartType BAR.
 */
public class ChartHelperBar extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperBar(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR; }

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
        return new DataArea[] { new DataAreaBar() };
    }
}
