/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
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
            addChild(_chartView);
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

        // Return
        return chartView;
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        if (_chartView != null)
            return BoxView.getPrefWidth(this, _chartView, aH);
        return super.getPrefWidthImpl(aH);
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        if (_chartView != null)
            return BoxView.getPrefHeight(this, _chartView, aW);
        return super.getPrefHeightImpl(aW);
    }

    @Override
    protected void layoutImpl()
    {
        if (_chartView != null)
            BoxView.layout(this, _chartView, true, true);
        else super.layoutImpl();
    }
}
