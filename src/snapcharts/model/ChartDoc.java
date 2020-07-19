package snapcharts.model;
import snap.util.FilePathUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
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

    // Constants for properties
    public static final String Charts_Prop = "Charts";

    // Constants
    public static final String CHARTS_FILE_EXTENSION = "charts";

    /**
     * Returns the Source URL.
     */
    public WebURL getSourceURL()  { return _srcURL; }

    /**
     * Sets the Source URL.
     */
    public void setSourceURL(WebURL aURL)
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
        firePropChange(Charts_Prop, null, aChart, anIndex);
        aChart.setDoc(this);
    }

    /**
     * Removes a chart at given index.
     */
    public Chart removeChart(int anIndex)
    {
        Chart chart = _charts.remove(anIndex);
        firePropChange(Charts_Prop, chart, null, anIndex);
        return chart;
    }

    /**
     * Override to return null.
     */
    public ChartPart getParent()  { return null; }

    /**
     * Returns XML bytes for ChartDoc.
     */
    public byte[] getChartsFileXMLBytes()
    {
        ChartArchiver archiver = new ChartArchiver();
        byte bytes[] = archiver.writeToXMLBytes(this);
        return bytes;
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive charts
        XMLElement chartsXML = new XMLElement(Charts_Prop);
        e.add(chartsXML);
        for (Chart chart : getCharts())
            chartsXML.add(anArchiver.toXML(chart));

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive charts
        XMLElement chartsXML = anElement.get(Charts_Prop);
        if (chartsXML!=null) {
            List<XMLElement> chartXMLs = chartsXML.getElements("Chart");
            for (XMLElement chartXML : chartXMLs) {
                Chart chart = (Chart)anArchiver.fromXML(chartXML, this);
                if (chart!=null)
                    addChart(chart);
            }
        }

        // Return this part
        return this;
    }

    /**
     * Loads the ChartView from JSON source.
     */
    public static ChartDoc createDocFromSource(Object aSource)
    {
        WebURL url = WebURL.getURL(aSource);
        if (url==null) {
            System.err.println("ChartDoc.createDocFromSource: Can't find URL for source: " + aSource);
            return null;
        }

        // Handle SnapCharts .charts file
        String path = url.getPath();
        String ext = FilePathUtils.getExtension(path).toLowerCase();
        if (ext.equals(CHARTS_FILE_EXTENSION)) {
            ChartArchiver archiver = new ChartArchiver();
            ChartDoc doc = archiver.getDocFromXMLSource(url);
            return doc;
        }

        // Handle json file
        if (ext.equals("json")) {
            ChartParser parser = new ChartParser();
            ChartDoc doc = parser.getDocForSource(url);

            Chart chart = doc.getChartCount() > 0 ? doc.getChart(0) : null;
            if (chart != null && chart.getDataSetList().isEmpty())
                chart.getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 3d, 3d, 4d, 5d);
            return doc;
        }

        // Just return null
        return null;
    }
}
