package snapcharts.model;
import snap.util.FilePathUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.web.WebURL;
import snapcharts.app.ChartSetPane;
import snapcharts.app.DocItemPane;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold multiple chart objects.
 */
public class Doc extends DocItem {

    // The Source URL
    private WebURL  _srcURL;

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
     * Creates the ItemPane.
     */
    @Override
    protected DocItemPane createItemPane()
    {
        ChartSetPane pane = new ChartSetPane();
        pane.setCharts(getCharts());
        return pane;
    }

    /**
     * Returns the charts.
     */
    public List<Chart> getCharts()
    {
        List<Chart> charts = new ArrayList<>();
        for (DocItem item : getItems())
            if (item instanceof DocItemChart)
                charts.add(((DocItemChart)item).getChart());
        return charts;
    }

    /**
     * Adds a chart.
     */
    public DocItem addChart(Chart aChart)
    {
        DocItemChart chartDocItem = new DocItemChart(aChart);
        addItem(chartDocItem);
        return chartDocItem;
    }

    /**
     * Adds a chart at given index.
     */
    public DocItem addChart(Chart aChart, int anIndex)
    {
        DocItemChart chartDocItem = new DocItemChart(aChart);
        addItem(chartDocItem, anIndex);
        return chartDocItem;
    }

    /**
     * Override to return this.
     */
    @Override
    public Doc getDoc()  { return this; }

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
    public static Doc createDocFromSource(Object aSource)
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
            Doc doc = archiver.getDocFromXMLSource(url);
            return doc;
        }

        // Handle json file
        if (ext.equals("json")) {
            ChartParser parser = new ChartParser();
            Doc doc = parser.getDocForSource(url);

            DocItemChart chartDocItem = doc.getItemCount()>0 ? (DocItemChart) doc.getItem(0) : null;
            Chart chart = chartDocItem!=null ? chartDocItem.getChart() : null;
            if (chart != null && chart.getDataSetList().isEmpty())
                chart.getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 3d, 3d, 4d, 5d);
            return doc;
        }

        // Just return null
        return null;
    }
}
