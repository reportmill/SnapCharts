package snapcharts.views;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;

/**
 * A stub ChartHelper.
 */
public class ChartHelperEmpty extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperEmpty(ChartView aChartView)
    {
        super(aChartView);
    }

    @Override
    public ChartType getChartType()
    {
        return null;
    }

    /**
     * Returns the AxisTypes.
     */
    @Override
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[0];
    }

    @Override
    protected DataArea[] createDataAreas()
    {
        return new DataArea[0];
    }
}
