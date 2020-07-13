package snapcharts.model;
import snap.web.WebURL;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold multiple chart objects.
 */
public class ChartDoc extends ChartPart {

    // The Source URL
    private WebURL  _srcURL;

    // The list of charts
    private List<Chart> _charts = new ArrayList<>();

    /**
     * Returns the Source URL.
     */
    public WebURL getSourceURL()  { return _srcURL; }

    /**
     * Sets the Source URL.
     */
    protected void setSourceURL(WebURL aURL)
    {
        _srcURL = aURL;
    }

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
        aChart.setDoc(this);
    }

    /**
     * Removes a chart at given index.
     */
    public Chart removeChart(int anIndex)
    {
        return _charts.remove(anIndex);
    }

    /**
     * Override to return null.
     */
    public ChartPart getParent()  { return null; }

    /**
     * Loads the ChartView from JSON source.
     */
    public static ChartDoc createDocFromSource(Object aSrc)
    {
        ChartParser parser = new ChartParser();
        ChartDoc doc = parser.getDocForSource(aSrc);

        Chart chart = doc.getChartCount()>0 ? doc.getChart(0) : null;
        if(chart!=null && chart.getDataSetList().isEmpty())
            chart.getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 3d, 3d, 4d, 5d);

        return doc;
    }
}
