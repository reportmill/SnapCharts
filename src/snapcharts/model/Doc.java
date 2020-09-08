package snapcharts.model;
import snap.util.FilePathUtils;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.web.WebURL;

/**
 * A class to hold multiple chart objects.
 */
public class Doc extends DocItemGroup {

    // The Source URL
    private WebURL  _srcURL;

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

        // Handle simple file
        if (ext.equals("simple")) {
            String str = url.getText();
            Doc doc = new DocTextReader().getDocForString(str);
            return doc;
        }

        // Just return null
        return null;
    }
}
