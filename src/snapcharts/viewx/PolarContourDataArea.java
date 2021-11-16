/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.TraceStyle;
import snapcharts.model.Trace;

/**
 * A (Polar)DataArea subclass for PolarContour charts.
 */
public class PolarContourDataArea extends PolarDataArea {

    // The ContourHelper
    private ContourHelper _contourHelper;

    // The ContourShapes
    private ContourPainter _contourPainter;

    /**
     * Constructor.
     */
    public PolarContourDataArea(PolarContourChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);

        _contourHelper = aChartHelper._contourHelper;
        _contourPainter = new ContourPainter(_contourHelper, this);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Paint contours
        _contourPainter.paintAll(aPntr);

        // Repaint (semi-transparent) gridlines on top of contours
        aPntr.setOpacity(.2);
        _chartHelper.paintGridlines(aPntr);
        aPntr.setOpacity(1);
    }

    /**
     * Override to clear cached contour data/shape values.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle changes
        Object src = aPC.getSource();
        if (src== getTrace() || src instanceof Axis || src instanceof TraceStyle) {
            _contourPainter.clearContoursAll();
        }
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        // Do normal version
        super.dataViewDidChangeSize();

        // Handle changes
        _contourPainter.clearContours();
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void axisViewDidChange(PropChange aPC)
    {
        // Do normal version
        super.axisViewDidChange(aPC);

        // Handle changes
        _contourPainter.clearContours();
    }
}
