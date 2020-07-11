package snapcharts.model;
import snap.web.WebURL;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold multiple chart objects.
 */
public class ChartDoc extends ChartPart {

    // The list of charts
    List<Chart> _charts = new ArrayList<>();

    /**
     * Returns the charts.
     */
    public List<Chart> getCharts()  { return _charts; }

    /**
     * Returns the number of charts.
     */
    public int getChartCount() { return _charts.size(); }

    /**
     * Returns the individual chart at given index.
     */
    public Chart getChart(int anIndex)  { return _charts.get(anIndex); }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart)
    {
        addChart(aChart, _charts.size());
    }

    /**
     * Adds a chart at given index.
     */
    public void addChart(Chart aChart, int anIndex)
    {
        _charts.add(anIndex, aChart);
    }

    /**
     * Removes a chart at given index.
     */
    public Chart removeChart(int anIndex)
    {
        return _charts.remove(anIndex);
    }

    /**
     * Loads the ChartView from JSON source.
     */
    public static ChartDoc createDocFromSource(Object aSrc)
    {
        WebURL url = WebURL.getURL(aSrc);
        String jsonText = url.getText();
        return createDocFromJSONString(jsonText);
    }

    /**
     * Loads the ChartView from JSON string.
     */
    public static ChartDoc createDocFromJSONString(String aStr)
    {
        ChartParser parser = new ChartParser();
        Chart chart = parser.getChartForJSONString(aStr);

        if(chart.getDataSet().isEmpty())
            chart.getDataSet().addSeriesForNameAndValues("Sample", 1d, 2d, 3d, 3d, 4d, 5d);

        ChartDoc cdoc = new ChartDoc();
        cdoc.addChart(chart);
        return cdoc;
    }
}
