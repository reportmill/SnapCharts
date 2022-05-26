/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.view.BoxView;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import snapcharts.view.ChartView;

/**
 * This View subclass shows snippets.
 */
public class ResponseView extends EntryView<Response> {

    // A ChartView for Chart response
    private ChartView  _chartView;

    /**
     * Constructor.
     */
    public ResponseView(NotebookView aNotebookView, Response aResponse)
    {
        // Do normal version
        super(aNotebookView, aResponse);

        // Set TextArea background to gray
        _textArea.setFill(Color.GRAY9);

        // Handle Chart response
        String text = aResponse.getText();
        if (text.startsWith("<")) {
            _chartView = createChartViewForChartString(text);
            _chartView.setLean(Pos.TOP_LEFT);
            _chartView.setGrowWidth(false);
            BoxView boxView = new BoxView(_chartView, false, false);
            addChild(boxView);
        }
    }

    /**
     * Creates ChartView for string.
     */
    private ChartView createChartViewForChartString(String chartString)
    {
        // Debug
        System.out.println(chartString);

        // Create Chart from chartString
        ChartArchiver chartArchiver = new ChartArchiver();
        Chart chart = (Chart) chartArchiver.readFromXMLString(chartString);

        // Create ChartView for Chart
        ChartView chartView = new ChartView();
        chartView.setChart(chart);
        chartView.setPrefSize(640, 400);

        // Return
        return chartView;
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        if (_chartView != null)
            return BoxView.getPrefWidth(this, _chartView.getParent(), aH);
        return super.getPrefWidthImpl(aH);
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        if (_chartView != null)
            return BoxView.getPrefHeight(this, _chartView.getParent(), aW);
        return super.getPrefHeightImpl(aW);
    }

    @Override
    protected void layoutImpl()
    {
        if (_chartView != null)
            BoxView.layout(this, _chartView.getParent(), true, true);
        else super.layoutImpl();
    }
}
