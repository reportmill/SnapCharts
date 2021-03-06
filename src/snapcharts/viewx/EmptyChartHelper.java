package snapcharts.viewx;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

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
