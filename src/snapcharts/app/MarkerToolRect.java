/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.ViewEvent;
import snapcharts.model.Chart;
import snapcharts.model.Marker;

/**
 * A tool to create square marker.
 */
public class MarkerToolRect extends MarkerTool {

    // A default marker
    private Marker  _marker;

    /**
     * Constructor.
     */
    public MarkerToolRect(ChartPaneTools aCPT)
    {
        super(aCPT);
    }

    /**
     * Returns a default marker.
     */
    public Marker getDefaultMarker()
    {
        if (_marker != null) return _marker;
        Marker marker = createDefaultMarker(_chartView.getChart());
        return _marker = marker;
    }

    /**
     *
     */
    @Override
    public void mouseReleased(ViewEvent anEvent)
    {
        super.mousePressed(anEvent);

        //
        Rect rect = _rect;
        _rect = null;
        _tools.setCurrentTool(null);
        if (rect.width * rect.height < 50)
            return;

        // Create Marker
        Chart chart = _chartView.getChart();
        Marker marker = createDefaultMarker(chart);

        // Set Marker bounds in ChartView coords
        marker.setCoordSpaceX(Marker.CoordSpace.Chart);
        marker.setCoordSpaceY(Marker.CoordSpace.Chart);
        marker.setFractionalX(false);
        marker.setFractionalY(false);
        marker.setBounds(rect.x, rect.y, rect.width, rect.height);

        // Add Marker to chart and select it
        chart.addMarker(marker);
        _chartPane.getSel().setSelChartPart(marker);
    }

    /**
     * Paints the tool.
     */
    public void paintTool(Painter aPntr)
    {
        if (_rect == null || _rect.isEmpty())
            return;

        Marker marker = getDefaultMarker();
        Color fill = marker.getFillColor();
        aPntr.setPaint(fill);
        aPntr.fill(_rect);
        Stroke stroke = marker.getLineStroke();
        Color strokeColor = marker.getLineColor();
        aPntr.setColor(strokeColor);
        aPntr.setStroke(stroke);
        aPntr.draw(_rect);
    }
}
