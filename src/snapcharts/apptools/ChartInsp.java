package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.model.*;
import snapcharts.app.ChartPane;
import snapcharts.util.ChartUtils;

/**
 * A class to manage UI to edit a ChartView.
 */
public class ChartInsp extends ChartPartInsp {
    
    /**
     * Constructor.
     */
    public ChartInsp(ChartPane aCP)
    {
        super(aCP);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Chart Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Chart
        Chart chart = getChart();

        // Reset NameText
        setViewValue("NameText", chart.getName());

        // Reset ChartButtons
        TraceType traceType = chart.getTraceType();
        String typeName = traceType.getName() + "ChartButton";
        if (traceType == TraceType.Scatter)
            typeName = ChartUtils.getScatterTypeString(chart) + "ChartButton";
        setViewValue(typeName, true);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Chart
        Chart chart = getChart();

        // Handle NameText
        if (anEvent.equals("NameText")) {
            chart.setName(anEvent.getStringValue());
            getChartPane().getDocPane().docItemNameChanged();
        }

        // Handle BarChartButton, LineChartButton, PieChartButton
        if (anEvent.equals("LineChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.LINE);
        if (anEvent.equals("ScatterChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.SCATTER);
        if (anEvent.equals("AreaChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.AREA);
        if (anEvent.equals("StackedAreaChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.STACKED_AREA);
        if (anEvent.equals("BarChartButton"))
            chart.setTraceType(TraceType.Bar);
        if (anEvent.equals("PieChartButton"))
            chart.setTraceType(TraceType.Pie);
        if (anEvent.equals("PolarChartButton"))
            chart.setTraceType(TraceType.Polar);
        if (anEvent.equals("ContourChartButton"))
            chart.setTraceType(TraceType.Contour);
        if (anEvent.equals("PolarContourChartButton"))
            chart.setTraceType(TraceType.PolarContour);
        if (anEvent.equals("Bar3DChartButton"))
            chart.setTraceType(TraceType.Bar3D);
        if (anEvent.equals("Pie3DChartButton"))
            chart.setTraceType(TraceType.Pie3D);
        if (anEvent.equals("Line3DChartButton"))
            chart.setTraceType(TraceType.Line3D);
        if (anEvent.equals("Contour3DChartButton"))
            chart.setTraceType(TraceType.Contour3D);
    }
}