package snapcharts.app;
import snap.util.XMLElement;
import snap.view.*;
import snapcharts.model.ChartArchiver;
import snapcharts.model.ChartPart;

/**
 * A CopyPaster implementation for Editor.
 */
public class DocPaneCopyPaster {

    // The DocPane
    private DocPane _docPane;

    // The MIME type for archival format
    public static final String    SNAPCHART_XML_TYPE = "snap-chart/xml";

    /**
     * Creates EditorCopyPaster for given editor.
     */
    public DocPaneCopyPaster(DocPane aDP)
    {
        _docPane = aDP;
    }

    /**
     * Returns the DocPane.
     */
    public DocPane getEditor()  { return _docPane; }

    /**
     * Handles editor cut operation.
     */
    public void cut()
    {
        _docPane.copy();
        _docPane.delete();
    }

    /**
     * Handles editor copy operation.
     */
    public void copy()
    {
        // Get selected chart part
        ChartPart selPart = _docPane.getSelPart();

        // Get clipboard
        Clipboard cb = Clipboard.get();

        // Get image and add to clipbard
        //PageView pageView = _editor._page
        //int scale = ViewUtils.isAltDown() ? 1 : 0;
        //Image image = ViewUtils.getImageForScale(view, scale);
        //cb.addData(image);

        // Get xml string for selected shapes and add to clipboard as SNAP_XML
        XMLElement xml = new ChartArchiver().writeToXML(selPart);
        String xmlStr = xml.getString();
        cb.addData(SNAPCHART_XML_TYPE, xmlStr);

        // Add xml as String (probably stupid)
        //cb.addData(xmlStr);
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // Get Clipboard
        Clipboard cb = Clipboard.get();

        // Handle SNAP_XML: Get bytes, unarchive view and add
        if (cb.hasData(SNAPCHART_XML_TYPE)) {
            byte bytes[] = cb.getDataBytes(SNAPCHART_XML_TYPE);
            ChartPart chartPart = new ChartArchiver().getChartPartFromXMLSource(bytes);
            //_docPane.addChartPart(view);
        }

        // Paste Image
        //else if (cb.hasImage()) {
        //    ClipboardData idata = cb.getImageData();
        //    byte bytes[] = idata.getBytes();
        //    ImageView iview = new ImageView(bytes);
        //    _editor.addView(iview);
        //}
    }

    /**
     * Deletes all the currently selected shapes.
     */
    public void delete()
    {
    }

    /**
     * Causes all the children of the current super selected shape to become selected.
     */
    public void selectAll()
    {
        ViewUtils.beep();
    }
}
