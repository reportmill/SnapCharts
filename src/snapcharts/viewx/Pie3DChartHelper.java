package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for ChartType PIE.
 */
public class Pie3DChartHelper extends PieChartHelper {

    // Constant for default chart depth
    public static final double DEFAULT_DEPTH = 50;

    /**
     * Constructor.
     */
    public Pie3DChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    @Override
    public ChartType getChartType()  { return ChartType.PIE_3D; }

    /**
     * Returns the AxisTypes.
     */
    @Override
    protected AxisType[] getAxisTypesImpl()  { return new AxisType[0]; }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getEnabledTraces();
        if (traces.length == 0)
            traces = traceList.getTraces();
        if (traces.length == 0)
            return new DataArea[0];
        Trace trace = traces[0];
        return new DataArea[] { _dataArea = new Pie3DDataArea(this, trace) };
    }
}
