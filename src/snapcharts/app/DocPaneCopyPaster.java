package snapcharts.app;
import snap.gfx.Image;
import snap.util.XMLElement;
import snap.view.*;
import snapcharts.doc.ChartArchiver;
import snapcharts.doc.DocItemParent;
import snapcharts.doc.DocItemTrace;
import snapcharts.charts.Chart;
import snapcharts.charts.ChartPart;
import snapcharts.doc.DocItem;
import snapcharts.charts.Trace;

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
    public DocPane getDocPane()  { return _docPane; }

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
        DocItem selPart = _docPane.getSelItem();

        // Get clipboard
        Clipboard cb = Clipboard.get();

        // Get image and add to clipbard
        DocItemPane docItemPane = _docPane.getSelItemPane();
        View view = docItemPane.getItemView();
        if (view!=null) {
            int scale = 1; //ViewUtils.isAltDown() ? 1 : 0;
            Image image = ViewUtils.getImageForScale(view, scale);
            cb.addData(image);
        }

        // Get xml string for selected shapes and add to clipboard as SNAP_XML
        ChartPart chartPart = selPart.getChartPart();
        if (chartPart!=null) {
            XMLElement xml = new ChartArchiver().writeToXML(chartPart);
            String xmlStr = xml.getString();
            cb.addData(SNAPCHART_XML_TYPE, xmlStr);
            cb.addData(xmlStr);
        }

        // Add xml as String (probably stupid)
        //cb.addData(xmlStr);
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // Get clipboard - if not loaded, come back loaded
        Clipboard clipboard = Clipboard.get();
        if (!clipboard.isLoaded()) {
            clipboard.addLoadListener(() -> paste());
            return;
        }

        // Handle SNAP_XML: Get bytes, unarchive view and add
        if (clipboard.hasData(SNAPCHART_XML_TYPE)) {
            byte[] bytes = clipboard.getDataBytes(SNAPCHART_XML_TYPE);
            ChartPart chartPart = new ChartArchiver().getChartPartFromXMLSource(bytes);
            _docPane.addChartPart(chartPart);
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
        // Get selected item and parent (just return if either are null
        DocItem selItem = _docPane.getSelItem(); if (selItem == null) return;
        DocItemParent parItem = selItem.getParent(); if (parItem == null) return;

        // Select best peer and remove item
        _docPane.selectBestDocItemPeer(selItem);

        // Handle DataSet type
        if (selItem instanceof DocItemTrace) {
            DocItemTrace docItemTrace = (DocItemTrace) selItem;
            Trace trace = docItemTrace.getTrace();
            Chart chart = trace.getChart();
            chart.getContent().removeTrace(trace);
        }

        // Handle anything else
        else parItem.removeItem(selItem);
    }

    /**
     * Causes all the children of the current super selected shape to become selected.
     */
    public void selectAll()
    {
        ViewUtils.beep();
    }
}
