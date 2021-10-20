/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Rect;
import snap.view.ViewEvent;
import snapcharts.view.ChartView;

/**
 * Base class for marker tool.
 */
public class MarkerTool {

    // The ChartPaneTools that owns this tool
    protected ChartPaneTools  _tools;

    // The ChartPane
    protected ChartPane  _chartPane;

    // The ChartView
    protected ChartView  _chartView;

    // The mouse down point
    protected double  _x0, _y0;

    // The current point
    protected double  _x1, _y1;

    // The current rect
    protected Rect  _rect;

    /**
     * Constructor.
     */
    public MarkerTool(ChartPaneTools aCPT)
    {
        _tools = aCPT;
        _chartPane = _tools._chartPane;
        _chartView = _chartPane.getChartView();
    }

    /**
     * Called when ChartPane.ChartBox gets mouse event.
     */
    public void processMouseEvent(ViewEvent anEvent)
    {
        ViewEvent.Type eventType = anEvent.getType();
        switch (eventType) {

            // Handle MousePress
            case MousePress:
                mousePressed(anEvent.copyForView(_chartView));
                anEvent.consume();
                break;

            // Handle MouseDragged
            case MouseDrag:
                mouseDragged(anEvent.copyForView(_chartView));
                anEvent.consume();
                break;

            // Handle MouseReleased
            case MouseRelease:
                mouseReleased(anEvent.copyForView(_chartView));
                anEvent.consume();
                break;

            // Anything else
            default: break;
        }
    }

    /**
     * Called when mouse pressed.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        _x0 = anEvent.getX();
        _y0 = anEvent.getY();
    }

    /**
     * Called when mouse dragged.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        _x1 = anEvent.getX();
        _y1 = anEvent.getY();
        double x = Math.min(_x0, _x1);
        double y = Math.min(_y0, _y1);
        double w = Math.max(_x0, _x1) - x;
        double h = Math.max(_y0, _y1) - y;
        _rect = new Rect(x, y, w, h);
    }

    /**
     * Called when mouse pressed.
     */
    public void mouseReleased(ViewEvent anEvent)
    {

    }
}
