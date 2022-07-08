/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Path;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.ViewEvent;
import snapcharts.model.Chart;
import snapcharts.model.Marker;

/**
 * A tool to create free hand path marker.
 */
public class MarkerToolPencil extends MarkerTool {

    // A path
    private Path2D  _path;

    // Constants for defaults
    private static final Color DEFAULT_LINE_COLOR = Color.RED;
    private static final Stroke DEFAULT_STROKE = Stroke.getStroke(5);

    /**
     * Constructor.
     */
    public MarkerToolPencil(ChartPaneTools aCPT)
    {
        super(aCPT);
    }

    /**
     * Override to start path.
     */
    @Override
    public void mousePressed(ViewEvent anEvent)
    {
        super.mousePressed(anEvent);

        _path = new Path2D();
        _path.moveTo(anEvent.getX(), anEvent.getY());
    }

    /**
     * Override to start update path.
     */
    @Override
    public void mouseDragged(ViewEvent anEvent)
    {
        super.mouseDragged(anEvent);

        // Add segment
        _path.lineTo(anEvent.getX(), anEvent.getY());
    }

    /**
     * Override to complete tool mouse loop.
     */
    @Override
    public void mouseReleased(ViewEvent anEvent)
    {
        super.mousePressed(anEvent);

        // Clear tool and path
        Path2D path = _path; _path = null;

        _tools.setCurrentTool(null);

        // If unusually small, just return
        Rect rect = path.getBounds();
        if (rect.width * rect.height < 50)
            return;

        // Get path at zero
        Shape pathLocal = path.copyForBounds(0, 0, rect.width, rect.height);
        Path pathFit = new Path(path);
        pathFit.fitToCurve(1);
        String svgText = pathFit.getString();

        // Create Marker with SVG
        Chart chart = _chartView.getChart();
        Marker marker = new Marker();
        marker.setName(getDefaultMarkerName(chart));
        marker.setSVG(svgText);
        marker.setLineColor(DEFAULT_LINE_COLOR);
        marker.setLineWidth((int) DEFAULT_STROKE.getWidth());

        // Set Marker bounds in ChartView coords
        marker.setCoordSpaceX(Marker.CoordSpace.ChartView);
        marker.setCoordSpaceY(Marker.CoordSpace.ChartView);
        marker.setFractionalX(false);
        marker.setFractionalY(false);
        marker.setBounds(rect.x, rect.y, rect.width, rect.height);

        // Add Marker to chart and select it
        chart.addMarker(marker);
        _chartPane.getSel().setSelChartPart(marker);
        _path = null;
    }

    /**
     * Paints the tool.
     */
    public void paintTool(Painter aPntr)
    {
        if (_path == null)
            return;

        aPntr.setColor(DEFAULT_LINE_COLOR);
        aPntr.setStroke(DEFAULT_STROKE);
        aPntr.draw(_path);
    }
}
