package snapcharts.doc;
import snap.util.FilePathUtils;
import snap.util.PropObject;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snap.web.WebURL;

/**
 * A class to hold multiple chart objects.
 */
public class Doc<T extends PropObject> extends DocItemGroup<T> {

    // The Source URL
    private WebURL  _srcURL;

    // Constants
    public static final String CHARTS_FILE_EXTENSION = "charts";

    /**
     * Constructor.
     */
    public Doc()
    {
        super();
    }

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
     * Returns the filename.
     */
    public String getFilename()
    {
        WebURL url = getSourceURL();
        String filename = url != null ? FilePathUtils.getFileName(url.getPath()) : "Untitled.charts";
        if (!filename.toLowerCase().endsWith(".charts"))
            filename = "Untitled.charts";
        return filename;
    }

    /**
     * Override to return this.
     */
    @Override
    public Doc getDoc()  { return this; }

    /**
     * Returns XML String for ChartDoc.
     */
    public String getChartsFileXMLString()
    {
        ChartArchiver archiver = new ChartArchiver();
        XMLElement xml = archiver.writeToXML(this);
        String xmlStr = xml.getString();
        return xmlStr;
    }

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
