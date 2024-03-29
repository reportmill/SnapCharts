/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.*;
import snap.props.PropChange;
import snapcharts.charts.*;
import snapcharts.view.TraceView;

/**
 * A TraceView subclass to display TraceType Contour.
 */
public class ContourTraceView extends TraceView {

    // The ContourHelper
    private ContourHelper _contourHelper;

    // The ContourShapes
    private ContourPainter _contourPainter;

    /**
     * Constructor.
     */
    public ContourTraceView(ContourChartHelper aChartHelper, Trace aTrace)
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
        // Paint contour data
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

        // Handle Data changes
        Object src = aPC.getSource();
        if (src== getTrace() || src instanceof Axis || src instanceof ContourAxis) {
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

        // Clear Contours
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

        // Clear Contours
        _contourPainter.clearContours();
    }
}
