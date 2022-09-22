/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.ShadowEffect;
import snap.view.*;
import snapcharts.app.DataSetPane;
import snapcharts.data.DataSet;
import snapcharts.model.Chart;
import snapcharts.view.ChartView;

/**
 * This View subclass shows snippets.
 */
public class ResponseView extends ParentView {

    // The NotebookView
    protected NotebookView  _notebookView;

    // The response
    private Response  _response;

    // The label
    private Label  _label;

    // The content view
    private View  _content;

    // Constants
    public static final ShadowEffect DEFAULT_SHADOW = new ShadowEffect(8, Color.GRAY3, 0, 0);
    private static final Color DEFAULT_TEXTAREA_FILL = new Color(.95);
    private static final Color DEFAULT_TEXTAREA_TEXTFILL = Color.GRAY4;

    /**
     * Constructor.
     */
    public ResponseView(NotebookView aNotebookView, Response aResponse)
    {
        // Do normal version
        super();

        // Set notebookView and entry
        _notebookView = aNotebookView;
        _response = aResponse;

        // Basic style config
        setSpacing(10);
        setPadding(5, 5, 5, 5);

        // Create/add entry label
        _label = createLabel();
        addChild(_label);

        // Set label text
        String labelText = "Out [" + aResponse.getIndex() + "] = ";
        _label.setText(labelText);

        // Create/add entry textArea (content)
        View contentView = createContentViewForEntry(aResponse);
        setContent(contentView);
    }

    /**
     * Returns the Response.
     */
    public Response getResponse()  { return _response; }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        if (_content != null)
            removeChild(_content);
        _content = aView;
        if (_content != null)
            addChild(_content);
    }

    /**
     * Creates the label.
     */
    protected Label createLabel()
    {
        Label label = new Label();
        label.setFont(Font.Arial12.getItalic());
        label.setTextFill(Color.GRAY6);
        label.setAlign(Pos.CENTER_RIGHT);
        label.setPrefWidth(50);
        return label;
    }

    /**
     * Override to support custom content views for response values.
     */
    protected View createContentViewForEntry(Response response)
    {
        // Get entry as response and get response.Value
        Object value = response.getValue();

        // Handle View
        if (value instanceof View)
            return createContentViewBoxForView((View) value);

        // Handle ViewOwner
        if (value instanceof ViewOwner)
            return createContentViewForViewOwner((ViewOwner) value);

        // Handle Chart
        if (value instanceof Chart)
            return createContentViewForChart((Chart) value);

        // Handle DataSet
        if (value instanceof DataSet)
            return createContentViewForDataSet((DataSet) value);

        // Do normal version
        String responseText = response.getText();
        return createContentViewForText(responseText);
    }

    /**
     * Creates content view for ViewOwner.
     */
    private View createContentViewForText(String aString)
    {
        // Create TextArea and configure Style
        TextArea textArea = new TextArea();
        textArea.setBorder(Color.GRAY7, 1);
        textArea.setBorderRadius(4);
        textArea.setFill(DEFAULT_TEXTAREA_FILL);
        textArea.setTextFill(DEFAULT_TEXTAREA_TEXTFILL);
        textArea.setFont(Font.Arial14);

        // Configure TextArea Sizing
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 30);
        textArea.setPadding(6, 6, 6, 6);

        // Set text
        if (aString != null && aString.length() > 0)
            textArea.setText(aString);

        // Wrap in standard box
        return createContentViewBoxForView(textArea);
    }

    /**
     * Creates content view for ViewOwner.
     */
    private View createContentViewForViewOwner(ViewOwner aViewOwner)
    {
        View view = aViewOwner.getUI();
        return createContentViewBoxForView(view);
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

        // Return
        return createContentViewBoxForView(chartView);
    }

    /**
     * Creates content view for DataSet.
     */
    private View createContentViewForDataSet(DataSet dataSet)
    {
        // Create/configure DataSetPane
        DataSetPane dataSetPane = new DataSetPane(dataSet);
        View dataSetPaneView = dataSetPane.getUI();
        dataSetPaneView.setPrefWidth(560);
        dataSetPaneView.setMaxHeight(300);

        // Return
        return createContentViewBoxForView(dataSetPaneView);
    }

    /**
     * Creates content view for DataSet.
     */
    private View createContentViewBoxForView(View aView)
    {
        // Create BoxView wrapper
        BoxView boxView = new BoxView(aView, false, false);
        boxView.setFill(Color.WHITE);
        boxView.setBorderRadius(4);
        boxView.setEffect(DEFAULT_SHADOW);

        // Sizing
        boxView.setMargin(0, 0, 0, 22);

        // Return
        return boxView;
    }

    @Override
    public void requestFocus()
    {
        if (_content != null)
            _content.requestFocus();
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return RowView.getPrefWidth(this, aH);
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    @Override
    protected void layoutImpl()
    {
        RowView.layout(this, true);
    }
}
