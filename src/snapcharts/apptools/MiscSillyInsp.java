/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.charts.ChartPart;

/**
 * This class provides UI editing for miscellaneous ChartPart properties.
 */
public class MiscSillyInsp extends ChartPartInsp {

    // Points of interest
    public static final String POI_FirstPoint = "First Point";
    public static final String POI_LastPoint = "Last Point";
    public static final String POI_LocalMin = "Local Min";
    public static final String POI_LocalMax = "Local Max";
    public static final String POI_SlopeMin = "Slope Min";
    public static final String POI_SlopeMax = "Slope Max";

    // POI array
    public static String[] ALL_POI = { POI_FirstPoint, POI_LastPoint, POI_LocalMin, POI_LocalMax, POI_SlopeMin, POI_SlopeMax };

    /**
     * Constructor.
     */
    public MiscSillyInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    @Override
    public String getName()
    {
        return "Points of Interest";
    }

    @Override
    public ChartPart getChartPart()
    {
        ChartPane chartPane = getChartPane();
        return chartPane.getSelChartPart();
    }

    @Override
    protected void initUI()
    {
        // Configure ListView
        ListView<String> listView = getView("ListView", ListView.class);
        listView.setItems(ALL_POI);
    }

    /**
     * Update UI.
     */
    @Override
    protected void resetUI()
    {
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
    }
}
