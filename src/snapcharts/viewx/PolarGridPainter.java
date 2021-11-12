/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Arc;
import snap.geom.Rect;
import snap.gfx.Painter;
import snapcharts.model.AxisType;
import snapcharts.model.Intervals;
import snapcharts.view.*;

/**
 * This class handles grid painting for Polar Charts.
 */
public class PolarGridPainter extends GridPainter {

    // The PolarChartHelper
    private PolarChartHelper  _polarHelper;

    // The DataArea mid points
    private double  areaMidX, areaMidY;

    /**
     * Constructor.
     */
    public PolarGridPainter(PolarChartHelper aPolarChartHelper)
    {
        super(aPolarChartHelper);
        _polarHelper = aPolarChartHelper;

        // Update area bounds
        Rect areaBnds = _polarHelper.getPolarBounds();
        areaX = areaBnds.x;
        areaY = areaBnds.y;
        areaW = areaBnds.width;
        areaH = areaBnds.height;
        areaMidX = areaX + areaW / 2;
        areaMidY = areaY + areaH / 2;
    }

    /**
     * Paints chart axis lines.
     */
    @Override
    public void paintGridlines(Painter aPntr, DataArea aDataArea)
    {
        // Get AxisViewX and update GridPainter properties
        AxisViewX axisViewX = aDataArea.getAxisViewX(); if (axisViewX == null) return;
        updateForAxisView(axisViewX);

        // Paint Radial gridlines
        paintRadialLines(aPntr, aDataArea);

        // Paint angular gridlines
        paintAngleLines(aPntr, aDataArea);
    }

    /**
     * Paints chart radial axis lines.
     */
    protected void paintRadialLines(Painter aPntr, DataArea aDataArea)
    {
        // Get X/Y AxisViews
        AxisViewX axisViewX = aDataArea.getAxisViewX(); if (axisViewX == null) return;
        AxisViewY axisViewY = aDataArea.getAxisViewY(); if (axisViewY == null) return;
        double reveal = aDataArea.getReveal();

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(gridStroke);

        // A shared arc
        Arc arc = new Arc(areaX, areaY, areaW, areaH, 0, 360);

        // Iterate over intervals and paint lines
        Intervals ivals = axisView.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {

            // Get something
            double dataRad = ivals.getInterval(i);
            double dispX = (int) Math.round(axisViewX.dataToView(dataRad));
            double dispY = (int) Math.round(axisViewY.dataToView(dataRad));
            double radLenX = dispX - areaMidX;
            double radLenY = areaMidY - dispY;
            radLenX *= reveal;
            radLenY *= reveal;

            // Draw radial
            aPntr.setColor(gridColor);
            double radX = areaMidX - radLenX;
            double radY = areaMidY - radLenY;
            arc.setRect(radX, radY, radLenX * 2, radLenY * 2);
            arc.setStartAngle(0);
            arc.setSweepAngle(-360 * reveal);
            arc.setClosure(Arc.Closure.Open);
            aPntr.draw(arc);
        }
    }

    /**
     * Paints chart angle axis lines.
     */
    protected void paintAngleLines(Painter aPntr, DataArea aDataArea)
    {
        // Get info
        double reveal = aDataArea.getReveal();
        AxisType axisTypeY = aDataArea.getAxisTypeY();

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(gridStroke);

        // Get interval max
        Intervals ivals = axisView.getIntervals();
        double dataRad = ivals.getMax() * reveal;

        // Iterate over intervals and paint lines
        for (int i = 0, iMax = 360; i < iMax; i += 15) {
            double angleRad = -Math.toRadians((i)) * (-reveal);
            double dispX = _polarHelper.polarDataToView(AxisType.X, angleRad, dataRad);
            double dispY = _polarHelper.polarDataToView(axisTypeY, angleRad, dataRad);
            aPntr.drawLine(areaMidX, areaMidY, dispX, dispY);
        }
    }
}
