/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.BoxView;
import snap.view.TextArea;
import snap.view.View;
import snapcharts.model.Chart;
import snapcharts.view.ChartView;

/**
 * This View subclass shows snippets.
 */
public class ResponseView extends EntryView<Response> {

    /**
     * Constructor.
     */
    public ResponseView(NotebookView aNotebookView, Response aResponse)
    {
        // Do normal version
        super(aNotebookView, aResponse);
    }

    /**
     * Override to support custom content views for response values.
     */
    @Override
    protected View createContentViewForEntry(Entry anEntry)
    {
        // Get entry as response and get response.Value
        Response response = (Response) anEntry;
        Object value = response.getValue();

        // Handle Chart
        if (value instanceof Chart) {
            Chart chart = (Chart) value;
            ChartView chartView = createChartViewForChartString(chart);
            BoxView boxView = new BoxView(chartView, false, false);
            boxView.setPadding(0, 0, 0, 12);
            return boxView;
        }

        // Do normal version
        return super.createContentViewForEntry(anEntry);
    }

    /**
     * Override to make gray.
     */
    @Override
    protected TextArea createTextArea()
    {
        TextArea textArea = super.createTextArea();
        textArea.setFill(Color.GRAY9);
        return textArea;
    }

    /**
     * Creates ChartView for chart.
     */
    private ChartView createChartViewForChartString(Chart aChart)
    {
        // Create ChartView for Chart
        ChartView chartView = new ChartView();
        chartView.setChart(aChart);
        chartView.setPrefSize(560, 340);
        chartView.setLean(Pos.TOP_LEFT);
        chartView.setGrowWidth(false);

        // Style
        chartView.setEffect(new ShadowEffect(8, Color.GRAY3, 0, 0));

        // Return
        return chartView;
    }

    /**
     * Override for response.
     */
    @Override
    protected String getLabelPrefix()  { return "Out"; }
}
