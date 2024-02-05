/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.props.PropChange;
import snapcharts.charts.Axis;
import snapcharts.charts.Trace;

/**
 * A (Polar)TraceView subclass for PolarContour charts.
 */
public class PolarContourTraceView extends PolarTraceView {

    // The ContourHelper
    private ContourHelper _contourHelper;

    // The ContourShapes
    private ContourPainter _contourPainter;

    /**
     * Constructor.
     */
    public PolarContourTraceView(PolarContourChartHelper aChartHelper, Trace aTrace)
    {
        super(aChartHelper, aTrace);

        _contourHelper = aChartHelper._contourHelper;
        _contourPainter = new ContourPainter(_contourHelper, this);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintTrace(Painter aPntr)
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
        if (src== getTrace() || src instanceof Axis) {
            _contourPainter.clearContoursAll();
        }
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void contentViewDidChangeSize()
    {
        // Do normal version
        super.contentViewDidChangeSize();

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
