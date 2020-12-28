package snapcharts.views;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;

/**
 * A ChartHelper for ChartType BAR_3D.
 */
public class ChartHelperBar3D extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperBar3D(ChartView aChartView)
    {
        super(aChartView);

        // Hide axes
        for (AxisView axisView : getAxisViews())
            axisView.setVisible(false);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR_3D; }

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
        return new DataArea[] { new DataAreaBar3D(this, null) };
    }
}
