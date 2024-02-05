package snapcharts.viewx;
import snapcharts.charts.AxisType;
import snapcharts.charts.TraceType;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.TraceView;

/**
 * A stub ChartHelper.
 */
public class EmptyChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public EmptyChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    @Override
    public TraceType getTraceType()
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
    protected TraceView[] createTraceViews()
    {
        return new TraceView[0];
    }
}
