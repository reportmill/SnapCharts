package snapcharts.app;
import snap.gfx.Color;
import snap.view.ColView;
import snap.view.RowView;
import snapcharts.model.Chart;
import snapcharts.views.PageView;
import java.util.ArrayList;
import java.util.List;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class ChartSetPane extends DocItemPane {

    // The list of charts
    private List<Chart>  _charts = new ArrayList<>();

    // The top ColView
    private ColView  _topColView;

    // The list of pages
    private List<PageView> _pageViews = new ArrayList<>();

    // The Inspector
    private ChartSetPaneInsp _insp;

    // Constants
    public static Color BACK_FILL = new Color(165, 179, 216).brighter();

    /**
     * Returns the list of charts.
     */
    public List<Chart> getCharts()  { return _charts; }

    /**
     * Sets the chart list.
     */
    public void setCharts(List<Chart> theCharts)
    {
        getUI();
        _charts.clear();

        _topColView.removeChildren();
        _pageViews.clear();

        for (Chart chart : theCharts) {
            addChart(chart);
        }
    }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart)
    {
        addChart(aChart, _charts.size());
    }

    /**
     * Adds a chart.
     */
    public void addChart(Chart aChart, int anIndex)
    {
        _charts.add(anIndex, aChart);

        PageView pageView = new PageView();
        pageView.addChart(aChart);
        _pageViews.add(pageView);
        _topColView.addChild(pageView);
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
    }
}
