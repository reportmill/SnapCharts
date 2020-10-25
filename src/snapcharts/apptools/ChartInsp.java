package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartType;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;

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
        if (anEvent.equals("LineChartButton")) chart.setType(ChartType.LINE);
        if (anEvent.equals("AreaChartButton")) chart.setType(ChartType.AREA);
        if (anEvent.equals("ScatterChartButton")) chart.setType(ChartType.SCATTER);
        if (anEvent.equals("BarChartButton")) chart.setType(ChartType.BAR);
        if (anEvent.equals("Bar3DChartButton")) chart.setType(ChartType.BAR_3D);
        if (anEvent.equals("PieChartButton")) chart.setType(ChartType.PIE);
    }
}