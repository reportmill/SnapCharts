package snapcharts.app;
import rmdraw.scene.SGDoc;
import snap.view.View;
import snap.view.ViewAnimUtils;
import snap.view.ViewOwner;
import snapcharts.doc.*;
import snapcharts.model.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The base class for DocItem editors.
 */
public class DocItemPane extends ViewOwner {

    // The DocPane that holds this DocItemPane
    private DocPane  _docPane;

    // Whether inspector is showing
    private boolean  _showInsp = true;

    // A map of DocItems to a DocItemPane
    private static Map<DocItem,DocItemPane> _docItemPanes = new HashMap<>();

    /**
     * Returns the DocPane.
     */
    public DocPane getDocPane()  { return _docPane; }

    /**
     * Sets the DocPane.
     */
    public void setDocPane(DocPane aDP)
    {
        _docPane = aDP;
    }

    /**
     * Returns the view for the DocItem.
     */
    public View getItemView()  { return null; }

    /**
     * Returns whether inspector is visible.
     */
    public boolean isShowInspector()  { return _showInsp; }

    /**
     * Sets whether inspector is visible.
     */
    public void setShowInspector(boolean aValue)
    {
        // If already set, just return
        if (aValue == isShowInspector()) return;

        // Set value
        _showInsp = aValue;

        // Get inspector and view
        ViewOwner insp = getInspector(); if (insp==null) return;
        View inspView = insp.getUI();

        // Set visible (animated)
        ViewAnimUtils.setVisible(inspView, aValue, true, false);
    }

    /**
     * Returns the inspector.
     */
    public ViewOwner getInspector()  { return null; }

    /**
     * Returns the DocItemPane for given DocItem.
     */
    public static DocItemPane getItemPane(DocItem anItem)
    {
        DocItemPane pane = _docItemPanes.get(anItem);
        if (pane!=null)
            return pane;

        pane = createItemPane(anItem);
        _docItemPanes.put(anItem, pane);
        return pane;
    }

    /**
     * Creates a DocItemPane for given DocItem.
     */
    protected static DocItemPane createItemPane(DocItem anItem)
    {
        // Handle DocItemChart
        if (anItem instanceof DocItemChart) {
            Chart chart = ((DocItemChart)anItem).getChart();
            ChartPane pane = new ChartPane();
            pane.setChart(chart);
            return pane;
        }

        // Handle DocItemDataSet
        if (anItem instanceof DocItemDataSet) {
            DataSet dset = ((DocItemDataSet)anItem).getDataSet();
            ChartPane pane = new ChartPane();
            pane.setDataSet(dset);
            return pane;
        }

        // Handle DocItemReport
        if (anItem instanceof DocItemReport) {
            SGDoc doc = ((DocItemReport)anItem).getReportDoc();
            ReportPane reportPane = new ReportPane();
            reportPane.setReportDoc(doc);
            return reportPane;
        }

        // Handle DocItemGroup
        if (anItem instanceof DocItemGroup) {
            ChartSetPane pane = new ChartSetPane();
            pane.setDocItem((DocItemGroup)anItem);
            return pane;
        }

        // Complain (bitterly)
        throw new RuntimeException("DocItemPane.createItemPane: Unknown item: " + anItem);
    }
}
