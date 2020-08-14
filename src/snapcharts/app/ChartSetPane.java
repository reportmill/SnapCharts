package snapcharts.app;
import snap.gfx.Color;
import snap.util.PropChange;
import snap.view.ColView;
import snap.view.RowView;
import snapcharts.model.Chart;
import snapcharts.model.DocItem;
import snapcharts.model.DocItemChart;
import snapcharts.model.DocItemGroup;
import snapcharts.views.PageView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class ChartSetPane extends DocItemPane {

    // The DocItem that this class displays
    private DocItemGroup  _docItem;

    // The list of charts
    private List<Chart>  _charts = new ArrayList<>();

    // The top ColView
    private ColView  _topColView;

    // The list of pages
    private List<PageView> _pageViews = new ArrayList<>();

    // The Inspector
    private ChartSetPaneInsp _insp;

    // Runnable to reset charts later
    private Runnable  _resetChartsRun, _resetChartsRunShared = () -> resetChartsImpl();

    // Constants
    public static Color BACK_FILL = new Color(165, 179, 216).brighter();

    /**
     * Returns the DocItem that this ChartSetPane displays.
     */
    public DocItemGroup getDocItem()  { return _docItem; }

    /**
     * Sets the DocItem that this ChartSetPane displays.
     */
    public void setDocItem(DocItemGroup anItem)
    {
        _docItem = anItem;
        _docItem.addPropChangeListener(pc -> docItemDidPropChange(pc));
    }

    /**
     * Called when DocItem has prop change.
     */
    private void docItemDidPropChange(PropChange aPC)
    {
        // Get PropChange.PropName
        String propName = aPC.getPropName();

        // Handle Items
        if (propName==DocItem.Items_Prop)
            resetCharts();
        if (propName==DocItemGroup.ItemsPerPage_Prop)
            resetCharts();
        if (propName==DocItemGroup.Portrait_Prop)
            resetCharts();
    }

    /**
     * Returns the list of charts.
     */
    public List<Chart> getCharts()  { return _charts; }

    /**
     * Resets the charts.
     */
    private void resetCharts()
    {
        if (_resetChartsRun==null)
            runLater(_resetChartsRun=_resetChartsRunShared);
    }

    /**
     * Resets the charts.
     */
    private void resetChartsImpl()
    {
        _charts.clear();
        _topColView.removeChildren();
        _pageViews.clear();

        // Get List of DocItemChart
        List<DocItemChart> chartDocItems = getFileteredList(_docItem.getItems(), DocItemChart.class);

        // Get number of plots per page
        DocItemGroup docItem = getDocItem();
        int plotsPerPage = docItem.getItemsPerPage();

        // Get the current page
        PageView pageView = new PageView();
        pageView.setVertical(docItem.isPortrait());
        _pageViews.add(pageView);
        _topColView.addChild(pageView);

        // Get charts
        for (int i=0; i<chartDocItems.size(); i++) {

            // Get Chart and to Charts list
            DocItemChart item = chartDocItems.get(i);
            Chart chart = item.getChart();
            _charts.add(chart);

            // Create/add PageView if needed
            if (pageView==null) {
                pageView = new PageView();
                pageView.setVertical(docItem.isPortrait());
                _pageViews.add(pageView);
                _topColView.addChild(pageView);
            }

            // Add to PageView
            pageView.addChart(chart);

            // If next chart needs new page, clear
            if ((i+1)%plotsPerPage==0)
                pageView = null;
        }
        _resetChartsRun = null;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        RowView topRowView = getUI(RowView.class);

        // Create/add InspectorPane
        _insp = new ChartSetPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        _topColView = getView("TopColView", ColView.class);
        _topColView.setFill(BACK_FILL);

        resetCharts();
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    private static <T,R> List<R> getFileteredList(Collection<T> aList, Class<R> aClass)
    {
        return (List<R>) aList.stream().filter(item -> aClass.isInstance(item)).collect(Collectors.toList());
    }
}