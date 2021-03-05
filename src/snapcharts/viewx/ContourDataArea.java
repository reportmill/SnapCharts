package snapcharts.viewx;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.views.DataArea;

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
    protected void paintChart(Painter aPntr)
    {
        _contourPainter.paintAll(aPntr);
    }

    /**
     * Override to clear cached contour data/shape values.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        Object src = aPC.getSource();
        if (src==getDataSet() || src instanceof Axis || src instanceof ChartTypeProps) {
            _contourPainter.clearContoursAll();
        }
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        _contourPainter.clearContours();
    }

    /**
     * Override to clear cached contour shape values.
     */
    @Override
    protected void axisViewDidChange(PropChange aPC)
    {
        _contourPainter.clearContours();
    }
}
