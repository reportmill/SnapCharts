package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.ColView;
import snap.view.RowView;
import snap.view.ViewOwner;
import snapcharts.model.Chart;
import snapcharts.views.ChartView;
import java.util.ArrayList;
import java.util.List;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class ChartSetPane extends ViewOwner {

    // The list of charts
    private List<Chart>  _charts = new ArrayList<>();

    // The Inspector
    private ChartSetPaneInsp _insp;

    /**
     * Returns the list of charts.
     */
    public List<Chart> getCharts()  { return _charts; }

    /**
     * Sets the chart list.
     */
    public void setCharts(List<Chart> theCharts)
    {
        _charts.clear();
        _charts.addAll(theCharts);
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

        ColView colView = getView("TopColView", ColView.class);

        for (Chart chart : getCharts()) {
            ChartView chartView = new ChartView();
            chartView.setBorder(Color.BLACK, 1);
            chartView.setEffect(new ShadowEffect());
            chartView.setChart(chart);
            colView.addChild(chartView);
        }
    }
}
