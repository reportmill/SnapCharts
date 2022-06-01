/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.BoxView;
import snap.view.TextArea;
import snap.view.View;
import snapcharts.app.DataSetPane;
import snapcharts.data.DataSet;
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
        if (value instanceof Chart)
            return createContentViewForChart((Chart) value);

        // Handle DataSet
        if (value instanceof DataSet)
            return createContentViewForDataSet((DataSet) value);

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
     * Creates content view for Chart.
     */
    private View createContentViewForChart(Chart aChart)
    {
        // Create ChartView for Chart
        ChartView chartView = new ChartView();
        chartView.setChart(aChart);
        chartView.setPrefSize(560, 340);
        chartView.setLean(Pos.TOP_LEFT);
        chartView.setGrowWidth(false);

        // Style
        chartView.setEffect(new ShadowEffect(8, Color.GRAY3, 0, 0));

        // Create BoxView wrapper
        BoxView boxView = new BoxView(chartView, false, false);
        boxView.setPadding(0, 0, 0, 12);

        // Return
        return boxView;
    }

    /**
     * Creates content view for DataSet.
     */
    private View createContentViewForDataSet(DataSet dataSet)
    {
        // Create/configure DataSetPane
        DataSetPane dataSetPane = new DataSetPane(dataSet);
        View dataSetPaneView = dataSetPane.getUI();
        dataSetPaneView.setPrefSize(560, 340);

        // Create BoxView wrapper
        BoxView boxView = new BoxView(dataSetPaneView, false, false) {
            public Shape getBoundsShape()
            {
                return new RoundRect(0,0, getWidth(), getHeight(), 4);
            }
        };
        boxView.setFill(Color.WHITE);
        boxView.setEffect(new ShadowEffect(8, Color.GRAY3, 0, 0));
        boxView.setMargin(0, 0, 0, 22);

        // Return
        return boxView;
    }

    /**
     * Override for response.
     */
    @Override
    protected String getLabelPrefix()  { return "Out"; }
}
