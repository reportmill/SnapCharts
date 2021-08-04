/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.view.DataArea;

/**
 * A DataArea subclass to display ChartType CONTOUR.
 */
public class ContourDataArea extends DataArea {

    // The ContourHelper
    private ContourHelper _contourHelper;

    // The ContourShapes
    private ContourPainter _contourPainter;

    /**
     * Constructor.
     */
    public ContourDataArea(ContourChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        _contourHelper = aChartHelper._contourHelper;
        _contourPainter = new ContourPainter(_contourHelper, this);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintDataArea(Painter aPntr)
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
        if (src==getDataSet() || src instanceof Axis || src instanceof DataStyle) {
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
