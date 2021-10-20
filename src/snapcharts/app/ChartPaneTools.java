/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Point;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.view.RowView;
import snap.view.ToggleButton;
import snap.view.View;
import snap.view.ViewEvent;

/**
 * This class handles Marker create/edit for ChartPane.
 */
public class ChartPaneTools {

    // The ChartPane
    protected ChartPane  _chartPane;

    // The current MarkerTool
    private MarkerTool  _currentTool;

    // The RectTool
    private MarkerToolRect  _rectTool;

    /**
     * Constructor.
     */
    public ChartPaneTools(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
        _rectTool = new MarkerToolRect(this);
    }

    /**
     * Returns current tool.
     */
    public MarkerTool getCurrentTool()  { return _currentTool; }

    /**
     * Sets current tool.
     */
    public void setCurrentTool(MarkerTool aMarkerTool)
    {
        // If already set, just return
        if (aMarkerTool == _currentTool) return;

        // Set value
        _currentTool = aMarkerTool;
    }

    /**
     * Called when ChartPane.ChartBox gets mouse event.
     */
    public void processMouseEvent(ViewEvent anEvent)
    {
        if (_currentTool != null) {
            _currentTool.processMouseEvent(anEvent);
        }
    }

    /**
     * Called when ChartPane repaints.
     */
    public void paintTool(Painter aPntr, View aHostView)
    {
        if (_currentTool != null) {
            aPntr.save();
            Point chartViewXY = _chartPane.getChartView().localToParent(0, 0, aHostView);
            aPntr.translate(chartViewXY.x, chartViewXY.y);
            _currentTool.paintTool(aPntr);
            aPntr.restore();
        }
    }

    /**
     * Responds to EditButton pressed.
     */
    public void respondEditButton(ViewEvent anEvent)
    {
        setCurrentTool(_rectTool);
    }

    /**
     * Adds tools.k
     */
    public void addChartPaneTools()
    {
        // Create EditButton
        ToggleButton editButton = new ToggleButton();
        editButton.setName("EditButton");
        editButton.setToolTip("Add Chart Annotations");
        Image editButtonImage = Image.get(getClass(), "SGPencil.png");
        editButton.setImage(editButtonImage);
        editButton.setShowArea(false);
        editButton.setPrefSize(24, 24);

        // Add to toolbar
        RowView toolBar = _chartPane.getView("ToolBar", RowView.class);
        toolBar.addChild(editButton, 0);
    }
}
