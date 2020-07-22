package snapcharts.app;

import snap.gfx.Font;
import snap.gfx.ShadowEffect;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.views.ChartView;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPane extends PartPane {
    
    // The chartView
    private ChartView _chartView;
    
    // The ChartBox
    private BoxView  _chartBox;
    
    // The DataPane to handle chart property editing
    private DataPane  _dataPane;

    // The Inspector
    private ChartPaneInsp _insp;

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return _chartView;
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        return _chartView.getChart();
    }

    /**
     * Sets the Chart.
     */
    public void setChart(Chart aChart)
    {
        getUI();
        _chartView.setChart(aChart);
        resetLater();
    }

    /**
     * Override to return the ChartView.
     */
    @Override
    public View getPartView()
    {
        return getChartView();
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Get ColView
        RowView topRowView = (RowView)super.createUI();

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setEffect(new ShadowEffect());

        // Create ChartBox
        _chartBox = (BoxView)topRowView.getChild("ChartBox");
        _chartBox.setContent(_chartView);
        _chartBox.setFill(ChartSetPane.BACK_FILL);

        // Create DataPane
        _dataPane = new DataPane(_chartView);
        _dataPane.getUI().setPrefHeight(300);
        _dataPane.getUI().setFont(Font.Arial13);

        // Configure TopColView
        ColView topColView = (ColView)topRowView.getChild("TopColView");
        topColView.setChildren(_chartBox, _dataPane.getUI());
        SplitView splitView = SplitView.makeSplitView(topColView);
        splitView.setDividerSpan(5);

        // Create/add InspectorPane
        _insp = new ChartPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Return TopRowView
        return topRowView;
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        System.out.println("EditorPane.respondUI: " + anEvent);
    }
}