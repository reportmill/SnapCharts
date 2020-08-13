package snapcharts.app;
import snap.gfx.Color;
import snap.util.PropChange;
import snap.view.ColView;
import snap.view.RowView;
import snapcharts.model.Chart;
import snapcharts.model.DocItem;
import snapcharts.model.DocItemChart;
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
    private DocItem  _docItem;

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
    public DocItem getDocItem()  { return _docItem; }

    /**
     * Sets the DocItem that this ChartSetPane displays.
     */
    public void setDocItem(DocItem anItem)
    {
        _docItem = anItem;
        _docItem.addPropChangeListener(pc -> docItemDidPropChange(pc));
    }

    /**
     * Called when DocItem has prop change.
     */
    private void docItemDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();

        // Handle Items
        if (propName==DocItem.Items_Prop) {
            resetCharts();
        }
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

        // Get charts
        for (DocItemChart item : chartDocItems) {

            // Get Chart and to Charts list
            Chart chart = item.getChart();
            _charts.add(chart);

            // Add to PageView
            PageView pageView = new PageView();
            pageView.addChart(chart);
            _pageViews.add(pageView);
            _topColView.addChild(pageView);
        }
        _resetChartsRun = null;
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    private static <T,R> List<R> getFileteredList(Collection<T> aList, Class<R> aClass)
    {
        return (List<R>) aList.stream().filter(item -> aClass.isInstance(item)).collect(Collectors.toList());
    }

    /**
     * Returns whether pages are portrait.
     */
    public boolean isPortrait()
    {
        PageView pview = _pageViews.size()>0 ? _pageViews.get(0) : null;
        return pview==null || pview.isVertical();
    }

    /**
     * Sets whether pages are portrait.
     */
    public void setPortrait(boolean aValue)
    {
        for (PageView pview : _pageViews)
            pview.setVertical(aValue);
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
}
