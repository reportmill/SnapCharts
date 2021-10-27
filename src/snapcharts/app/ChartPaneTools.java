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

    // The Rect tool
    private MarkerToolRect  _rectTool;

    // The Pencil tool
    private MarkerToolPencil  _pencilTool;

    /**
     * Constructor.
     */
    public ChartPaneTools(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
        _rectTool = new MarkerToolRect(this);
        _pencilTool = new MarkerToolPencil(this);
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
    public void respondToolButton(ViewEvent anEvent)
    {
        if (anEvent.equals("RectToolButton"))
            setCurrentTool(_rectTool);
        else if (anEvent.equals("PencilToolButton"))
            setCurrentTool(_pencilTool);
    }

    /**
     * Adds tools.k
     */
    public void addChartPaneTools()
    {
        addChartPaneTool("PencilToolButton", "Add Sketch Annotations", "SGPencil.png");
        addChartPaneTool("RectToolButton", "Add Rect Annotations", "SGRect.png");
    }

    /**
     * Adds tools.k
     */
    public void addChartPaneTool(String buttonName, String toolTip, String imagePath)
    {
        // Create Tool button
        ToggleButton toolButton = new ToggleButton();
        toolButton.setName(buttonName);
        toolButton.setToolTip(toolTip);
        Image buttonImage = Image.get(getClass(), imagePath);
        toolButton.setImage(buttonImage);
        toolButton.setShowArea(false);
        toolButton.setPrefSize(24, 24);

        // Add to toolbar
        RowView toolBar = _chartPane.getView("ToolBar", RowView.class);
        toolBar.addChild(toolButton, 0);
    }
}
