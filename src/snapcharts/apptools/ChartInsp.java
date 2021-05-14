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
        ChartType type = chart.getType();
        String typeName = type.getStringPlain() + "ChartButton";
        if (type == ChartType.SCATTER)
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
        if (anEvent.equals("AreaChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.AREA);
        if (anEvent.equals("ScatterChartButton"))
            ChartUtils.setScatterType(chart, ChartUtils.ScatterType.SCATTER);
        if (anEvent.equals("BarChartButton"))
            chart.setType(ChartType.BAR);
        if (anEvent.equals("PieChartButton"))
            chart.setType(ChartType.PIE);
        if (anEvent.equals("PolarChartButton"))
            chart.setType(ChartType.POLAR);
        if (anEvent.equals("ContourChartButton"))
            chart.setType(ChartType.CONTOUR);
        if (anEvent.equals("PolarContourChartButton"))
            chart.setType(ChartType.POLAR_CONTOUR);
        if (anEvent.equals("Bar3DChartButton"))
            chart.setType(ChartType.BAR_3D);
        if (anEvent.equals("Pie3DChartButton"))
            chart.setType(ChartType.PIE_3D);
        if (anEvent.equals("Line3DChartButton"))
            chart.setType(ChartType.LINE_3D);
    }
}